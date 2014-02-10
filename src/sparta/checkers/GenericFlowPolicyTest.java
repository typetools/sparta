package sparta.checkers;

import checkers.util.test.CheckerTest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GenericFlowPolicyTest extends CheckerTest {

    private final File testDir;
    private final List<String> checkerOptionsWPolicy;

    public GenericFlowPolicyTest(final String parentDir, final String[] optionsWithoutPolicyFile) {
        super(FlowChecker.class, parentDir, optionsWithoutPolicyFile);
        this.testDir = new File(parentDir);
        this.checkerOptionsWPolicy = new ArrayList<String>(Arrays.asList(optionsWithoutPolicyFile));
    }

    @Test
    public void run() {
        test(checkerName, checkerOptions, new File(testDir, "WithoutPolicyTest.java"));

        final File policyFile = new File(testDir, "flow.policy");
        checkerOptionsWPolicy.add("-AflowPolicy=" + policyFile.getAbsolutePath());

        test(checkerName, checkerOptionsWPolicy, new File(testDir, "WithPolicyTest.java"));
    }

    protected static Collection<Object[]> testDirs(final String[] options, final String... dirNames) {
        final Collection<Object[]> arguments = new ArrayList<Object[]>();
        for (final String dirName : dirNames) {
            final String dir = "tests" + File.separator + dirName;
            arguments.add(new Object[] { dir, options });
        }
        return arguments;
    }
}
