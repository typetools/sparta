package sparta.checkers.poly;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.checkerframework.framework.qual.PolyAll;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedNullType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedWildcardType;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.visitor.AnnotatedTypeScanner;
import org.checkerframework.framework.type.visitor.SimpleAnnotatedTypeVisitor;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TypesUtils;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;

/**
 * Abstract class that can be extend for different kinds of polymorphism. Ported
 * from org.checkerframework.framework.util.QualifierPolymorphism
 *
 */
public abstract class QualifierPolymorphism {

    private final Types types;

    protected final AnnotatedTypeFactory atypeFactory;

    protected final Completer completer;

    /**
     * The polymorphic qualifiers: mapping from the top of a qualifier hierarchy
     * to the polymorphic qualifier of that hierarchy. Field always non-null but
     * might be an empty mapping. The "null" key, if present, always maps to
     * PolyAll.
     */
    protected final Map<AnnotationMirror, AnnotationMirror> polyQuals;

    /** The qualifiers at the top of the qualifier hierarchy. */
    protected final Set<? extends AnnotationMirror> topQuals;

    /** The qualifier hierarchy to use. */
    protected final QualifierHierarchy qualhierarchy;

    private final AnnotationMirror POLYALL;

    /**
     * Creates a {@link QualifierPolymorphism} instance that uses the given
     * checker for querying type qualifiers and the given factory for getting
     * annotated types.
     *
     * @param env
     *            the processing environment
     * @param factory
     *            the factory for the current checker
     */
    public QualifierPolymorphism(ProcessingEnvironment env,
            AnnotatedTypeFactory factory) {
        this.atypeFactory = factory;

        this.types = env.getTypeUtils();

        Elements elements = env.getElementUtils();
        POLYALL = AnnotationBuilder.fromClass(elements, PolyAll.class);
        this.qualhierarchy = factory.getQualifierHierarchy();

        Map<AnnotationMirror, AnnotationMirror> polys = getPolyQualifiers(elements);

        this.polyQuals = polys;
        this.topQuals = qualhierarchy.getTopAnnotations();

        this.collector = new PolyCollector();
        this.completer = new Completer();
    }

    /**
     * Returns a mapping from an annotation to be used in the event that the
     * polymorphic qualifier cannot be resolved to the polymorphic qualifier.
     * The two annotations should be in the same hierarchy
     *
     * @param elements
     * @return map of polyqualifier to default qualifier
     */
    public abstract Map<AnnotationMirror, AnnotationMirror> getPolyQualifiers(
            Elements elements);

    public static boolean isPolyAll(AnnotationMirror qual) {
        return AnnotationUtils.areSameByClass(qual, PolyAll.class);
    }

    /**
     * Resolves polymorphism annotations for the given type.
     *
     * @param tree
     *            the tree associated with the type
     * @param type
     *            the type to annotate
     */
    public abstract void annotate(MethodInvocationTree tree,
            AnnotatedExecutableType type);

    public abstract void annotate(NewClassTree tree,
            AnnotatedExecutableType type);

    protected final AnnotatedTypeScanner<Void, Map<AnnotationMirror, Set<? extends AnnotationMirror>>> replacer = new AnnotatedTypeScanner<Void, Map<AnnotationMirror, Set<? extends AnnotationMirror>>>() {
        @Override
        public Void scan(AnnotatedTypeMirror type,
                Map<AnnotationMirror, Set<? extends AnnotationMirror>> matches) {
            if (type != null) {
                for (Map.Entry<AnnotationMirror, Set<? extends AnnotationMirror>> pqentry : matches
                        .entrySet()) {
                    AnnotationMirror poly = pqentry.getKey();
                    if (poly != null && type.hasAnnotation(poly)) {
                        type.removeAnnotation(poly);
                        Set<? extends AnnotationMirror> quals = pqentry
                                .getValue();
                        type.replaceAnnotations(quals);
                    }
                }
            }
            return super.scan(type, matches);
        }
    };

    /**
     * Completes a type by removing any unresolved polymorphic qualifiers,
     * replacing them with the top qualifiers.
     */
    public class Completer extends AnnotatedTypeScanner<Void, Void> {
        @Override
        protected Void scan(AnnotatedTypeMirror type, Void p) {
            if (type != null) {
                for (Map.Entry<AnnotationMirror, AnnotationMirror> pqentry : polyQuals
                        .entrySet()) {
                    AnnotationMirror top = pqentry.getKey();
                    AnnotationMirror poly = pqentry.getValue();

                    if (type.hasAnnotation(poly)) {
                        addDefaultForPolyQualifier(type, top, poly);
                    }
                }
            }
            return super.scan(type, p);
        }

    }

