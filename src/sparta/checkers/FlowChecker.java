package sparta.checkers;

/*>>>
import org.checkerframework.checker.compilermsgs.qual.CompilerMessageKey;
*/

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.framework.qual.PolyAll;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.qual.TypeQualifiers;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.SupportedOptions;

import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySinkR;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.PolySourceR;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

@StubFiles("information_flow.astub")
@SupportedOptions({ FlowPolicy.POLICY_FILE_OPTION, FlowChecker.MSG_FILTER_OPTION,
        FlowVisitor.CHECK_CONDITIONALS_OPTION, FlowChecker.PRETTY_PRINT_OPTION })

public class FlowChecker extends BaseTypeChecker {
    public static final String SPARTA_OUTPUT_DIR = System.getProperty("user.dir")+File.separator+"sparta-out"+File.separator;
    public static final String MSG_FILTER_OPTION = "msgFilter";
    public static final String PRETTY_PRINT_OPTION = "prettyPrint";

    protected Set<String> unfilteredMessages;

    public FlowChecker() {
        super();
    }
    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new FlowVisitor(this);
    }
    @Override
    protected LinkedHashSet<Class<? extends BaseTypeChecker>> getImmediateSubcheckerClasses() {
        LinkedHashSet<Class<? extends BaseTypeChecker>> flowSubcheckers = super.getImmediateSubcheckerClasses();
        flowSubcheckers.add(ValueChecker.class);
        return flowSubcheckers;
    }

    @Override
    public void initChecker() {
        super.initChecker();

        String unfilteredStr = getOption(MSG_FILTER_OPTION);
        if (unfilteredStr == null) {
            unfilteredMessages = null;
        } else {
            final String[] unfilteredMsgs = unfilteredStr.split(":");
            unfilteredMessages = new HashSet<String>();
            for (final String unfilteredMsg : unfilteredMsgs) {
                if (!unfilteredMsg.trim().isEmpty()) {
                    unfilteredMessages.add(unfilteredMsg.trim());
                }
            }

            if (unfilteredMessages.isEmpty()) {
                unfilteredMessages = null;
            }
        }
    }

    @Override
    public void typeProcessingOver() {
        File outputDir = new File(SPARTA_OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        if (outputDir.exists() && outputDir.isDirectory()) {
            FlowAnnotatedTypeFactory factory = ((FlowVisitor) visitor)
                    .getTypeFactory();
            factory.flowAnalizer.printAllFlows();
            factory.flowAnalizer.printForbiddenFlows();
            factory.flowAnalizer.printAllFlowsWithSourceLocation();
            factory.flowAnalizer.printIntentFlowsByComponent();
        }
    }
}
