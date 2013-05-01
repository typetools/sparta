package sparta.checkers;

import checkers.types.AnnotatedTypeMirror;
import checkers.util.AnnotationBuilder;
import checkers.util.AnnotationUtils;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.Sources;
import sparta.checkers.quals.Sinks.FlowSink;
import sparta.checkers.quals.Sources.FlowSource;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;



import java.util.*;

public class FlowUtil {

    //lazily initialize FLOW_SOURCES_NO_ANY and FLOW_SINKS_NO_ANY to avoid
    //any possible static initialization errors
    private static boolean lazyInit = true;
    private final static Set<FlowSource> FLOW_SOURCES_NO_ANY = new HashSet<FlowSource>();
    private final static Set<FlowSink>   FLOW_SINKS_NO_ANY   = new HashSet<FlowSink>();

    private static void init() {
        FLOW_SOURCES_NO_ANY.addAll(Arrays.asList(FlowSource.values()));
        FLOW_SOURCES_NO_ANY.remove(FlowSource.ANY);

        FLOW_SINKS_NO_ANY.addAll(Arrays.asList(FlowSink.values()));
        FLOW_SINKS_NO_ANY.remove(FlowSink.ANY);
    }

    public static Set<FlowSource> getSourcesNoAny() {
        if(lazyInit) {
            init();
        }
        return FLOW_SOURCES_NO_ANY;
    }

    public static Set<FlowSink> getSinksNoAny() {
        if(lazyInit) {
            init();
        }
        return FLOW_SINKS_NO_ANY;
    }

    public static List<FlowSink> getSinks(final AnnotatedTypeMirror type){
	return getSinks(type.getAnnotation(Sinks.class));
    }
    public static List<FlowSource> getSources(final AnnotatedTypeMirror type){
	return getSources(type.getAnnotation(Sources.class));
    }
    public static List<FlowSink> getSinks(final AnnotationMirror am) {
	if(am == null){
	    return new ArrayList<FlowSink>();
	}
        return AnnotationUtils.getElementValueEnumArray(am, "value", FlowSink.class, true);
    }

    public static List<FlowSource> getSources(final AnnotationMirror am) {
	if(am == null){
	    return new ArrayList<FlowSource>();
	}
	
        return AnnotationUtils.getElementValueEnumArray(am, "value", FlowSource.class, true);
    }

    public static Set<FlowSink> getSinks(final AnnotationMirror annotationMirror, boolean replaceAny) {
        final Set<FlowSink> sinkSet =  new HashSet<FlowSink>(getSinks(annotationMirror));
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
    public static Set<FlowSource> getSources(final AnnotationMirror annotationMirror, boolean replaceAny) {
        final Set<FlowSource> sourceSet =  new HashSet<FlowSource>(getSources(annotationMirror));
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
    public static Set<FlowSource> getSourcesOrEmpty(final AnnotationMirror annotationMirror, boolean replaceAny) {
        if(annotationMirror == null) {
            return new HashSet<FlowSource>();
        } else {
            return getSources(annotationMirror, replaceAny);
        }
    }

    public static Set<FlowSink> getSinksOrEmpty(final AnnotationMirror annotationMirror, boolean replaceAny) {
        if(annotationMirror == null) {
            return new HashSet<FlowSink>();
        } else {
            return getSinks(annotationMirror, replaceAny);
        }
    }

    private static <T, E> AnnotationMirror createAnnoFromEnumArray(final ProcessingEnvironment processingEnv, final Class<T> qualClass, final E[] enumVals ) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv, qualClass.getCanonicalName());
        builder.setValue("value", enumVals );
        return builder.build();
    }

    public static AnnotationMirror createAnnoFromSinks(final ProcessingEnvironment processingEnv, final Set<Sinks.FlowSink> sinks) {
	if (sinks.contains(FlowSink.ANY) && sinks.size() > 1) {
	    sinks.clear();
	    sinks.add(FlowSink.ANY);
	}
        return createAnnoFromEnumArray(processingEnv, Sinks.class, sinks.toArray(new Sinks.FlowSink[sinks.size()]));
    }

    public static AnnotationMirror createAnnoFromSources(final ProcessingEnvironment processingEnv, Set<Sources.FlowSource> sources) {
	if (sources.contains(FlowSource.ANY) && sources.size() > 1) {
	    sources.clear();
	    sources.add(FlowSource.ANY);
	}
	return createAnnoFromEnumArray(processingEnv, Sources.class, sources.toArray(new Sources.FlowSource[sources.size()]));
    }

    public static Set<FlowSink> replaceAnySink(final Set<FlowSink> sinks, boolean inPlace) {
        return replaceAny(FlowSink.class,
                FlowSink.values(),
                sinks,
                inPlace);
    }

    public static Set<FlowSource> replaceAnySource(final Set<FlowSource> sources, boolean inPlace) {
        return replaceAny(FlowSource.class,
                FlowSource.values(),
                sources,
                inPlace);
    }

    public static Set<FlowSource> allToAnySource(final Set<FlowSource> sources, boolean inPlace) {
        return allToAny(FlowSource.class, FlowSource.values(), sources, inPlace);
    }

    public static Set<FlowSink> allToAnySink(final Set<FlowSink> sinks, boolean inPlace) {
        return allToAny(FlowSink.class, FlowSink.values(), sinks, inPlace);
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
