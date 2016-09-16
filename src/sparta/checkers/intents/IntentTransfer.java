package sparta.checkers.intents;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import org.checkerframework.common.aliasing.qual.Unique;
import org.checkerframework.dataflow.analysis.FlowExpressions;
import org.checkerframework.dataflow.analysis.FlowExpressions.Receiver;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.MethodAccessNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.ObjectCreationNode;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.TypesUtils;

import sparta.checkers.Flow;
import sparta.checkers.quals.IntentMap;
import sparta.checkers.quals.IntentMapBottom;
import sparta.checkers.quals.IntentMapNew;
import sparta.checkers.quals.PutExtra;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

public class IntentTransfer extends CFTransfer {

    private IntentAnnotatedTypeFactory intentATF;
    private AnnotatedTypeFactory aliasingATF;

    public IntentTransfer(
            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(analysis);
        intentATF = (IntentAnnotatedTypeFactory) analysis.getTypeFactory();
        aliasingATF = intentATF.getAliasingTypeFactory();
    }

    /**
     * This method overrides super so that variables are not refined in
     * conditionals see test case in flow/Conditions.java
     */
    @Override
    protected TransferResult<CFValue, CFStore> strengthenAnnotationOfEqualTo(
            TransferResult<CFValue, CFStore> res, Node firstNode,
            Node secondNode, CFValue firstValue, CFValue secondValue,
            boolean notEqualTo) {
        return res;
    }

    @Override
    public TransferResult<CFValue, CFStore> visitAssignment(AssignmentNode n,
            TransferInput<CFValue, CFStore> in) {
        if (intentATF.getAnnotatedType(n.getExpression().getTree()).
                hasAnnotation(IntentMapNew.class)) {
            // Do not refine assignments where @IntentMapNew is on the RHS.
            return new RegularTransferResult<>(null, in.getRegularStore());
        } else {
            return super.visitAssignment(n, in);
        }
    }

    @Override
    public TransferResult<CFValue, CFStore> visitMethodInvocation(
            MethodInvocationNode n, TransferInput<CFValue, CFStore> in) {
        TransferResult<CFValue, CFStore> result = super.visitMethodInvocation(
                n, in);
        // Refine result
        MethodAccessNode target = n.getTarget();
        ExecutableElement method = target.getMethod();
        Node receiverNode = target.getReceiver();
        // Refine if the receiver is an Intent or Bundle.
        if (TypesUtils.isDeclaredOfName(receiverNode.getType(),
                "android.content.Intent")
                || TypesUtils.isDeclaredOfName(receiverNode.getType(),
                        "android.os.Bundle")) {
            // putExtra method and receiver intent has type @Unique
            if (intentATF.getDeclAnnotation(method, PutExtra.class) != null
                    && aliasingATF.getAnnotatedType(receiverNode.getTree())
                            .hasAnnotation(Unique.class)) {
                // Never refine IntentMapBottom.
                if (intentATF.getAnnotationMirror(receiverNode.getTree(),
                        IntentMapBottom.class) != null) {
                    return result;
                }

                // Retrieving current IntentMap type
                AnnotatedTypeMirror intentATM = intentATF
                        .getAnnotatedType(receiverNode.getTree());
                AnnotationMirror iMap = intentATM
                        .getAnnotation(IntentMap.class);

                // Dealing with @IntentMapNew receiver
                if (iMap == null) {
                    iMap = intentATF.createTopIntentMap();
                }

                iMap = refineIntentMap(n, intentATM, iMap);

                // Refining receiver with new IntentMap.
                Receiver receiver = FlowExpressions.internalReprOf(intentATF,
                        receiverNode);
                CFStore store = result.getRegularStore();
                // Removing from store if it was already there.
                store.clearValue(receiver);
                store.insertValue(receiver, iMap);

                // Refining result with new IntentMap.
                CFValue newResultValue = result.getResultValue();
                if (newResultValue != null) {
                    // When the method return type is void, for example, in Bundle.putString,
                    // newResultValue == null, and we want to avoid a nullpointer below.
                    newResultValue = analysis.createSingleAnnotationValue(
                            iMap, newResultValue.getUnderlyingType());
                }

                return new RegularTransferResult<>(newResultValue, store);

            }
        }
        return result;
    }

    private AnnotationMirror refineIntentMap(MethodInvocationNode n,
            AnnotatedTypeMirror intentATM, AnnotationMirror iMap) {
        // Getting @Source and @Sink from the value parameter of a putExtra.
        Node nodeValue = n.getArgument(1);
        AnnotationMirror source = intentATF.getAnnotationMirror(
                nodeValue.getTree(), Source.class);
        AnnotationMirror sink = intentATF.getAnnotationMirror(
                nodeValue.getTree(), Sink.class);

        List<String> keys = intentATF.getKeysFromPutExtraOrGetExtraCall(n
                .getTree());
        for (String key : keys) {
            AnnotationMirror oldExtra = IntentUtils.getIExtra(intentATM, key);
            AnnotationMirror declaredExtra = IntentUtils.getDeclaredTypeIExtra(
                    intentATM, key);
            if (declaredExtra == null) {
                // Only refine Extra if is isn't in the declared type.
                if (oldExtra != null) {
                    // Remove old @Extra with same key if it existed already
                    // and wasn't in the declared type.
                    iMap = IntentUtils.getNewIMapWithExtra(iMap, key,
                            Flow.getSources(source), Flow.getSinks(sink),
                            intentATF.getProcessingEnv());
                } else {
                    // Add new key to the refined type.
                    AnnotationMirror iExtra = IntentUtils.createIExtraAnno(key,
                            Flow.getSources(source), Flow.getSinks(sink),
                            intentATF.getProcessingEnv());
                    iMap = IntentUtils.addIExtraInIntentExtras(iMap, iExtra,
                            intentATF.getProcessingEnv());
                }
            }
        }
        return iMap;
    }

}