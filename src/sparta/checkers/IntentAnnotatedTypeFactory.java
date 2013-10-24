package sparta.checkers;

import static checkers.quals.DefaultLocation.LOCAL_VARIABLE;
import static checkers.quals.DefaultLocation.OTHERWISE;
import static checkers.quals.DefaultLocation.RECEIVERS;
import static checkers.quals.DefaultLocation.RESOURCE_VARIABLE;
import static checkers.quals.DefaultLocation.UPPER_BOUNDS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javacutils.AnnotationUtils;
import javacutils.Pair;
import javacutils.TreeUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import sparta.checkers.FlowAnnotatedTypeFactory.FlowPolicyTreeAnnotator;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.IExtra;
import sparta.checkers.quals.IntentExtras;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import checkers.basetype.BaseTypeChecker;
import checkers.quals.DefaultLocation;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.TreeAnnotator;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.QualifierHierarchy;
import checkers.util.AnnotationBuilder;
import checkers.util.MultiGraphQualifierHierarchy;
import checkers.util.QualifierDefaults;
import checkers.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import checkers.util.QualifierPolymorphism;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;

public class IntentAnnotatedTypeFactory extends FlowAnnotatedTypeFactory {

	protected final AnnotationMirror INTENTEXTRAS, IEXTRA, EMPTYINTENTEXTRAS,
			INTENTEXTRASALL;

	public IntentAnnotatedTypeFactory(BaseTypeChecker checker) {
		super(checker);
		INTENTEXTRAS = AnnotationUtils.fromClass(elements, IntentExtras.class);
		IEXTRA = AnnotationUtils.fromClass(elements, IExtra.class);
		EMPTYINTENTEXTRAS = createEmptyIntentExtras(); // top
		INTENTEXTRASALL = createAllIntentExtras(); // bottom
		if (this.getClass().equals(IntentAnnotatedTypeFactory.class)) {
			this.postInit();
		}
	}

	private AnnotationMirror createAllIntentExtras() {
		// TODO: Define the bottom type of @IntentExtras
		final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
				IntentExtras.class);

