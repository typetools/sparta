package tests;

import org.checkerframework.framework.test.CheckerFrameworkPerFileTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized.Parameters;

import sparta.checkers.FlowChecker;
import sparta.checkers.NotReviewedLibraryChecker;
import sparta.checkers.intents.IntentChecker;
import sparta.checkers.permission.AndroidFenumChecker;
import sparta.checkers.permission.PermissionsChecker;
import sparta.checkers.report.ReportAPIChecker;

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

    public static class AndroidFenumCheckerTests extends CheckerFrameworkPerFileTest {
        public AndroidFenumCheckerTests(File testFile) {
            super(testFile, AndroidFenumChecker.class, "sparta.checkers", "-Anomsgtext", "-AprintErrorStack");
//            super(testFile, AndroidFenumChecker.class, "sparta.checkers", "-Astubs=apiusage.astub");

        }

        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"fenums"};
        }
    }

    public static class AndroidPermissionsCheckerTests extends CheckerFrameworkPerFileTest {
        public AndroidPermissionsCheckerTests(File testFile) {
            super(testFile, PermissionsChecker.class, "sparta.checkers", "-Anomsgtext");
        }

        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"permissions"};
        }
    }

    public static class AndroidReportCheckerTests extends CheckerFrameworkPerFileTest {
        public AndroidReportCheckerTests(File testFile) {
            super(testFile, ReportAPIChecker.class, "sparta.checkers", "-Anomsgtext",
                    "-Astubs=apiusage.astub:suspicious.astub");
        }

        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"report"};
        }
    }

    public static class IntentCheckerTests extends CheckerFrameworkPerFileTest {
        public IntentCheckerTests(File testFile) {
            super(testFile, IntentChecker.class, "sparta.checkers", "-Anomsgtext");
            // Uncomment the line below to see the full errors in the JUnit tests
//             super(testFile, IntentChecker.class, "sparta.checkers");
        }

        private IntentCheckerTests(File testFile, String... checkerOptions) {
            super(testFile, IntentChecker.class, "sparta.checkers", checkerOptions);
        }


        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"intent"};
        }

        @Override
        public List<String> customizeOptions(List<String> previousOptions) {
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

            return optionsWithPf;
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
        public static String[] getTestDirs() {
            return new String[]{"parameterized"};
        }
    }
    public static class ReflectionFlowCheckerTests extends FlowCheckerTests {
        public ReflectionFlowCheckerTests(File testFile){
            super(testFile, "-Anomsgtext", "-AresolveReflection");
        }

        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"reflection"};
        }
    }

    public static class FlowCheckerTests extends CheckerFrameworkPerFileTest {
        public FlowCheckerTests(File testFile) {
            super(testFile, FlowChecker.class, "sparta.checkers", "-Anomsgtext");
            // Uncomment the line below to see the full errors in the JUnit tests
            // super(testFile, FlowChecker.class, "sparta.checkers");
        }

        protected FlowCheckerTests(File testFile, String... checkerOptions) {
            super(testFile, FlowChecker.class, "sparta.checkers", checkerOptions);
        }

        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"flow"};
        }

        @Override
        public List<String> customizeOptions(List<String> previousOptions) {
            final File flowPolicyFile = getFlowPolicy(testFile);
            final List<String> optionsWithPf = new ArrayList<>(checkerOptions);

            if (flowPolicyFile.exists()) {
                optionsWithPf.add("-AflowPolicy=" + flowPolicyFile.getAbsolutePath());
                optionsWithPf.add("-AprintErrorStack");

                // AprintErrorStack
            } else {
                optionsWithPf.add("-AprintErrorStack");
            }

            return optionsWithPf;
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
        public static String[] getTestDirs() {
            return new String[]{"strict"};
        }
    }

    public static class StubfileTests extends FlowCheckerTests {
        public StubfileTests(File testFile) {
            super(testFile, "-Anomsgtext", "-AstubWarnIfNotFound");
        }

        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"stubfile"};
        }

        @Override
        public List<String> customizeOptions(List<String> previousOptions) {
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

            return optionsWithPf;
        }

        protected File getStubfile(final File javaFile) {
            return getFile(javaFile, ".astub");
        }

        protected File getjarfile(final File javaFile) {
            return getFile(javaFile, ".jar");
        }

    }

    public static class FlowPolicyTests extends FlowCheckerTests {
        public FlowPolicyTests(File testFile) {
            super(testFile);

        }

        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"policyfile"};
        }
    }

    public static class NotReviewedLibraryCheckerTests extends CheckerFrameworkPerFileTest {
        final static String dirname="testOutput";
        final static String filename="testmissing.astub";
        final static String stubname=dirname+File.separator+filename;
        final static String dirOpt = "-A"+NotReviewedLibraryChecker.OUTPUT_DIR_OPTION+"="+dirname;
        final static String fileOpt = "-A"+NotReviewedLibraryChecker.OUTPUT_FILE_OPTION+"="+filename;
        final static String freqOpt = "-A"+NotReviewedLibraryChecker.PRINT_FREQUENCY_OPTION;

        public NotReviewedLibraryCheckerTests(File testFile) {

            super(testFile, NotReviewedLibraryChecker.class, "sparta.checkers", "-Anomsgtext", dirOpt,fileOpt, freqOpt);
        }

        protected NotReviewedLibraryCheckerTests(File testFile, String... checkerOptions) {
            super(testFile, NotReviewedLibraryChecker.class, "sparta.checkers", checkerOptions);
        }

        @Parameters
        public static String[] getTestDirs() {
            return new String[]{"not-reviewed"};
        }

        @Override
        public void run() {
            //Test it once, with expected errors
            super.run();

            // TODO: StubGenerator doesn't produce well formed stub files any more.
            // See https://github.com/typetools/checker-framework/issues/1488
            /*
            //Test it again with the generated stub file,
            //and expect no errors/warnings, but "Note: All methods reviewed"
            boolean shouldEmitDebugInfo = TestUtilities.getShouldEmitDebugInfo();

            List<String> optionsPlusStub = new ArrayList<String>(checkerOptions);
            optionsPlusStub.add("-Astubs=" + stubname);
            TestConfiguration config = buildDefaultConfiguration(testDir, testFile, checkerName, optionsPlusStub,
                                                                 shouldEmitDebugInfo);

            TypecheckExecutor executor = new TypecheckExecutor() {
                @Override
                protected List<TestDiagnostic> readDiagnostics(TestConfiguration config, CompilationResult compilationResult) {
                    return Arrays.asList(TestDiagnosticUtils.fromJavaxToolsDiagnostic("Note: All methods reviewed.", true));
                }
            };

            TypecheckResult testResult = executor.runTest(config);
            TestUtilities.assertResultsAreValid(testResult);
            */
        }
    }


}
