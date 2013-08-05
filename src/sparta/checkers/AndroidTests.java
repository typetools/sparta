package sparta.checkers;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized.Parameters;

import checkers.util.test.ParameterizedCheckerTest;

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
            System.out.println("Run had " + run.getFailureCount() + " failure(s) out of " +
                    run.getRunCount() + " run(s)!");

            for (Failure f : run.getFailures()) {
                System.out.println(f.toString());
            }
        }
    }

    public static class AndroidFenumCheckerTests extends ParameterizedCheckerTest {
        public AndroidFenumCheckerTests(File testFile) {
            super(testFile, AndroidFenumChecker.class, "sparta.checkers", "-Anomsgtext");
        }
        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("fenums");
        }
    }

    public static class AndroidReqPermissionsCheckerTests extends ParameterizedCheckerTest {
        public AndroidReqPermissionsCheckerTests(File testFile) {
            super(testFile, PermissionsChecker.class, "sparta.checkers", "-Anomsgtext");
        }
        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("reqperms");
        }
    }

    public static class AndroidReportCheckerTests extends ParameterizedCheckerTest {
        public AndroidReportCheckerTests(File testFile) {
            super(testFile, AndroidReportChecker.class, "sparta.checkers", "-Anomsgtext", "-Astubs=apiusage.astub:suspicious.astub");
        }
        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("report");
        }
    }

    public static class FlowCheckerTests extends ParameterizedCheckerTest {
        public FlowCheckerTests(File testFile) {
             super(testFile, FlowChecker.class, "sparta.checkers", "-Anomsgtext", "-Astubs=tests/flow/flowtests.astub");
//           Uncomment the line below to see the full errors in the JUnit tests
//           super(testFile, FlowChecker.class, "sparta.checkers", "stubWarnIfNotFound", "-Astubs=tests/flow/flowtests.astub");
        }

        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("flow");
        }

        @Override
        protected void test(final File testFile) {
            final File flowPolicyFile = getFlowPolicy(testFile);
            final String [] optionsWithPf;

            if (flowPolicyFile.exists()) {
                optionsWithPf = Arrays.copyOf(checkerOptions, checkerOptions.length + 2);
                optionsWithPf[optionsWithPf.length - 1] = "-AflowPolicy=" + flowPolicyFile.getAbsolutePath();
                optionsWithPf[optionsWithPf.length - 2] = "-AprintErrorStack";

                //AprintErrorStack
            } else {
                optionsWithPf = Arrays.copyOf(checkerOptions, checkerOptions.length + 1);
                optionsWithPf[optionsWithPf.length - 1] = "-AprintErrorStack";

            }

            // System.out.println("OPTIONS:\n" + join(optionsWithPf, " "));
            test(checkerName, optionsWithPf, testFile);
        }

        protected File getFile(final File javaFile, final String extension) {
            final String path = javaFile.getAbsolutePath();
            if (!path.endsWith(".java")) {
                throw new RuntimeException("Cannot recognize java file " + javaFile.getAbsolutePath());
            } else {
                return new File(javaFile.getAbsolutePath().substring(0, path.length() - 5) + extension);
            }
        }

        protected File getFlowPolicy(final File javaFile) {
            return getFile(javaFile, "Flowpolicy");
        }
    }

    public static class StubfileTests extends FlowCheckerTests{
        public StubfileTests(File testFile) {
            super(testFile);
        }

        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("stubfile");
        }

        @Override
        protected void test(final File testFile) {
            final File flowPolicyFile = getFlowPolicy(testFile);
            final File stubFile = getStubfile(testFile);
            final String [] optionsWithPf;
            // int length = checkerOptions.length;

            if (flowPolicyFile.exists() && !stubFile.exists()) {
                optionsWithPf = Arrays.copyOf(checkerOptions, checkerOptions.length + 2);
                optionsWithPf[optionsWithPf.length - 1] = "-AflowPolicy=" + flowPolicyFile.getAbsolutePath();
                optionsWithPf[optionsWithPf.length - 2] = "-AprintErrorStack";

                //AprintErrorStack
            } else if (!flowPolicyFile.exists() && stubFile.exists()) {
                optionsWithPf = Arrays.copyOf(checkerOptions, checkerOptions.length + 2);
                optionsWithPf[optionsWithPf.length - 1] = "-Astubs=" + stubFile.getAbsolutePath();
                optionsWithPf[optionsWithPf.length - 2] = "-AprintErrorStack";

                //AprintErrorStack
            } else if (flowPolicyFile.exists() && stubFile.exists()) {
                optionsWithPf = Arrays.copyOf(checkerOptions, checkerOptions.length + 3);
                optionsWithPf[optionsWithPf.length - 1] = "-AflowPolicy=" + flowPolicyFile.getAbsolutePath();
                optionsWithPf[optionsWithPf.length - 2] = "-Astubs=" + stubFile.getAbsolutePath();
                optionsWithPf[optionsWithPf.length - 3] = "-AprintErrorStack";

                //AprintErrorStack
            } else {
                optionsWithPf = Arrays.copyOf(checkerOptions, checkerOptions.length + 1);
                optionsWithPf[optionsWithPf.length - 1] = "-AprintErrorStack";

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
        private static String [] OPTIONS = new String[]{ "-Anomsgtext" };

        public FlowPolicyTests(final String testDir, final String [] options) {
            super(testDir, options);
        }

        public static String inTestDir(final String subFolder) {
            return "policyfile" + File.separator + subFolder;
        }

        @Parameters
        public static Collection<Object[]> data() {
            return testDirs(OPTIONS,
                    inTestDir( "validation" ),
                    inTestDir( "suppression" )
            );
        }
    }

    public static class ReflectionTests extends ParameterizedCheckerTest {
        public ReflectionTests(File testFile) {
             super(testFile, FlowChecker.class, "sparta.checkers", "-Anomsgtext", "-AstubWarnIfNotFound", "-Astubs=tests/reflection/reflection.astub", "-AresolveReflection", "-AdebugReflection");
        }

        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("reflection");
        }

        @Override
        protected void test(final File testFile) {
            final File flowPolicyFile = getFlowPolicy(testFile);
            final String [] optionsWithPf;

            if(flowPolicyFile.exists()) {
                optionsWithPf = Arrays.copyOf(checkerOptions, checkerOptions.length + 2);
                optionsWithPf[optionsWithPf.length - 1] = "-AflowPolicy=" + flowPolicyFile.getAbsolutePath();
                optionsWithPf[optionsWithPf.length - 2] = "-AprintErrorStack";

                //AprintErrorStack
            } else {
                 optionsWithPf = Arrays.copyOf(checkerOptions, checkerOptions.length + 1);
                  optionsWithPf[optionsWithPf.length - 1] = "-AprintErrorStack";

            }
            test(checkerName, optionsWithPf, testFile);
        }

        protected File getFile(final File javaFile, final String extension) {
            final String path = javaFile.getAbsolutePath();
            if(!path.endsWith(".java")) {
                throw new RuntimeException("Cannot recognize java file " + javaFile.getAbsolutePath());
            } else {
                return new File(javaFile.getAbsolutePath().substring(0, path.length() - 5) + extension);
            }
        }

        protected File getFlowPolicy(final File javaFile) {
            return getFile(javaFile, "Flowpolicy");
        }
    }
}