		return builder.build();
	}

	private AnnotationMirror createEmptyIntentExtras() {
		final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
				IntentExtras.class);
		return builder.build();
	}

	/**
	 * This method modifies the @Source(INTENT) and @Sink(INTENT) to
	 * 
	 * @Source(ANY), @Sink({}) and modifies the @Source and @Sink from the
	 *               return type of getExtra calls. This is necessary because
	 *               the stub files will maintain @Source(INTENT) and
	 * @Sink(INTENT) annotations to perform flow check analysis without the
	 *               intent analysis.
	 */
	@Override
	public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> methodFromUse(
			MethodInvocationTree tree) {
		Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> mfuPair = super
				.methodFromUse(tree);
		if (checker instanceof IntentChecker) {
			IntentChecker intentChecker = (IntentChecker) checker;
			if (intentChecker.isGetExtraMethod(tree)) {
				// Modifying type of getExtra call
				mfuPair = changeMethodReturnType(tree, mfuPair);
				// Modifying @Source and @Sink types for parameters in getExtra
				// calls
				removeIntentFlowPermission(mfuPair.first.getParameterTypes());
			} else if (intentChecker.isPutExtraMethod(tree)) {
				// TODO: isPutExtraMethod is not working!
				// Modifying @Source and @Sink types for parameters in putExtra
				// calls
				removeIntentFlowPermission(mfuPair.first.getParameterTypes());

			}
		}
		return mfuPair;

	}

	/**
	 * 
	 * This method modifies the @Source(INTENT) and @Sink(INTENT) to
	 * 
	 * @Source(ANY) and @Sink({})
	 * 
	 * @param parametersAnnotations
	 */

	private void removeIntentFlowPermission(
			List<AnnotatedTypeMirror> parametersAnnotations) {
		for (AnnotatedTypeMirror parameterAnnotation : parametersAnnotations) {
			if (parameterAnnotation.hasAnnotation(Source.class)) {
				// Modifying @Source type
				parameterAnnotation.removeAnnotation(Source.class);
				parameterAnnotation.addAnnotation(ANYSOURCE);
			}
			if (parameterAnnotation.hasAnnotation(Sink.class)) {
				// Modifying @Sink type
				parameterAnnotation.removeAnnotation(Sink.class);
				parameterAnnotation.addAnnotation(NOSINK);
			}
		}
	}

	@Override
	protected QualifierDefaults createQualifierDefaults() {
		QualifierDefaults defaults = super.createQualifierDefaults();
		DefaultLocation[] topLocations = { LOCAL_VARIABLE, RESOURCE_VARIABLE,
				UPPER_BOUNDS, RECEIVERS };
		defaults.addAbsoluteDefaults(EMPTYINTENTEXTRAS, topLocations);
		defaults.addAbsoluteDefault(EMPTYINTENTEXTRAS, OTHERWISE);
		return defaults;
	}

	@Override
	protected TreeAnnotator createTreeAnnotator() {
		TreeAnnotator treeAnnotator = super.createTreeAnnotator();
		treeAnnotator.addTreeKind(Tree.Kind.NULL_LITERAL, EMPTYINTENTEXTRAS);
		treeAnnotator.addTreeKind(Tree.Kind.INT_LITERAL, EMPTYINTENTEXTRAS);
		treeAnnotator.addTreeKind(Tree.Kind.LONG_LITERAL, EMPTYINTENTEXTRAS);
		treeAnnotator.addTreeKind(Tree.Kind.FLOAT_LITERAL, EMPTYINTENTEXTRAS);
		treeAnnotator.addTreeKind(Tree.Kind.DOUBLE_LITERAL, EMPTYINTENTEXTRAS);
		treeAnnotator.addTreeKind(Tree.Kind.BOOLEAN_LITERAL, EMPTYINTENTEXTRAS);
		treeAnnotator.addTreeKind(Tree.Kind.CHAR_LITERAL, EMPTYINTENTEXTRAS);
		treeAnnotator.addTreeKind(Tree.Kind.STRING_LITERAL, EMPTYINTENTEXTRAS);
		return treeAnnotator;
	}

	@Override
	public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
		return new IntentQualifierHierarchy(factory);
	}

	private class IntentQualifierHierarchy extends FlowQualifierHierarchy {

		protected IntentQualifierHierarchy(MultiGraphFactory f) {
			super(f);
		}

		@Override
		protected Set<AnnotationMirror> findBottoms(
				Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
			Set<AnnotationMirror> newBottoms = super.findBottoms(supertypes);
			newBottoms.add(INTENTEXTRASALL);
			return newBottoms;
		}

		@Override
		protected Set<AnnotationMirror> findTops(
				Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
			Set<AnnotationMirror> newTops = super.findTops(supertypes);
			newTops.add(EMPTYINTENTEXTRAS);
			return newTops;
		}

		private boolean isIntentExtrasQualifier(AnnotationMirror anno) {
			if (INTENTEXTRAS.getAnnotationType() != null
					&& anno.getAnnotationType() != null) {
				return INTENTEXTRAS.getAnnotationType().asElement()
						.equals(anno.getAnnotationType().asElement());
			}
			return INTENTEXTRAS.getAnnotationType().equals(
					anno.getAnnotationType());
		}

		private boolean isIExtraQualifier(AnnotationMirror anno) {
			return IEXTRA.getAnnotationType().equals(anno.getAnnotationType());
		}

		@Override
		public AnnotationMirror getTopAnnotation(AnnotationMirror start) {
			if (isIntentExtrasQualifier(start)) {
				return EMPTYINTENTEXTRAS;
			} else if (isIExtraQualifier(start)) {
				return IEXTRA; // What to do here?
			} else {
				return super.getTopAnnotation(start);
			}
		}

		@Override
		public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
			if (isIExtraQualifier(rhs)) {
				checker.errorAbort("IntentChecker: unexpected AnnotationMirrors: "
						+ rhs + " and " + lhs);
				return false;
			} else if (isIntentExtrasQualifier(rhs)) {
				if (rhs == null || lhs == null || !isIntentExtrasQualifier(lhs)) {
					return false;
				}
				List<AnnotationMirror> rhsIExtrasList = AnnotationUtils
						.getElementValueArray(rhs, "value",
								AnnotationMirror.class, true);
				List<AnnotationMirror> lhsIExtrasList = AnnotationUtils
						.getElementValueArray(lhs, "value",
								AnnotationMirror.class, true);
				if (lhsIExtrasList.isEmpty()) {
					return true;
				}

				for (AnnotationMirror lhsIExtra : lhsIExtrasList) {
					boolean found = false;
					String leftKey = AnnotationUtils.getElementValue(lhsIExtra,
							"key", String.class, true);
					for (AnnotationMirror rhsIExtra : rhsIExtrasList) {
						String rightKey = AnnotationUtils.getElementValue(
								rhsIExtra, "key", String.class, true);
						if (rightKey.equals(leftKey)) {
							found = true;

							Set<FlowPermission> lhsAnnotatedSources = new HashSet<FlowPermission>(
									AnnotationUtils.getElementValueEnumArray(
											lhsIExtra, "source",
											FlowPermission.class, true));
							Set<FlowPermission> lhsAnnotatedSinks = new HashSet<FlowPermission>(
									AnnotationUtils.getElementValueEnumArray(
											lhsIExtra, "sink",
											FlowPermission.class, true));
							Set<FlowPermission> rhsAnnotatedSources = new HashSet<FlowPermission>(
									AnnotationUtils.getElementValueEnumArray(
											rhsIExtra, "source",
											FlowPermission.class, true));
							Set<FlowPermission> rhsAnnotatedSinks = new HashSet<FlowPermission>(
									AnnotationUtils.getElementValueEnumArray(
											rhsIExtra, "sink",
											FlowPermission.class, true));

							if (!(lhsAnnotatedSources
									.containsAll(rhsAnnotatedSources)
									&& rhsAnnotatedSources
											.containsAll(lhsAnnotatedSources)
									&& lhsAnnotatedSinks
											.containsAll(rhsAnnotatedSinks) && rhsAnnotatedSinks
										.containsAll(lhsAnnotatedSinks))) {
								return false;
							} else {
								break;
							}
						}
					}
					if (!found) {
						return false;
					}
				}
				return true;
			}
			return super.isSubtype(rhs, lhs);
		}

		/**
		 * This method receives annotations of 2 Intents and returns true if rhs
		 * can be sent to lsh. For that to happen, every key in lhs need to
		 * exists in rhs, and the @Source and @Sink with that key in rhs needs
		 * to be a subtype of the
		 * 
		 * @Source and @Sink with that same key in lhs.
		 * @param rhs
		 *            Sender intent annotations
		 * @param lhs
		 *            Receiver intent annotations
		 * @return true if the intent with annotations rhs can be sent to the
		 *         intent with annotations lhs.
		 */

		public boolean isCopyableTo(AnnotationMirror rhs, AnnotationMirror lhs) {
			if (rhs == null || lhs == null) {
				return false;
			}
			List<AnnotationMirror> rhsIExtrasList = AnnotationUtils
					.getElementValueArray(rhs, "value", AnnotationMirror.class,
							true);
			List<AnnotationMirror> lhsIExtrasList = AnnotationUtils
					.getElementValueArray(lhs, "value", AnnotationMirror.class,
							true);
			if (lhsIExtrasList.isEmpty()) {
				return true;
			}

			// Iterating on the @IExtra from lhs
			for (AnnotationMirror lhsIExtra : lhsIExtrasList) {
				boolean found = false;
				String leftKey = AnnotationUtils.getElementValue(lhsIExtra,
						"key", String.class, true);
				for (AnnotationMirror rhsIExtra : rhsIExtrasList) {
					String rightKey = AnnotationUtils.getElementValue(
							rhsIExtra, "key", String.class, true);
					if (rightKey.equals(leftKey)) {
						// Found 2 @IExtra with same keys in rhs and lhs.
						// Now we need to make sure that @Source and @Sink of
						// the @IExtra in rhs are subtypes of @Source and @Sink
						// of @IExtra in lhs.

						found = true;
						Set<FlowPermission> lhsAnnotatedSources = new HashSet<FlowPermission>(
								AnnotationUtils.getElementValueEnumArray(
										lhsIExtra, "source",
										FlowPermission.class, true));
						Set<FlowPermission> lhsAnnotatedSinks = new HashSet<FlowPermission>(
								AnnotationUtils.getElementValueEnumArray(
										lhsIExtra, "sink",
										FlowPermission.class, true));
						Set<FlowPermission> rhsAnnotatedSources = new HashSet<FlowPermission>(
								AnnotationUtils.getElementValueEnumArray(
										rhsIExtra, "source",
										FlowPermission.class, true));
						Set<FlowPermission> rhsAnnotatedSinks = new HashSet<FlowPermission>(
								AnnotationUtils.getElementValueEnumArray(
										rhsIExtra, "sink",
										FlowPermission.class, true));

						AnnotationMirror lhsSourceAnnotation = createAnnoFromSource(lhsAnnotatedSources);
						AnnotationMirror lhsSinkAnnotation = createAnnoFromSink(lhsAnnotatedSinks);
						AnnotationMirror rhsSourceAnnotation = createAnnoFromSource(rhsAnnotatedSources);
						AnnotationMirror rhsSinkAnnotation = createAnnoFromSink(rhsAnnotatedSinks);

						if (isSubtype(rhsSourceAnnotation, lhsSourceAnnotation)
								&& isSubtype(rhsSinkAnnotation,
										lhsSinkAnnotation)) {
							return false;
						} else {
							// if it is a subtype, exit the last loop and
							// check another @IExtra in lhs
							break;
						}
					}
				}
				if (!found) {
					// If key is missing on the rhs, return false
					return false;
				}
			}
			return true;
		}

		/**
		 * The LUB between 2 @IntentExtras is an @IntentExtras containing 
		 * all the @IExtra with keys both have in common. For each pair of 2
		 * @IExtra with the same key in the 2 @IntentExtras, the resulting @Source 
		 * is the union of the @Source of both @IExtra, and the resulting @Sink 
		 * is the intersection of @Sink in both @IExtra.
		 */

		@Override
		public AnnotationMirror leastUpperBound(AnnotationMirror a1,
				AnnotationMirror a2) {
			if (isSubtype(a1, a2))
				return a2;
			if (isSubtype(a2, a1))
				return a1;

			if (AnnotationUtils.areSameIgnoringValues(a1, a2)) {
				if (AnnotationUtils.areSameIgnoringValues(a1, INTENTEXTRAS)) {
					List<AnnotationMirror> a1IExtrasList = AnnotationUtils
							.getElementValueArray(a1, "value",
									AnnotationMirror.class, true);
					List<AnnotationMirror> IExtraOutputSet = new ArrayList<AnnotationMirror>();
					
					for (AnnotationMirror a1IExtra : a1IExtrasList) {
						String a1IExtraKey = AnnotationUtils.getElementValue(
								a1IExtra, "key", String.class, true);
						if(IntentUtils.hasKey(a2, a1IExtraKey)) {
							AnnotationMirror a2IExtra = IntentUtils.getIExtraWithKey(a2, a1IExtraKey);
							//Here we have found matching keys.
							//First do the union of sources:
							Set<FlowPermission> unionSources = IntentUtils.unionSourcesIExtras(a1IExtra, a2IExtra);

							//Intersection of sinks:
							Set<FlowPermission> intersectedSinks = IntentUtils.intersectionSinksIExtras(a1IExtra, a2IExtra);
							
							//Create a new IExtra with the results of sources and sinks
							AnnotationMirror newIExtra = IntentUtils.createIExtraAnno(a1IExtraKey, createAnnoFromSource(unionSources), createAnnoFromSink(intersectedSinks),processingEnv);
							IExtraOutputSet.add(newIExtra);
							break;
						}

					}
					AnnotationMirror output = IntentUtils.addIExtrasToIntentExtras(EMPTYINTENTEXTRAS, IExtraOutputSet,processingEnv);
					return output;
				} else if (AnnotationUtils.areSameIgnoringValues(a1, IEXTRA)) {
					// is this one necessary?
				}
			}

			return super.leastUpperBound(a1, a2);
		}
		
		/**
		 * The GLB between 2 @IntentExtras will contain the union of keys
		 * that these annotations contain, and the @Source of the @IExtra
		 * with this key will be the intersection of sources and the @Sink
		 * will be the union of sinks.
		 */

		@Override
		public AnnotationMirror greatestLowerBound(AnnotationMirror a1,
				AnnotationMirror a2) {
			// What would be the GLB between (Key k1, source s1) and (Key k1,
			// source s2) ?
			// (Key k1, source empty) ? I think so. Need to do the same on LUB.
			if (AnnotationUtils.areSame(a1, a2))
                return a1;
			
			if (AnnotationUtils.areSameIgnoringValues(a1, a2)) {
				if (AnnotationUtils.areSameIgnoringValues(a1, INTENTEXTRAS)) {
					List<AnnotationMirror> a1IExtrasList = AnnotationUtils
							.getElementValueArray(a1, "value",
									AnnotationMirror.class, true);
					List<AnnotationMirror> a2IExtrasList = AnnotationUtils
							.getElementValueArray(a2, "value",
									AnnotationMirror.class, true);
					List<AnnotationMirror> IExtraOutputSet = new ArrayList<AnnotationMirror>();
					for (AnnotationMirror a1IExtra : a1IExtrasList) {
						String a1IExtraKey = AnnotationUtils.getElementValue(
								a1IExtra, "key", String.class, true);
						if(IntentUtils.hasKey(a2, a1IExtraKey)) {
							AnnotationMirror a2IExtra = IntentUtils.getIExtraWithKey(a2, a1IExtraKey);
							//If we have found matching keys:
							//First do the intersection of sources:
							Set<FlowPermission> intersectedSources = IntentUtils.intersectionSourcesIExtras(a1IExtra, a2IExtra);

							//Union of sinks:
							Set<FlowPermission> unionSinks = IntentUtils.unionSinksIExtras(a1IExtra, a2IExtra);
							
							//Create a new IExtra with the results of sources and sinks
							AnnotationMirror newIExtra = IntentUtils.createIExtraAnno(a1IExtraKey, createAnnoFromSource(intersectedSources), createAnnoFromSink(unionSinks),processingEnv);
							IExtraOutputSet.add(newIExtra);
							break;
						} else {
						//If we could not find a key in a2, add the @IExtra with
						//this key to the resulting @IntentExtras
							IExtraOutputSet.add(a1IExtra);
						}

					}
					//Now we need to fill the resulting set with @IExtra containing keys 
					//that are in a2 but not in a1.
					for (AnnotationMirror a2IExtra : a2IExtrasList) {
						String a2IExtraKey = AnnotationUtils.getElementValue(
								a2IExtra, "key", String.class, true);
						if(!IntentUtils.hasKey(a1, a2IExtraKey)) {
							IExtraOutputSet.add(a2IExtra);
						}
					}
					
					AnnotationMirror output = IntentUtils.addIExtrasToIntentExtras(EMPTYINTENTEXTRAS, IExtraOutputSet,processingEnv);
					return output;
				} else if (AnnotationUtils.areSameIgnoringValues(a1, IEXTRA)) {
					// is this one necessary?
				}
			}
			
			return super.greatestLowerBound(a1, a2);
		}

	}

	/**
	 * This method changes the return type of a
	 * <code>Intent.getExtra(key)</code> call depending on the @IntentExtras
	 * type of Intent and <code>key</code>.
	 * 
	 * @param tree
	 *            The method tree
	 * @param origResult
	 *            The original result
	 * @return
	 */

	public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> changeMethodReturnType(
			MethodInvocationTree tree,
			Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> origResult) {
		if (tree != null) {
			ExpressionTree receiver = TreeUtils.getReceiverTree(tree);
			AnnotatedTypeMirror receiverType = getAnnotatedType(receiver);
			String keyName = tree.getArguments().get(0).toString();
			// Removing "" from key. "key" -> key
			keyName = keyName.substring(1, keyName.length() - 1);
			if (receiverType.hasAnnotation(IntentExtras.class)) {

				AnnotationMirror receiverIntentAnnotation = receiverType
						.getAnnotation(IntentExtras.class);
				List<AnnotationMirror> iExtrasList = AnnotationUtils
						.getElementValueArray(receiverIntentAnnotation,
								"value", AnnotationMirror.class, true);
				// Here we have the list of IExtra from the Intent
				// We iterate this list until we find the key we need.
				for (AnnotationMirror iExtra : iExtrasList) {
					String key = AnnotationUtils.getElementValue(iExtra, "key",
							String.class, true);
					if (key.equals(keyName)) {
						// Found the key, now change the annotation of the
						// return of getExtra() to the
						// correct @Source and @Sink annotations
						Set<FlowPermission> annotatedSources = new HashSet<FlowPermission>(
								AnnotationUtils.getElementValueEnumArray(
										iExtra, "source", FlowPermission.class,
										true));
						Set<FlowPermission> annotatedSinks = new HashSet<FlowPermission>(
								AnnotationUtils.getElementValueEnumArray(
										iExtra, "sink", FlowPermission.class,
										true));

						AnnotationMirror sourceAnnotation = createAnnoFromSource(annotatedSources);
						AnnotationMirror sinkAnnotation = createAnnoFromSink(annotatedSinks);
						origResult.first.getReturnType().clearAnnotations();
						origResult.first.getReturnType().addAnnotation(
								sourceAnnotation);
						origResult.first.getReturnType().addAnnotation(
								sinkAnnotation);

						if (!origResult.first.getReturnType().hasAnnotation(
								IntentExtras.class)) {
							origResult.first.getReturnType().addAnnotation(
									EMPTYINTENTEXTRAS);
						}
						return origResult;
					}
				}
			}
		}
		/*
		 * Return original result if resolution failed
		 */
		return origResult;
	}

	
	/**
	 * TODO: This method is duplicated in the IntentQualifierHierarchy. It
	 * should only be there, but I need the IntentVisitor to have visibility of
	 * this method in order to type-check a send intent call, this is why it is
	 * also here now.
	 * 
	 * This method receives annotations of 2 Intents and returns true if rhs can
	 * be sent to lsh. For that to happen, every key in lhs need to exists in
	 * rhs, and the @Source and @Sink with that key in rhs needs to be a subtype
	 * of the
	 * 
	 * @Source and @Sink with that same key in lhs.
	 * @param rhs
	 *            Sender intent annotations
	 * @param lhs
	 *            Receiver intent annotations
	 * @return true if the intent with annotations rhs can be sent to the intent
	 *         with annotations lhs.
	 */

	public boolean isCopyableTo(AnnotationMirror rhs, AnnotationMirror lhs) {
		if (rhs == null || lhs == null) {
			return false;
		}
		List<AnnotationMirror> rhsIExtrasList = AnnotationUtils
				.getElementValueArray(rhs, "value", AnnotationMirror.class,
						true);
		List<AnnotationMirror> lhsIExtrasList = AnnotationUtils
				.getElementValueArray(lhs, "value", AnnotationMirror.class,
						true);
		if (lhsIExtrasList.isEmpty()) {
			return true;
		}

		// Iterating on the @IExtra from lhs
		for (AnnotationMirror lhsIExtra : lhsIExtrasList) {
			boolean found = false;
			String leftKey = AnnotationUtils.getElementValue(lhsIExtra, "key",
					String.class, true);
			for (AnnotationMirror rhsIExtra : rhsIExtrasList) {
				String rightKey = AnnotationUtils.getElementValue(rhsIExtra,
						"key", String.class, true);
				if (rightKey.equals(leftKey)) {
					// Found 2 @IExtra with same keys in rhs and lhs.
					// Now we need to make sure that @Source and @Sink of
					// the @IExtra in rhs are subtypes of @Source and @Sink
					// of @IExtra in lhs.

					found = true;
					Set<FlowPermission> lhsAnnotatedSources = new HashSet<FlowPermission>(
							AnnotationUtils.getElementValueEnumArray(lhsIExtra,
									"source", FlowPermission.class, true));
					Set<FlowPermission> lhsAnnotatedSinks = new HashSet<FlowPermission>(
							AnnotationUtils.getElementValueEnumArray(lhsIExtra,
									"sink", FlowPermission.class, true));
					Set<FlowPermission> rhsAnnotatedSources = new HashSet<FlowPermission>(
							AnnotationUtils.getElementValueEnumArray(rhsIExtra,
									"source", FlowPermission.class, true));
					Set<FlowPermission> rhsAnnotatedSinks = new HashSet<FlowPermission>(
							AnnotationUtils.getElementValueEnumArray(rhsIExtra,
									"sink", FlowPermission.class, true));

					TypeMirror dummy = processingEnv.getTypeUtils()
							.getPrimitiveType(TypeKind.BOOLEAN);
					AnnotatedTypeMirror lhsAnnotatedType = AnnotatedTypeMirror
							.createType(dummy, this);
					AnnotatedTypeMirror rhsAnnotatedType = AnnotatedTypeMirror
							.createType(dummy, this);

					AnnotationMirror lhsSourceAnnotation = createAnnoFromSource(lhsAnnotatedSources);
					AnnotationMirror lhsSinkAnnotation = createAnnoFromSink(lhsAnnotatedSinks);
					AnnotationMirror rhsSourceAnnotation = createAnnoFromSource(rhsAnnotatedSources);
					AnnotationMirror rhsSinkAnnotation = createAnnoFromSink(rhsAnnotatedSinks);

					lhsAnnotatedType.addAnnotation(lhsSourceAnnotation);
					lhsAnnotatedType.addAnnotation(lhsSinkAnnotation);
					rhsAnnotatedType.addAnnotation(rhsSourceAnnotation);
					rhsAnnotatedType.addAnnotation(rhsSinkAnnotation);
					lhsAnnotatedType.addAnnotation(EMPTYINTENTEXTRAS);
					rhsAnnotatedType.addAnnotation(EMPTYINTENTEXTRAS);
					if (!getTypeHierarchy().isSubtype(rhsAnnotatedType,
							lhsAnnotatedType)) {
						return false;
					} else {
						// if it is a subtype, exit the last loop and
						// check another @IExtra in lhs
						break;
					}
				}
			}
			if (!found) {
				// If key is missing on the rhs, return false
				return false;
			}
		}
		return true;
	}

}
