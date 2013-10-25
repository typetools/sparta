package sparta.checkers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javacutils.AnnotationUtils;
import javacutils.InternalUtils;
import javacutils.TreeUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import com.sun.source.tree.MethodInvocationTree;

import checkers.util.AnnotationBuilder;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.IExtra;
import sparta.checkers.quals.IntentExtras;

public class IntentUtils {
	
	private static List<String> GETEXTRA_SIGNATURES = Arrays
			.asList(new String[] { "getStringExtra", "getStringArrayListExtra",
					"getStringArrayExtra", "getShortExtra",
					"getShortArrayExtra", "getSerializableExtra",
					"getParcelableExtra", "getParcelableArrayListExtra",
					"getParcelableArrayExtra", "getLongExtra",
					"getLongArrayExtra", "getIntegerArrayListExtra",
					"getIntExtra", "getIntArrayExtra", "getFloatExtra",
					"getFloatArrayExtra", "getDoubleExtra",
					"getDoubleArrayExtra", "getCharSequenceExtra",
					"getCharSequenceArrayListExtra",
					"getCharSequenceArrayExtra", "getCharExtra",
					"getCharArrayExtra", "getByteExtra", "getByteArrayExtra",
					"getBundleExtra", "getBooleanExtra", "getBooleanArrayExtra" });

	private static List<String> PUTEXTRA_SIGNATURES = Arrays
			.asList(new String[] { "putExtra", "putCharSequenceArrayListExtra",
					"putIntegerArrayListExtra", "putParcelableArrayListExtra",
					"putStringArrayListExtra" });

	/**
	 * Method that receives an @IntentExtras and a <code> key </code>
	 * and return the @IExtra with that key and <code>null</code> if it 
	 * does not contain the key.
	 */
	public static AnnotationMirror getIExtraWithKey(AnnotationMirror intentExtras, String key) {
		List<AnnotationMirror> iExtrasList = AnnotationUtils
				.getElementValueArray(intentExtras, "value",
						AnnotationMirror.class, true);
		for(AnnotationMirror iExtra : iExtrasList) {
			String iExtraKey = AnnotationUtils.getElementValue(
					iExtra, "key", String.class, true);
			if(iExtraKey.equals(key)) {
				return iExtra;
			}
		}
		return null;
	}
	
	/**
	 * Return true if @IntentExtras has this key
	 */
	
