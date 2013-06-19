package sparta.checkers;

import checkers.types.AnnotatedTypeMirror;
import checkers.util.AnnotationBuilder;
import checkers.util.AnnotationUtils;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.FlowPermission;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;



import java.util.*;

public class FlowUtil {

    //lazily initialize FLOW_SOURCES_NO_ANY and FLOW_SINKS_NO_ANY to avoid
    //any possible static initialization errors
    private static boolean lazyInit = true;
    private final static Set<FlowPermission> FLOW_SOURCES_NO_ANY = new HashSet<FlowPermission>();
    private final static Set<FlowPermission>   FLOW_SINKS_NO_ANY   = new HashSet<FlowPermission>();

    private static void init() {
        FLOW_SOURCES_NO_ANY.addAll(Arrays.asList(FlowPermission.values()));
        FLOW_SOURCES_NO_ANY.remove(FlowPermission.ANY);

        FLOW_SINKS_NO_ANY.addAll(Arrays.asList(FlowPermission.values()));
        FLOW_SINKS_NO_ANY.remove(FlowPermission.ANY);
    }

    public static Set<FlowPermission> getSourceNoAny() {
        if(lazyInit) {
            init();
        }
        return FLOW_SOURCES_NO_ANY;
    }

    public static Set<FlowPermission> getSinkNoAny() {
        if(lazyInit) {
            init();
        }
        return FLOW_SINKS_NO_ANY;
    }

    public static List<FlowPermission> getSink(final AnnotatedTypeMirror type){
	return getSink(type.getAnnotation(Sink.class));
    }
    public static List<FlowPermission> getSource(final AnnotatedTypeMirror type){
	return getSource(type.getAnnotation(Source.class));
    }
    public static List<FlowPermission> getSink(final AnnotationMirror am) {
	if(am == null){
	    return new ArrayList<FlowPermission>();
	}
        return AnnotationUtils.getElementValueEnumArray(am, "value", FlowPermission.class, true);
    }

    public static List<FlowPermission> getSource(final AnnotationMirror am) {
	if(am == null){
	    return new ArrayList<FlowPermission>();
	}
	
        return AnnotationUtils.getElementValueEnumArray(am, "value", FlowPermission.class, true);
    }

    public static Set<FlowPermission> getSink(final AnnotationMirror annotationMirror, boolean replaceAny) {
        final Set<FlowPermission> sinkSet =  new HashSet<FlowPermission>(getSink(annotationMirror));
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
    public static Set<FlowPermission> getSource(final AnnotationMirror annotationMirror, boolean replaceAny) {
        final Set<FlowPermission> sourceSet =  new HashSet<FlowPermission>(getSource(annotationMirror));
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
    public static Set<FlowPermission> getSourceOrEmpty(final AnnotationMirror annotationMirror, boolean replaceAny) {
        if(annotationMirror == null) {
            return new HashSet<FlowPermission>();
        } else {
            return getSource(annotationMirror, replaceAny);
        }
    }

    public static Set<FlowPermission> getSinkOrEmpty(final AnnotationMirror annotationMirror, boolean replaceAny) {
        if(annotationMirror == null) {
            return new HashSet<FlowPermission>();
        } else {
            return getSink(annotationMirror, replaceAny);
        }
    }

    private static <T, E> AnnotationMirror createAnnoFromEnumArray(final ProcessingEnvironment processingEnv, final Class<T> qualClass, final E[] enumVals ) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv, qualClass.getCanonicalName());
        builder.setValue("value", enumVals );
        return builder.build();
    }

    public static AnnotationMirror createAnnoFromSink(final ProcessingEnvironment processingEnv, final Set<FlowPermission> sinks) {
	if (sinks.contains(FlowPermission.ANY) && sinks.size() > 1) {
	    sinks.clear();
	    sinks.add(FlowPermission.ANY);
	}
        return createAnnoFromEnumArray(processingEnv, Sink.class, sinks.toArray(new FlowPermission[sinks.size()]));
    }

    public static AnnotationMirror createAnnoFromSource(final ProcessingEnvironment processingEnv, Set<FlowPermission> sources) {
	if (sources.contains(FlowPermission.ANY) && sources.size() > 1) {
	    sources.clear();
	    sources.add(FlowPermission.ANY);
	}
	return createAnnoFromEnumArray(processingEnv, Source.class, sources.toArray(new FlowPermission[sources.size()]));
    }

    public static Set<FlowPermission> replaceAnySink(final Set<FlowPermission> sinks, boolean inPlace) {
        return replaceAny(FlowPermission.class,
                FlowPermission.values(),
                sinks,
                inPlace);
    }

    public static Set<FlowPermission> replaceAnySource(final Set<FlowPermission> sources, boolean inPlace) {
        return replaceAny(FlowPermission.class,
                FlowPermission.values(),
                sources,
                inPlace);
    }

    public static Set<FlowPermission> allToAnySource(final Set<FlowPermission> sources, boolean inPlace) {
        return allToAny(FlowPermission.class, FlowPermission.values(), sources, inPlace);
    }

    public static Set<FlowPermission> allToAnySink(final Set<FlowPermission> sinks, boolean inPlace) {
        return allToAny(FlowPermission.class, FlowPermission.values(), sinks, inPlace);
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

	public static boolean isTop(AnnotatedTypeMirror atm) {
		List<FlowPermission> sources = getSource(atm);
		List<FlowPermission> sinks = getSink(atm);
		return sources.contains(FlowPermission.ANY) && sinks.isEmpty();		
	}

}
