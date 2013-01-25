package sparta.checkers;

import checkers.util.test.CheckerParameterized;
import checkers.util.test.CheckerTest;
import checkers.util.test.TestUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.*;

@RunWith(Parameterized.class)
public class GenericFlowPolicyTest extends CheckerTest {

    private final File testDir;
    private final String [] checkerOptionsWPolicy;

    public GenericFlowPolicyTest(final String parentDir, final String [] optionsWithoutPolicyFile) {
        super(FlowChecker.class.getName(), parentDir, optionsWithoutPolicyFile);
        this.testDir = new File(parentDir);
        this.checkerOptionsWPolicy = Arrays.copyOf(optionsWithoutPolicyFile, optionsWithoutPolicyFile.length + 1);
    }

    @Test
    public void run() {
        test(checkerName, checkerOptions, new File(testDir, "WithoutPolicyTest.java"));

        final File policyFile = new File(testDir, "flow.policy");
        checkerOptionsWPolicy[checkerOptionsWPolicy.length -1] =
                "-AflowPolicy=" + policyFile.getAbsolutePath();

        test(checkerName, checkerOptionsWPolicy, new File(testDir, "WithPolicyTest.java"));
    }

    protected static Collection<Object[]> testDirs(final String [] options, final String ... dirNames) {
        final Collection<Object[]> arguments = new ArrayList<Object[]>();
        for (final String dirName : dirNames) {
            final String dir = "tests" + File.separator + dirName;
            arguments.add(new Object[]{dir, options});
        }
        return arguments;
    }
}
