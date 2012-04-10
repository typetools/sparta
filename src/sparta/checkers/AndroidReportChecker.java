package sparta.checkers;

import javax.annotation.processing.SupportedOptions;

import checkers.quals.TypeQualifiers;
import checkers.quals.Unqualified;
import checkers.util.report.ReportChecker;

//Keep qualifiers & options in sync with superclass.
@TypeQualifiers({Unqualified.class})
@SupportedOptions({"reportTreeKinds"})
// TODO: add an annotation to provide additional .astub files
public class AndroidReportChecker extends ReportChecker {}