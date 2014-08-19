package sparta.checkers.intents;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import org.checkerframework.dataflow.analysis.FlowExpressions;
import org.checkerframework.dataflow.analysis.FlowExpressions.Receiver;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.MethodAccessNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.TypesUtils;

import sparta.checkers.quals.IntentMap;
import sparta.checkers.quals.IntentMapBottom;
import sparta.checkers.quals.IntentMapNew;
import sparta.checkers.quals.PutExtra;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

public class IntentTransfer extends CFTransfer {

    private IntentAnnotatedTypeFactory factory;

    public IntentTransfer(
            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(analysis);
        factory = (IntentAnnotatedTypeFactory) analysis.getTypeFactory();
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
        // Do not refine assignments where @IntentMapNew is on the RHS.
        if (factory.getAnnotatedType(n.getExpression().getTree()).
                hasAnnotation(IntentMapNew.class)) {
            return new RegularTransferResult<>(null, in.getRegularStore());
        }
        return super.visitAssignment(n, in);
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
            // putExtra method
            if (factory.getDeclAnnotation(method, PutExtra.class) != null) {
                //TODO: Refine receiver expression of a putExtra call here.
            }
        }
        return result;
    }

}