package sparta.checkers;

import checkers.types.AnnotatedTypeMirror;
import checkers.util.AnnotationBuilder;
import checkers.util.AnnotationUtils;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.Sources;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.SpartaPermission;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;



import java.util.*;

public class FlowUtil {

    //lazily initialize FLOW_SOURCES_NO_ANY and FLOW_SINKS_NO_ANY to avoid
    //any possible static initialization errors
    private static boolean lazyInit = true;
    private final static Set<SpartaPermission> FLOW_SOURCES_NO_ANY = new HashSet<SpartaPermission>();
    private final static Set<SpartaPermission>   FLOW_SINKS_NO_ANY   = new HashSet<SpartaPermission>();

    private static void init() {
        FLOW_SOURCES_NO_ANY.addAll(Arrays.asList(SpartaPermission.values()));
        FLOW_SOURCES_NO_ANY.remove(SpartaPermission.ANY);

        FLOW_SINKS_NO_ANY.addAll(Arrays.asList(SpartaPermission.values()));
        FLOW_SINKS_NO_ANY.remove(SpartaPermission.ANY);
    }

    public static Set<SpartaPermission> getSourcesNoAny() {
        if(lazyInit) {
            init();
        }
        return FLOW_SOURCES_NO_ANY;
    }

    public static Set<SpartaPermission> getSinksNoAny() {
        if(lazyInit) {
            init();
        }
        return FLOW_SINKS_NO_ANY;
    }

    public static List<SpartaPermission> getSinks(final AnnotatedTypeMirror type){
	return getSinks(type.getAnnotation(Sinks.class));
    }
    public static List<SpartaPermission> getSources(final AnnotatedTypeMirror type){
	return getSources(type.getAnnotation(Sources.class));
    }
    public static List<SpartaPermission> getSinks(final AnnotationMirror am) {
	if(am == null){
	    return new ArrayList<SpartaPermission>();
	}
        return AnnotationUtils.getElementValueEnumArray(am, "value", SpartaPermission.class, true);
    }

    public static List<SpartaPermission> getSources(final AnnotationMirror am) {
	if(am == null){
	    return new ArrayList<SpartaPermission>();
	}
	
        return AnnotationUtils.getElementValueEnumArray(am, "value", SpartaPermission.class, true);
    }

    public static Set<SpartaPermission> getSinks(final AnnotationMirror annotationMirror, boolean replaceAny) {
        final Set<SpartaPermission> sinkSet =  new HashSet<SpartaPermission>(getSinks(annotationMirror));
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
    public static Set<SpartaPermission> getSources(final AnnotationMirror annotationMirror, boolean replaceAny) {
        final Set<SpartaPermission> sourceSet =  new HashSet<SpartaPermission>(getSources(annotationMirror));
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
    public static Set<SpartaPermission> getSourcesOrEmpty(final AnnotationMirror annotationMirror, boolean replaceAny) {
        if(annotationMirror == null) {
            return new HashSet<SpartaPermission>();
        } else {
            return getSources(annotationMirror, replaceAny);
        }
    }

    public static Set<SpartaPermission> getSinksOrEmpty(final AnnotationMirror annotationMirror, boolean replaceAny) {
        if(annotationMirror == null) {
            return new HashSet<SpartaPermission>();
        } else {
            return getSinks(annotationMirror, replaceAny);
        }
    }

    private static <T, E> AnnotationMirror createAnnoFromEnumArray(final ProcessingEnvironment processingEnv, final Class<T> qualClass, final E[] enumVals ) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv, qualClass.getCanonicalName());
        builder.setValue("value", enumVals );
        return builder.build();
    }

    public static AnnotationMirror createAnnoFromSinks(final ProcessingEnvironment processingEnv, final Set<SpartaPermission> sinks) {
	if (sinks.contains(SpartaPermission.ANY) && sinks.size() > 1) {
	    sinks.clear();
	    sinks.add(SpartaPermission.ANY);
	}
        return createAnnoFromEnumArray(processingEnv, Sinks.class, sinks.toArray(new SpartaPermission[sinks.size()]));
    }

    public static AnnotationMirror createAnnoFromSources(final ProcessingEnvironment processingEnv, Set<SpartaPermission> sources) {
	if (sources.contains(SpartaPermission.ANY) && sources.size() > 1) {
	    sources.clear();
	    sources.add(SpartaPermission.ANY);
	}
	return createAnnoFromEnumArray(processingEnv, Sources.class, sources.toArray(new SpartaPermission[sources.size()]));
    }

    public static Set<SpartaPermission> replaceAnySink(final Set<SpartaPermission> sinks, boolean inPlace) {
        return replaceAny(SpartaPermission.class,
                SpartaPermission.values(),
                sinks,
                inPlace);
    }

    public static Set<SpartaPermission> replaceAnySource(final Set<SpartaPermission> sources, boolean inPlace) {
        return replaceAny(SpartaPermission.class,
                SpartaPermission.values(),
                sources,
                inPlace);
    }

    public static Set<SpartaPermission> allToAnySource(final Set<SpartaPermission> sources, boolean inPlace) {
        return allToAny(SpartaPermission.class, SpartaPermission.values(), sources, inPlace);
    }

    public static Set<SpartaPermission> allToAnySink(final Set<SpartaPermission> sinks, boolean inPlace) {
        return allToAny(SpartaPermission.class, SpartaPermission.values(), sinks, inPlace);
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
