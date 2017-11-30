package sparta.checkers.intents;

import java.util.LinkedHashSet;

import javax.annotation.processing.SupportedOptions;

import org.checkerframework.common.aliasing.AliasingChecker;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.framework.qual.StubFiles;

import sparta.checkers.FlowChecker;
import sparta.checkers.FlowPolicy;
import sparta.checkers.FlowVisitor;

@StubFiles({"information_flow.astub","receive-send-intent.astub","intent-map.astub","put-get-extra.astub"})
@SupportedOptions({ FlowPolicy.POLICY_FILE_OPTION, ComponentMap.COMPONENT_MAP_FILE_OPTION,
        FlowChecker.MSG_FILTER_OPTION, FlowVisitor.CHECK_CONDITIONALS_OPTION })
public class IntentChecker extends FlowChecker {
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

    @Override
    protected LinkedHashSet<Class<? extends BaseTypeChecker>> getImmediateSubcheckerClasses() {
        LinkedHashSet<Class<? extends BaseTypeChecker>> subCheckers = super.
                getImmediateSubcheckerClasses();
        subCheckers.add(ValueChecker.class);
        subCheckers.add(AliasingChecker.class);
        return subCheckers;
    }
}