    protected void addDefaultForPolyQualifier(AnnotatedTypeMirror type,
            AnnotationMirror top, AnnotationMirror poly) {
        type.removeAnnotation(poly);
        if (top == null) {
            // poly is PolyAll -> add all tops not explicitly given
            type.addMissingAnnotations(topQuals);
        } else if (type.getKind() != TypeKind.TYPEVAR
                && type.getKind() != TypeKind.WILDCARD) {
            // Do not add the top qualifiers to type variables and wildcards
            type.addAnnotation(top);
        }
    }

    protected final PolyCollector collector;

    /**
     * A Helper class that tries to resolve the polymorhpic qualifiers with the
     * most restricted qualifier. The mapping is from the polymorhpic qualifier
     * to the substitution for that qualifier, which is a set of qualifiers. For
     * most polymorphic qualifiers this will be a singleton set. For the @PolyAll
     * qualifier, this might be a set of qualifiers.
     */
    public class PolyCollector
            extends
            SimpleAnnotatedTypeVisitor<Map<AnnotationMirror, Set<? extends AnnotationMirror>>, AnnotatedTypeMirror> {

        public Map<AnnotationMirror, Set<? extends AnnotationMirror>> reduce(
                Map<AnnotationMirror, Set<? extends AnnotationMirror>> r1,
                Map<AnnotationMirror, Set<? extends AnnotationMirror>> r2) {

            if (r1 == null || r1.isEmpty())
                return r2;
            if (r2 == null || r2.isEmpty())
                return r1;

            Map<AnnotationMirror, Set<? extends AnnotationMirror>> res = new HashMap<AnnotationMirror, Set<? extends AnnotationMirror>>(
                    r1.size());
            // Ensure that all qualifiers from r1 and r2 are visited.
            Set<AnnotationMirror> r2remain = AnnotationUtils
                    .createAnnotationSet();
            r2remain.addAll(r2.keySet());
            for (Map.Entry<AnnotationMirror, Set<? extends AnnotationMirror>> kv1 : r1
                    .entrySet()) {
                AnnotationMirror key1 = kv1.getKey();
                Set<? extends AnnotationMirror> a1Annos = kv1.getValue();
                Set<? extends AnnotationMirror> a2Annos = r2.get(key1);
                if (a2Annos != null && !a2Annos.isEmpty()) {
                    r2remain.remove(key1);
                    Set<? extends AnnotationMirror> lubs = qualhierarchy
                            .leastUpperBounds(a1Annos, a2Annos);
                    res.put(key1, lubs);
                } else {
                    res.put(key1, a1Annos);
                }
            }
            for (AnnotationMirror key2 : r2remain) {
                res.put(key2, r2.get(key2));
            }
            return res;
        }

        public Map<AnnotationMirror, Set<? extends AnnotationMirror>> visit(
                Iterable<? extends AnnotatedTypeMirror> types,
                Iterable<? extends AnnotatedTypeMirror> actualTypes) {
            Map<AnnotationMirror, Set<? extends AnnotationMirror>> result = new HashMap<AnnotationMirror, Set<? extends AnnotationMirror>>();

            Iterator<? extends AnnotatedTypeMirror> itert = types.iterator();
            Iterator<? extends AnnotatedTypeMirror> itera = actualTypes
                    .iterator();

            while (itert.hasNext() && itera.hasNext()) {
                AnnotatedTypeMirror type = itert.next();
                AnnotatedTypeMirror actualType = itera.next();
                result = reduce(result, visit(type, actualType));
            }
            return result;
        }

        @Override
        public Map<AnnotationMirror, Set<? extends AnnotationMirror>> visitDeclared(
                AnnotatedDeclaredType type, AnnotatedTypeMirror actualType) {
            if (actualType.getKind() == TypeKind.INTERSECTION) {
                // We don't support poly annotations on intersection types
                // See Issue 744
                // https://github.com/typetools/checker-framework/issues/744
                return Collections.emptyMap();
            }
            if (actualType.getKind() == TypeKind.TYPEVAR) {
                if (visited.contains(actualType.getUnderlyingType()))
                    return Collections.emptyMap();
                visited.add(actualType.getUnderlyingType());
                Map<AnnotationMirror, Set<? extends AnnotationMirror>> result = visit(
                        type,
                        ((AnnotatedTypeVariable) actualType).getUpperBound());
                visited.remove(actualType.getUnderlyingType());
                return result;
            }

            if (actualType.getKind() == TypeKind.WILDCARD) {
                if (visited.contains(actualType.getUnderlyingType()))
                    return Collections.emptyMap();
                AnnotatedWildcardType wctype = (AnnotatedWildcardType) actualType;

                visited.add(actualType.getUnderlyingType());

                Map<AnnotationMirror, Set<? extends AnnotationMirror>> result;
                if (wctype.getUnderlyingType().getExtendsBound() != null) {
                    result = visit(type, wctype.getExtendsBound());
                } else if (wctype.getUnderlyingType().getSuperBound() != null) {
                    if (TypesUtils.isErasedSubtype(type.getUnderlyingType(),
                            wctype.getSuperBound().getUnderlyingType(), types)) {
                        result = visit(type, wctype.getSuperBound());
                    } else {
                        AnnotatedTypeMirror superBoundAsSuper =
                                AnnotatedTypes.asSuper(atypeFactory, wctype.getSuperBound(), type);
                        result = visit(type, superBoundAsSuper);
                    }
                } else {
                    result = Collections.emptyMap();
                }
                visited.remove(actualType.getUnderlyingType());
                return result;
            }

            if (actualType.getKind() != type.getKind() || actualType == type) {
                return Collections.emptyMap();
            }

            assert actualType.getKind() == type.getKind();
            type = (AnnotatedDeclaredType) AnnotatedTypes.asSuper(atypeFactory, type, actualType);

            AnnotatedDeclaredType dcType = (AnnotatedDeclaredType) actualType;

            Map<AnnotationMirror, Set<? extends AnnotationMirror>> result = new HashMap<AnnotationMirror, Set<? extends AnnotationMirror>>();

            for (Map.Entry<AnnotationMirror, AnnotationMirror> kv : polyQuals
                    .entrySet()) {
                AnnotationMirror top = kv.getKey();
                AnnotationMirror poly = kv.getValue();

                if (top == null && dcType.hasAnnotation(POLYALL)) {
                    // PolyAll qualifier
                    result.put(poly, type.getAnnotations());
                } else if (dcType.hasAnnotation(poly)) {
                    AnnotationMirror typeQual = type
                            .getAnnotationInHierarchy(top);
                    result.put(poly, Collections.singleton(typeQual));
                }
            }

            if (!type.wasRaw() && !dcType.wasRaw()) {
                result = reduce(
                        result,
                        visit(type.getTypeArguments(),
                                dcType.getTypeArguments()));
            }

            return result;
        }

        @Override
        public Map<AnnotationMirror, Set<? extends AnnotationMirror>> visitPrimitive(
                AnnotatedPrimitiveType type, AnnotatedTypeMirror actualType) {
            Map<AnnotationMirror, Set<? extends AnnotationMirror>> result = new HashMap<AnnotationMirror, Set<? extends AnnotationMirror>>();

            for (Map.Entry<AnnotationMirror, AnnotationMirror> kv : polyQuals
                    .entrySet()) {
                AnnotationMirror top = kv.getKey();
                AnnotationMirror poly = kv.getValue();

                if (top == null && actualType.hasAnnotation(POLYALL)) {
                    // PolyAll qualifier
                    result.put(poly, type.getAnnotations());
                } else if (actualType.hasAnnotation(poly)) {
                    AnnotationMirror typeQual = type
                            .getAnnotationInHierarchy(top);
                    result.put(poly, Collections.singleton(typeQual));
                }
            }

            return result;
        }

        @Override
        public Map<AnnotationMirror, Set<? extends AnnotationMirror>> visitNull(
                AnnotatedNullType type, AnnotatedTypeMirror actualType) {

            Map<AnnotationMirror, Set<? extends AnnotationMirror>> result = new HashMap<AnnotationMirror, Set<? extends AnnotationMirror>>();

            for (Map.Entry<AnnotationMirror, AnnotationMirror> kv : polyQuals
                    .entrySet()) {
                AnnotationMirror top = kv.getKey();
                AnnotationMirror poly = kv.getValue();

                if (top == null) {
                    // PolyAll qualifier
                    result.put(poly, type.getAnnotations());
                } else if (actualType.hasAnnotation(poly)) {
                    AnnotationMirror typeQual = type
                            .getAnnotationInHierarchy(top);
                    result.put(poly, Collections.singleton(typeQual));
                }
            }
            if (!result.isEmpty()) {
                return result;
            } else {
                return super.visitNull(type, actualType);
            }
        }

        @Override
        public Map<AnnotationMirror, Set<? extends AnnotationMirror>> visitArray(
                AnnotatedArrayType type, AnnotatedTypeMirror actualType) {
            if (actualType.getKind() == TypeKind.INTERSECTION) {
                // We don't support poly annotations on intersection types
                // See Issue 744
                // https://github.com/typetools/checker-framework/issues/744
                return Collections.emptyMap();
            }

            if (actualType.getKind() == TypeKind.DECLARED)
                return visit(AnnotatedTypes.asSuper(atypeFactory, type, actualType), actualType);
            if (actualType.getKind() == TypeKind.TYPEVAR) {
                if (visited.contains(actualType.getUnderlyingType()))
                    return Collections.emptyMap();
                visited.add(actualType.getUnderlyingType());
                Map<AnnotationMirror, Set<? extends AnnotationMirror>> result = visit(
                        type,
                        ((AnnotatedTypeVariable) actualType).getUpperBound());
                visited.remove(actualType.getUnderlyingType());
                return result;
            }
            if (actualType.getKind() == TypeKind.WILDCARD) {
                if (visited.contains(actualType.getUnderlyingType()))
                    return Collections.emptyMap();
                visited.add(actualType.getUnderlyingType());
                Map<AnnotationMirror, Set<? extends AnnotationMirror>> result = visit(
                        type,
                        ((AnnotatedWildcardType) actualType).getExtendsBound());
                visited.remove(actualType.getUnderlyingType());
                return result;
            }

            assert type.getKind() == actualType.getKind() : actualType;
            AnnotatedArrayType arType = (AnnotatedArrayType) actualType;

            Map<AnnotationMirror, Set<? extends AnnotationMirror>> result = new HashMap<AnnotationMirror, Set<? extends AnnotationMirror>>();

            for (Map.Entry<AnnotationMirror, AnnotationMirror> kv : polyQuals
                    .entrySet()) {
                AnnotationMirror top = kv.getKey();
                AnnotationMirror poly = kv.getValue();

                if (arType.hasAnnotation(poly)) {
                    Set<AnnotationMirror> typeQuals;
                    if (top == null) {
                        // PolyAll qualifier
                        typeQuals = type.getAnnotations();
                    } else {
                        typeQuals = Collections.singleton(type
                                .getAnnotationInHierarchy(top));
                    }
                    result.put(poly, typeQuals);
                }
            }
            result = reduce(result,
                    visit(type.getComponentType(), arType.getComponentType()));
            return result;
        }

        private final Set<TypeMirror> visited = new HashSet<TypeMirror>();

        @Override
        public Map<AnnotationMirror, Set<? extends AnnotationMirror>> visitTypeVariable(
                AnnotatedTypeVariable type, AnnotatedTypeMirror actualType) {

            if (actualType.getKind() == TypeKind.WILDCARD)
                // give up
                return Collections.emptyMap();

            AnnotatedTypeMirror typeSuper = AnnotatedTypes.asSuper(atypeFactory, type, actualType);
            if (typeSuper.getKind() != TypeKind.TYPEVAR)
                return visit(typeSuper, actualType);

            assert typeSuper.getKind() == actualType.getKind() : actualType;
            assert type.getKind() == actualType.getKind() : actualType;
            AnnotatedTypeVariable tvType = (AnnotatedTypeVariable) typeSuper;

            if (visited.contains(actualType.getUnderlyingType()))
                return Collections.emptyMap();
            visited.add(type.getUnderlyingType());
            // a type variable cannot be annotated
            Map<AnnotationMirror, Set<? extends AnnotationMirror>> result = visit(
                    type.getUpperBound(), tvType.getUpperBound());
            visited.remove(type.getUnderlyingType());
            return result;
        }

        @Override
        public Map<AnnotationMirror, Set<? extends AnnotationMirror>> visitWildcard(
                AnnotatedWildcardType type, AnnotatedTypeMirror actualType) {
            AnnotatedTypeMirror typeSuper = AnnotatedTypes.asSuper(atypeFactory, type, actualType);
            if (typeSuper.getKind() != TypeKind.WILDCARD) {
                return visit(typeSuper, actualType);
            }
            // TODO: hack against unbound wildcard introduced by
            // separate compilation. Test against Issue 257 test.
            if (((com.sun.tools.javac.code.Type.WildcardType) typeSuper
                    .getUnderlyingType()).isUnbound()) {
                return Collections.emptyMap();
            }
            assert typeSuper.getKind() == actualType.getKind() ||
            // TODO: actualType might be the capture of a wildcard;
            // better/different check?
                    actualType.getKind() == TypeKind.TYPEVAR : "PolyCollector: mismatched type "
                    + "kinds: " +
                    actualType
                    + " (" +
                    actualType.getKind()
                    + ") and "
                    + typeSuper + " (" + typeSuper.getKind() + ")";
            AnnotatedWildcardType wcType = (AnnotatedWildcardType) typeSuper;

            if (visited.contains(actualType.getUnderlyingType())) {
                return Collections.emptyMap();
            }
            visited.add(type.getUnderlyingType());
            Map<AnnotationMirror, Set<? extends AnnotationMirror>> result;
            if (type.getExtendsBound() != null
                    && wcType.getExtendsBound() != null)
                result = visit(type.getExtendsBound(), wcType.getExtendsBound());
            else if (type.getSuperBound() != null
                    && wcType.getSuperBound() != null)
                result = visit(type.getSuperBound(), wcType.getSuperBound());
            else
                result = new HashMap<AnnotationMirror, Set<? extends AnnotationMirror>>();

            visited.remove(type.getUnderlyingType());
            return result;
        }
    }

}
