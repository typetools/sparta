package sparta.checkers;

import java.util.Properties;

import javax.annotation.processing.SupportedOptions;

import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.quals.Unqualified;
import checkers.util.report.ReportChecker;

//Keep qualifiers & options in sync with superclass.
@TypeQualifiers({Unqualified.class})
@SupportedOptions({"reportTreeKinds"})
@StubFiles({"report.astub", "reflection.astub"})
public class AndroidReportChecker extends ReportChecker {

    // TODO: provide an annotation on the Checker class to change the
    // used message.properties file.
    // TODO: use a lint option to create user-friendly output.
    @Override
    public Properties getMessages() {
        if (this.messages != null)
            return this.messages;

        this.messages = new Properties();
        messages.putAll(getProperties(AndroidReportChecker.class, "json-report-messages.properties"));
        return this.messages;
    }

}