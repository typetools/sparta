package sparta.checkers;

import javax.annotation.processing.SupportedOptions;

import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.quals.Unqualified;
import checkers.util.report.ReportChecker;

//Keep qualifiers & options in sync with superclass.
@TypeQualifiers({Unqualified.class})
@SupportedOptions({"reportTreeKinds"})
@StubFiles({"report.astub", "reflection.astub"})
public class AndroidReportChecker extends ReportChecker {}