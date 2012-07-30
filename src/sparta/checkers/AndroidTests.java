package sparta.checkers;

import java.io.File;
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

        if( run.wasSuccessful() ) {
            System.out.println("Run was successful with " + run.getRunCount() + " test(s)!");
        } else {
            System.out.println("Run had " + run.getFailureCount() + " failure(s) out of " +
                    run.getRunCount() + " run(s)!");

            for( Failure f : run.getFailures() ) {
                System.out.println(f.toString());
            }
        }
    }

    public static class AndroidFenumCheckerTests extends ParameterizedCheckerTest {
        public AndroidFenumCheckerTests(File testFile) {
            super(testFile, AndroidFenumChecker.class.getName(), "sparta.checkers", "-Anomsgtext");
        }
        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("fenums");
        }
    }

    public static class AndroidReqPermissionsCheckerTests extends ParameterizedCheckerTest {
        public AndroidReqPermissionsCheckerTests(File testFile) {
            super(testFile, PermissionsChecker.class.getName(), "sparta.checkers", "-Anomsgtext");
        }
        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("reqperms");
        }
    }

    public static class AndroidReportCheckerTests extends ParameterizedCheckerTest {
        public AndroidReportCheckerTests(File testFile) {
            super(testFile, AndroidReportChecker.class.getName(), "sparta.checkers", "-Anomsgtext");
        }
        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("report");
        }
    }

    public static class FlowCheckerTests extends ParameterizedCheckerTest {
        public FlowCheckerTests(File testFile) {
            super(testFile, FlowChecker.class.getName(), "sparta.checkers", "-Anomsgtext");
        }
        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("flow");
        }
    }
}