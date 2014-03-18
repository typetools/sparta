package sparta.checkers.intents;

import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.qual.PolyAll;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.qual.TypeQualifiers;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.source.SupportedLintOptions;

import java.util.List;

import javax.annotation.processing.SupportedOptions;

import sparta.checkers.FlowChecker;
import sparta.checkers.FlowPolicy;
import sparta.checkers.FlowVisitor;
import sparta.checkers.quals.Extra;
import sparta.checkers.quals.IntentMap;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

@TypeQualifiers({ Source.class, Sink.class, PolySource.class, PolySink.class, PolyAll.class,
        IntentMap.class, Extra.class })
@StubFiles({"information_flow.astub","receive-send-intent.astub"})
@SupportedOptions({ FlowPolicy.POLICY_FILE_OPTION, ComponentMap.COMPONENT_MAP_FILE_OPTION,
        FlowChecker.MSG_FILTER_OPTION, FlowChecker.IGNORE_NOT_REVIEWED, FlowVisitor.CHECK_CONDITIONALS_OPTION })
@SupportedLintOptions({ FlowPolicy.STRICT_CONDITIONALS_OPTION })
public class IntentChecker extends FlowChecker {
//    @Override
//    public void report(Result r, Object src) {
//        List<String> messageKeys = r.getMessageKeys();
//        if (messageKeys.contains("intent.key.notfound")
//                || messageKeys.contains("intent.type.incompatible")
//                || messageKeys.contains("send.intent")
//                || messageKeys.contains("getintent.not.found")
//                || messageKeys.contains("intent.receiver.notfound")
//                || messageKeys.contains("intent.receiveintent.notfound")){
//            super.report(r, src);
//        }
//    }

    
 
    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        try {
            return new IntentVisitor(this);
        } catch (Exception e) {
            //The SourceVisitor cuts off the stack trace, 
            //so print it here and the throw again.
            e.printStackTrace();
            throw e;
        }
    }

}
