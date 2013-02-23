package sparta.checkers;

import checkers.util.AnnotationBuilder;
import checkers.util.AnnotationUtils;
import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.FlowSources;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import static sparta.checkers.quals.FlowSources.FlowSource;

import static sparta.checkers.quals.FlowSinks.FlowSink;

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

    public static Set<FlowSource> getFlowSourcesNoAny() {
        if(lazyInit) {
            init();
        }
        return FLOW_SOURCES_NO_ANY;
    }

    public static Set<FlowSink> getFlowSinksNoAny() {
        if(lazyInit) {
            init();
        }
        return FLOW_SINKS_NO_ANY;
    }

    public static List<FlowSink> getFlowSinks(final AnnotationMirror am) {
        return AnnotationUtils.getElementValueEnumArray(am, "value", FlowSink.class, true);
    }

    public static List<FlowSource> getFlowSources(final AnnotationMirror am) {
        return AnnotationUtils.getElementValueEnumArray(am, "value", FlowSource.class, true);
    }

    public static Set<FlowSink> getFlowSinks(final AnnotationMirror annotationMirror, boolean replaceAny) {
        final Set<FlowSink> sinkSet =  new HashSet<FlowSink>(getFlowSinks(annotationMirror));
        if(replaceAny) {
            replaceAnySink(sinkSet, true);
        }

        return sinkSet;
    }

    public static Set<FlowSource> getFlowSources(final AnnotationMirror annotationMirror, boolean replaceAny) {
        final Set<FlowSource> sourceSet =  new HashSet<FlowSource>(getFlowSources(annotationMirror));
        if(replaceAny) {
            replaceAnySource(sourceSet, true);
        }

        return sourceSet;
    }


    public static Set<FlowSource> getFlowSourcesOrEmpty(final AnnotationMirror annotationMirror, boolean replaceAny) {
        if(annotationMirror == null) {
            return new HashSet<FlowSource>();
        } else {
            return getFlowSources(annotationMirror, replaceAny);
        }
    }

    public static Set<FlowSink> getFlowSinksOrEmpty(final AnnotationMirror annotationMirror, boolean replaceAny) {
        if(annotationMirror == null) {
            return new HashSet<FlowSink>();
        } else {
            return getFlowSinks(annotationMirror, replaceAny);
        }
    }

    private static <T, E> AnnotationMirror createAnnoFromEnumArray(final ProcessingEnvironment processingEnv, final Class<T> qualClass, final E[] enumVals ) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv, qualClass.getCanonicalName());
        builder.setValue("value", enumVals );
        return builder.build();
    }

    public static AnnotationMirror createAnnoFromSinks(final ProcessingEnvironment processingEnv, final Set<FlowSinks.FlowSink> sinks) {
        return createAnnoFromEnumArray(processingEnv, FlowSinks.class, sinks.toArray(new FlowSinks.FlowSink[sinks.size()]));
    }

    public static AnnotationMirror createAnnoFromSources(final ProcessingEnvironment processingEnv, final Set<FlowSources.FlowSource> sources) {
        return createAnnoFromEnumArray(processingEnv, FlowSources.class, sources.toArray(new FlowSources.FlowSource[sources.size()]));
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
        if(values.size() == enumValues.length) {
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
