package sparta.checkers.intents;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import sparta.checkers.Flow;
import sparta.checkers.FlowAnnotationFormatter;
import sparta.checkers.quals.IntentMap;

/**
 * Formats @IntentMap(extras=@Extra(key="k1", source=PERM, sink=PERM) as
 * @IntentMap([k1: PERM->PERM])
 * @author smillst
 *
 */
public class IntentAnnotationFormatter extends FlowAnnotationFormatter {
    @Override
    public String formatAnnotationMirror(AnnotationMirror anno) {
        StringBuffer buff = new StringBuffer();
        if (AnnotationUtils.areSameByClass(anno, IntentMap.class)) {
            List<AnnotationMirror> extras = IntentUtils.getIExtras(anno);
            buff.append("@IntentMap(");
            boolean first = true;
            for (AnnotationMirror extra : extras) {
                if (!first) {
                    buff.append(",");
                } else {
                    first = false;
                }

                buff.append(extraToString(extra));
            }
            buff.append(") ");
            return buff.toString();
        }
        return super.formatAnnotationMirror(anno);
    }

    private Object extraToString(AnnotationMirror extra) {
        String key = IntentUtils.getKeyName(extra);
        Flow flow = new Flow(IntentUtils.getSourcesPFP(extra),
                IntentUtils.getSinksPFP(extra));
        return "["+key+": "+flow.toString()+"]";
    }
}
