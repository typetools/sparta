package sparta.checkers;

import checkers.types.AnnotatedTypeMirror;
import checkers.util.AnnotationBuilder;
import checkers.util.AnnotationUtils;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.Sources;
import sparta.checkers.quals.Sinks.SPARTA_Permission;
import sparta.checkers.quals.Sources.SPARTA_Permission;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;



import java.util.*;

public class FlowUtil {

    //lazily initialize FLOW_SOURCES_NO_ANY and FLOW_SINKS_NO_ANY to avoid
    //any possible static initialization errors
    private static boolean lazyInit = true;
    private final static Set<SPARTA_Permission> FLOW_SOURCES_NO_ANY = new HashSet<SPARTA_Permission>();
    private final static Set<SPARTA_Permission>   FLOW_SINKS_NO_ANY   = new HashSet<SPARTA_Permission>();

    private static void init() {
        FLOW_SOURCES_NO_ANY.addAll(Arrays.asList(SPARTA_Permission.values()));
        FLOW_SOURCES_NO_ANY.remove(SPARTA_Permission.ANY);

        FLOW_SINKS_NO_ANY.addAll(Arrays.asList(SPARTA_Permission.values()));
        FLOW_SINKS_NO_ANY.remove(SPARTA_Permission.ANY);
    }

    public static Set<SPARTA_Permission> getSourcesNoAny() {
        if(lazyInit) {
            init();
        }
        return FLOW_SOURCES_NO_ANY;
    }

    public static Set<SPARTA_Permission> getSinksNoAny() {
        if(lazyInit) {
            init();
        }
        return FLOW_SINKS_NO_ANY;
    }

    public static List<SPARTA_Permission> getSinks(final AnnotatedTypeMirror type){
	return getSinks(type.getAnnotation(Sinks.class));
    }
    public static List<SPARTA_Permission> getSources(final AnnotatedTypeMirror type){
	return getSources(type.getAnnotation(Sources.class));
    }
    public static List<SPARTA_Permission> getSinks(final AnnotationMirror am) {
	if(am == null){
	    return new ArrayList<SPARTA_Permission>();
	}
        return AnnotationUtils.getElementValueEnumArray(am, "value", SPARTA_Permission.class, true);
    }

    public static List<SPARTA_Permission> getSources(final AnnotationMirror am) {
	if(am == null){
	    return new ArrayList<SPARTA_Permission>();
	}
	
        return AnnotationUtils.getElementValueEnumArray(am, "value", SPARTA_Permission.class, true);
    }

    public static Set<SPARTA_Permission> getSinks(final AnnotationMirror annotationMirror, boolean replaceAny) {
        final Set<SPARTA_Permission> sinkSet =  new HashSet<SPARTA_Permission>(getSinks(annotationMirror));
        if(replaceAny) {
            replaceAnySink(sinkSet, true);
        }

        return sinkSet;
    }
/**
 * Returns the set of flow sources for the given annotation mirror
 * @param annotationMirror if null, then the empty set is returned
 * @param replaceAny
 * @return
 */
    public static Set<SPARTA_Permission> getSources(final AnnotationMirror annotationMirror, boolean replaceAny) {
        final Set<SPARTA_Permission> sourceSet =  new HashSet<SPARTA_Permission>(getSources(annotationMirror));
        if(replaceAny) {
            replaceAnySource(sourceSet, true);
        }

        return sourceSet;
    }

/**
 * REturn the set of flow sinks for the given annoation mirror
 * @param annotationMirror if null, then the empty set is returned
 * @param replaceAny
 * @return
 */
    public static Set<SPARTA_Permission> getSourcesOrEmpty(final AnnotationMirror annotationMirror, boolean replaceAny) {
        if(annotationMirror == null) {
            return new HashSet<SPARTA_Permission>();
        } else {
            return getSources(annotationMirror, replaceAny);
        }
    }

    public static Set<SPARTA_Permission> getSinksOrEmpty(final AnnotationMirror annotationMirror, boolean replaceAny) {
        if(annotationMirror == null) {
            return new HashSet<SPARTA_Permission>();
        } else {
            return getSinks(annotationMirror, replaceAny);
        }
    }

    private static <T, E> AnnotationMirror createAnnoFromEnumArray(final ProcessingEnvironment processingEnv, final Class<T> qualClass, final E[] enumVals ) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv, qualClass.getCanonicalName());
        builder.setValue("value", enumVals );
        return builder.build();
    }

    public static AnnotationMirror createAnnoFromSinks(final ProcessingEnvironment processingEnv, final Set<Sinks.SPARTA_Permission> sinks) {
	if (sinks.contains(SPARTA_Permission.ANY) && sinks.size() > 1) {
	    sinks.clear();
	    sinks.add(SPARTA_Permission.ANY);
	}
        return createAnnoFromEnumArray(processingEnv, Sinks.class, sinks.toArray(new Sinks.SPARTA_Permission[sinks.size()]));
    }

    public static AnnotationMirror createAnnoFromSources(final ProcessingEnvironment processingEnv, Set<Sources.SPARTA_Permission> sources) {
	if (sources.contains(SPARTA_Permission.ANY) && sources.size() > 1) {
	    sources.clear();
	    sources.add(SPARTA_Permission.ANY);
	}
	return createAnnoFromEnumArray(processingEnv, Sources.class, sources.toArray(new Sources.SPARTA_Permission[sources.size()]));
    }

    public static Set<SPARTA_Permission> replaceAnySink(final Set<SPARTA_Permission> sinks, boolean inPlace) {
        return replaceAny(SPARTA_Permission.class,
                SPARTA_Permission.values(),
                sinks,
                inPlace);
    }

    public static Set<SPARTA_Permission> replaceAnySource(final Set<SPARTA_Permission> sources, boolean inPlace) {
        return replaceAny(SPARTA_Permission.class,
                SPARTA_Permission.values(),
                sources,
                inPlace);
    }

    public static Set<SPARTA_Permission> allToAnySource(final Set<SPARTA_Permission> sources, boolean inPlace) {
        return allToAny(SPARTA_Permission.class, SPARTA_Permission.values(), sources, inPlace);
    }

    public static Set<SPARTA_Permission> allToAnySink(final Set<SPARTA_Permission> sinks, boolean inPlace) {
        return allToAny(SPARTA_Permission.class, SPARTA_Permission.values(), sinks, inPlace);
    }

    private static <VALUE extends Enum<VALUE>> Set<VALUE> allToAny( final Class<VALUE> vc,
                                                                    final VALUE [] enumValues,
                                                                    final Set<VALUE> values,
                                                                    final boolean inPlace) {
        final Set<VALUE> retSet = (inPlace) ? values : new HashSet<VALUE>(values);
        if(values.size() == enumValues.length-1) {
            retSet.clear();
            retSet.add(VALUE.valueOf(vc, "ANY"));
        }
        return retSet;
    }

    private static <VALUE extends Enum<VALUE>> Set<VALUE> replaceAny(final Class<VALUE> vc,
                                                                     final VALUE [] enumValues,
                                                                     final Set<VALUE> values,
                                                                     final boolean inPlace ) {
        final VALUE any = VALUE.valueOf(vc, "ANY");
        final Set<VALUE> retSet = (inPlace) ? values : new HashSet<VALUE>(values);
        if(retSet.contains(any)) {
            retSet.addAll(Arrays.asList(enumValues));
            retSet.remove(any);
        }

        return retSet;
    }

}
