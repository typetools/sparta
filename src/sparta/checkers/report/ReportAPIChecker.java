package sparta.checkers.report;

import org.checkerframework.common.util.report.ReportChecker;
import org.checkerframework.common.util.report.qual.ReportCreation;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.qual.TypeQualifiers;
import org.checkerframework.framework.qual.Unqualified;

import java.util.Properties;

import javax.annotation.processing.SupportedOptions;

//Keep qualifiers & options in sync with superclass.
@TypeQualifiers({ Unqualified.class })
@SupportedOptions({ "reportTreeKinds", "reportModifiers" })
public class ReportAPIChecker extends ReportChecker {

}