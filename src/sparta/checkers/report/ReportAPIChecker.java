package sparta.checkers.report;

import org.checkerframework.common.util.report.ReportChecker;
import org.checkerframework.common.util.report.qual.ReportCreation;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.qual.TypeQualifiers;
import org.checkerframework.framework.qual.Unqualified;

import java.util.Properties;

import javax.annotation.processing.SupportedOptions;
/**
 * Checker to report the usage of certain API methods
 * depending on what stub file is passed.
 * For example, states.astub, suspicious.astub, or api.astub.
 * used by ant target "report-suspicious".   
 */
//Keep qualifiers & options in sync with superclass.
@TypeQualifiers({ Unqualified.class })
@SupportedOptions({ "reportTreeKinds", "reportModifiers" })
public class ReportAPIChecker extends ReportChecker {

}