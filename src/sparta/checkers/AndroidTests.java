package sparta.checkers;



import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.checkerframework.framework.test.ParameterizedCheckerTest;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized.Parameters;

import sparta.checkers.intents.IntentChecker;

/**
 * JUnit tests for the SPARTA Checkers.
 */
public class AndroidTests {
    public static void main(String[] args) {
        org.junit.runner.JUnitCore jc = new org.junit.runner.JUnitCore();
        Result run = jc.run(AndroidFenumCheckerTests.class);

        if (run.wasSuccessful()) {
            System.out.println("Run was successful with " + run.getRunCount() + " test(s)!");
        } else {
            System.out.println("Run had " + run.getFailureCount() + " failure(s) out of "
                    + run.getRunCount() + " run(s)!");

            for (Failure f : run.getFailures()) {
                System.out.println(f.toString());
            }
        }
    }

    public static class AndroidFenumCheckerTests extends ParameterizedCheckerTest {
        public AndroidFenumCheckerTests(File testFile) {
            super(testFile, AndroidFenumChecker.class, "sparta.checkers", "-Anomsgtext", "-AprintErrorStack");
//            super(testFile, AndroidFenumChecker.class, "sparta.checkers", "-Astubs=apiusage.astub");

        }

        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("fenums");
        }
    }

    public static class AndroidPermissionsCheckerTests extends ParameterizedCheckerTest {
        public AndroidPermissionsCheckerTests(File testFile) {
            super(testFile, PermissionsChecker.class, "sparta.checkers", "-Anomsgtext");
        }

        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("permissions");
        }
    }

    public static class AndroidReportCheckerTests extends ParameterizedCheckerTest {
        public AndroidReportCheckerTests(File testFile) {
            super(testFile, AndroidReportChecker.class, "sparta.checkers", "-Anomsgtext",
                    "-Astubs=apiusage.astub:suspicious.astub");
        }

        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("report");
        }
    }

    public static class IntentCheckerTests extends ParameterizedCheckerTest {
        public IntentCheckerTests(File testFile) {
            super(testFile, IntentChecker.class, "sparta.checkers", "-Anomsgtext");
            // Uncomment the line below to see the full errors in the JUnit tests
//             super(testFile, IntentChecker.class, "sparta.checkers");
        }

        private IntentCheckerTests(File testFile, String... checkerOptions) {
            super(testFile, IntentChecker.class, "sparta.checkers", checkerOptions);
        }

        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("intent");
        }

        @Override
        protected void test(final File testFile) {
            final File flowPolicyFile = getFlowPolicy(testFile);
            final File componentMapFile = getComponentMap(testFile);
            final List<String> optionsWithPf = new ArrayList<>(checkerOptions);

            if (flowPolicyFile.exists() && componentMapFile.exists()) {
                optionsWithPf.add("-AflowPolicy=" + flowPolicyFile.getAbsolutePath());
                if(componentMapFile.exists()) {
                    optionsWithPf.add("-AcomponentMap=" + componentMapFile.getAbsolutePath());
                }
                optionsWithPf.add("-AprintErrorStack");
                // AprintErrorStack
            } else if (flowPolicyFile.exists()) {
                optionsWithPf.add("-AflowPolicy=" + flowPolicyFile.getAbsolutePath());
                optionsWithPf.add("-AprintErrorStack");
                // AprintErrorStack
            } else {
                optionsWithPf.add("-AprintErrorStack");
            }

            // System.out.println("OPTIONS:\n" + join(optionsWithPf, " "));
            test(checkerName, optionsWithPf, testFile);
        }

        protected File getFile(final File javaFile, final String extension) {
            final String path = javaFile.getAbsolutePath();
            if (!path.endsWith(".java")) {
                throw new RuntimeException("Cannot recognize java file "
                        + javaFile.getAbsolutePath());
            } else {
                return new File(javaFile.getAbsolutePath().substring(0, path.length() - 5)
                        + extension);
            }
        }

        protected File getFlowPolicy(final File javaFile) {
            return getFile(javaFile, "Flowpolicy");
        }

        protected File getComponentMap(final File javaFile) {
            return getFile(javaFile, "Componentmap");
        }
    }

    public static class ParameterizedFlowCheckerTests extends FlowCheckerTests {

        public ParameterizedFlowCheckerTests(File testFile) {
            super(testFile);
        }

        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("parameterized");
        }
    }
    
    public static class FlowCheckerTests extends ParameterizedCheckerTest {
        public FlowCheckerTests(File testFile) {
            super(testFile, FlowChecker.class, "sparta.checkers", "-Anomsgtext");
            // Uncomment the line below to see the full errors in the JUnit tests
            // super(testFile, FlowChecker.class, "sparta.checkers");
        }

        private FlowCheckerTests(File testFile, String... checkerOptions) {
            super(testFile, FlowChecker.class, "sparta.checkers", checkerOptions);
        }

        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("flow");
        }

        @Override
        protected void test(final File testFile) {
            final File flowPolicyFile = getFlowPolicy(testFile);
            final List<String> optionsWithPf = new ArrayList<>(checkerOptions);

            if (flowPolicyFile.exists()) {
                optionsWithPf.add("-AflowPolicy=" + flowPolicyFile.getAbsolutePath());
                optionsWithPf.add("-AprintErrorStack");

                // AprintErrorStack
            } else {
                optionsWithPf.add("-AprintErrorStack");
            }

            // System.out.println("OPTIONS:\n" + join(optionsWithPf, " "));
            test(checkerName, optionsWithPf, testFile);
        }

        protected File getFile(final File javaFile, final String extension) {
            final String path = javaFile.getAbsolutePath();
            if (!path.endsWith(".java")) {
                throw new RuntimeException("Cannot recognize java file "
                        + javaFile.getAbsolutePath());
            } else {
                return new File(javaFile.getAbsolutePath().substring(0, path.length() - 5)
                        + extension);
            }
        }

        protected File getFlowPolicy(final File javaFile) {
            return getFile(javaFile, "Flowpolicy");
        }
    }

    public static class FlowStrictTests extends FlowCheckerTests {

        public FlowStrictTests(File testFile) {
             super(testFile,
                     "-Alint=strict-conditional",
                     "-AcheckCastElementType","-AinvariantArrays",
                     "-Anomsgtext");
             }

        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("strict");
        }
    }

    public static class StubfileTests extends FlowCheckerTests {
        public StubfileTests(File testFile) {
            super(testFile, "-Anomsgtext", "-AstubWarnIfNotFound");
        }

        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("stubfile");
        }

        @Override
        protected void test(final File testFile) {
            final File flowPolicyFile = getFlowPolicy(testFile);
            final File stubFile = getStubfile(testFile);
            final List<String> optionsWithPf = new ArrayList<>(checkerOptions);

            if (flowPolicyFile.exists() && !stubFile.exists()) {
                optionsWithPf.add("-AflowPolicy=" + flowPolicyFile.getAbsolutePath());
                optionsWithPf.add("-AprintErrorStack");

                // AprintErrorStack
            } else if (!flowPolicyFile.exists() && stubFile.exists()) {
                optionsWithPf.add("-Astubs=" + stubFile.getAbsolutePath());
                optionsWithPf.add("-AprintErrorStack");

                // AprintErrorStack
            } else if (flowPolicyFile.exists() && stubFile.exists()) {
                optionsWithPf.add("-AflowPolicy=" + flowPolicyFile.getAbsolutePath());
                optionsWithPf.add("-Astubs=" + stubFile.getAbsolutePath());
                optionsWithPf.add("-AprintErrorStack");

                // AprintErrorStack
            } else {
                optionsWithPf.add("-AprintErrorStack");
            }

            // System.out.println("OPTIONS:\n" + join(optionsWithPf, " "));
            test(checkerName, optionsWithPf, testFile);
        }

        protected File getStubfile(final File javaFile) {
            return getFile(javaFile, ".astub");
        }

        protected File getjarfile(final File javaFile) {
            return getFile(javaFile, ".jar");
        }

    }

    public static class FlowPolicyTests extends GenericFlowPolicyTest {
        private static String[] OPTIONS = new String[] { "-Anomsgtext" };

        public FlowPolicyTests(final String testDir, final String[] options) {
            super(testDir, options);
        }

        public static String inTestDir(final String subFolder) {
            return "policyfile" + File.separator + subFolder;
        }

        @Parameters
        public static Collection<Object[]> data() {
            return testDirs(OPTIONS, inTestDir("validation"), inTestDir("suppression"));
        }
    }

}