	public static boolean hasKey(AnnotationMirror intentExtras, String key) {
		List<AnnotationMirror> iExtrasList = AnnotationUtils
				.getElementValueArray(intentExtras, "value",
						AnnotationMirror.class, true);
		for(AnnotationMirror iExtra : iExtrasList) {
			String iExtraKey = AnnotationUtils.getElementValue(
					iExtra, "key", String.class, true);
			if(iExtraKey.equals(key)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return the union of sources from 2 @IExtra annotations
	 */
	
	public static Set<FlowPermission> unionSourcesIExtras(AnnotationMirror iExtra1, AnnotationMirror iExtra2) {
		Set<FlowPermission> a1AnnotatedSources = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra1, "source",
								FlowPermission.class,
								true));
		Set<FlowPermission> a2AnnotatedSources = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra2, "source",
								FlowPermission.class,
								true));
		a1AnnotatedSources.addAll(a2AnnotatedSources);
		return Flow.convertToAnySource(a1AnnotatedSources,true);

	}
	
	/**
	 * Return the union of sinks from 2 @IExtra annotations
	 */
	
	public static Set<FlowPermission> unionSinksIExtras(AnnotationMirror iExtra1, AnnotationMirror iExtra2) {
		Set<FlowPermission> a1AnnotatedSinks = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra1, "sink",
								FlowPermission.class,
								true));
		Set<FlowPermission> a2AnnotatedSinks = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra2, "sink",
								FlowPermission.class,
								true));
		a1AnnotatedSinks.addAll(a2AnnotatedSinks);
		return Flow.convertToAnySink(a1AnnotatedSinks,true);

	}
	
	/**
	 * Return the intersection of sources from 2 @IExtra annotations
	 */
	
	public static Set<FlowPermission> intersectionSourcesIExtras(AnnotationMirror iExtra1, AnnotationMirror iExtra2) {
		Set<FlowPermission> a1AnnotatedSources = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra1, "source",
								FlowPermission.class,
								true));
		Set<FlowPermission> a2AnnotatedSources = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra2, "source",
								FlowPermission.class,
								true));
		return Flow.intersectSinks(a1AnnotatedSources, a2AnnotatedSources);

	}
	
	/**
	 * Return the intersection of sinks from 2 @IExtra annotations
	 */
	
	public static Set<FlowPermission> intersectionSinksIExtras(AnnotationMirror iExtra1, AnnotationMirror iExtra2) {
		Set<FlowPermission> a1AnnotatedSinks = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra1, "sink",
								FlowPermission.class,
								true));
		Set<FlowPermission> a2AnnotatedSinks = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra2, "sink",
								FlowPermission.class,
								true));
		return Flow.intersectSinks(a1AnnotatedSinks, a2AnnotatedSinks);

	}
	
	/**
	 * Creates a new IExtra AnnotationMirror
	 * @param key
	 * @param sources
	 * @param sinks
	 * @param processingEnv
	 * @return
	 */
	
	public static AnnotationMirror createIExtraAnno(String key,
			AnnotationMirror sources, AnnotationMirror sinks, ProcessingEnvironment processingEnv) {
		final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
				IExtra.class);
		Set<FlowPermission> sourcesSet = Flow.getSources(sources);
		Set<FlowPermission> sinksSet = Flow.getSinks(sinks);
		builder.setValue("key", key);
		builder.setValue("source",
				sourcesSet.toArray(new FlowPermission[sourcesSet.size()]));
		builder.setValue("sink",
				sinksSet.toArray(new FlowPermission[sinksSet.size()]));
		return builder.build();
	}
	
	/**
	 * Returns a new @IntentExtras containing all @IExtra from <code>intentExtras</code>
	 * and a new <code>IExtra</code>.
	 * @param intentExtras
	 * @param iExtra
	 * @param processingEnv
	 * @return
	 */

	public static AnnotationMirror addIExtraToIntentExtras(
			AnnotationMirror intentExtras, AnnotationMirror iExtra, ProcessingEnvironment processingEnv) {
		final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
				IntentExtras.class);
		List<AnnotationMirror> iExtrasList = AnnotationUtils
				.getElementValueArray(intentExtras, "value",
						AnnotationMirror.class, true);
		iExtrasList.add(iExtra);
		builder.setValue("value", iExtrasList.toArray());
		return builder.build();
	}
	
	/**
	 * Returns a new @IntentExtras containing all @IExtra from <code>intentExtras</code>
	 * and all <code>IExtras</code>. 
	 * @param intentExtras
	 * @param iExtras
	 * @param processingEnv
	 * @return
	 */
	
	public static AnnotationMirror addIExtrasToIntentExtras(AnnotationMirror intentExtras, List<AnnotationMirror> iExtras, ProcessingEnvironment processingEnv) {
		AnnotationMirror result = intentExtras;
		for (AnnotationMirror iExtra : iExtras) {
			result = addIExtraToIntentExtras(result, iExtra, processingEnv);
		}
		return result;
	}
	
	/**
	 * Returns true if the MethodInvocationTree corresponds to one of the <code>Intent.getExtra()</code> calls
	 * @param tree
	 * @return
	 */

	public static boolean isGetExtraMethod(MethodInvocationTree tree, ProcessingEnvironment processingEnv) {
		for (String getExtraSignature : GETEXTRA_SIGNATURES) {
			//The getExtra call can have 1 or 2 parameters,
			//2 when there is a use of default parameter, 1 otherwise.
			ExecutableElement getExtra = TreeUtils.getMethod(
					"android.content.Intent", getExtraSignature, 1, processingEnv);
			ExecutableElement getExtraWithDefault = TreeUtils.getMethod(
					"android.content.Intent", getExtraSignature, 2, processingEnv);
			if (getExtra != null
					&& TreeUtils.isMethodInvocation(tree, getExtra,
							processingEnv)) {
				return true;
			}
			if (getExtraWithDefault != null
					&& TreeUtils.isMethodInvocation(tree, getExtraWithDefault,
							processingEnv)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the MethodInvocationTree corresponds to one of the <code>Intent.putExtra()</code> calls
	 * TODO: It cannot be implemented the same way the isGetExtraMethod() was implemented.
	 * The problem is that there are several putExtra signatures with the same amount of parameters and name
	 * and the TreeUtils.getMethod() cannot differentiate between them, it always returns the putExtra(String,boolean).
	 * If tree is putExtra(String,String) it won't pass this method.
	 * @param tree
	 * @return
	 */

	public static boolean isPutExtraMethod(MethodInvocationTree tree) {
		Element ele = (Element) InternalUtils.symbol(tree);
        if(ele instanceof ExecutableElement){
            ExecutableElement method = (ExecutableElement) ele;
            return PUTEXTRA_SIGNATURES.contains(method.getSimpleName().toString());
        }
        return false;
//				// correct way to do it. the problem is that there are several
//				// putExtra methods with the same name and all
//				// of them has the same number of paremeters. How to get each
//				// one of them? TreeUtils.getMethod returns only
//				// the first one.
//				// ExecutableElement putExtra = TreeUtils.getMethod(
//				// "android.content.Intent", s, 2, processingEnv);
//				// if (putExtra != null
//				// && TreeUtils.isMethodInvocation(tree, putExtra,
//				// processingEnv)) {
//				// return true;
//				// }
	}
	
}
