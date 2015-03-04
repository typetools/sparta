package sparta.checkers;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.Collection;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.framework.util.DefaultAnnotationFormatter;

import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

/**
 * Formats <code> @Source(PERM) @Sink(PERM) </code> as <code> @Flow(PERM->PERM) </code> 
 * @author smillst
 *
 */
public class FlowAnnotationFormatter extends DefaultAnnotationFormatter {
    @Override
    public String formatAnnotationString(
            Collection<? extends AnnotationMirror> annos, boolean printInvisible) {
        AnnotationMirror source = null;
        AnnotationMirror sink = null;
        Set<AnnotationMirror> notFlowAnnos = AnnotationUtils
                .createAnnotationSet();
        for (AnnotationMirror anno : annos) {
            if (AnnotationUtils.areSameByClass(anno, Source.class))
                source = anno;
            else if (AnnotationUtils.areSameByClass(anno, Sink.class))
                sink = anno;
            else
                notFlowAnnos.add(anno);
        }
        StringBuffer buff = new StringBuffer();
        if (source != null && sink != null) {
            Flow flow = new Flow(Flow.getSources(source), Flow.getSources(sink));
            buff.append("@Flow(").append(flow.toString()).append(") ");
            for(AnnotationMirror anno : notFlowAnnos){
                buff.append(formatAnnotationMirror(anno));
            }
            return buff.toString();
        }
        return super.formatAnnotationString(annos, printInvisible);
    }
}
