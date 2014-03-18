package sparta.checkers;

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
//@StubFiles({"report.astub", "reflection.astub"})
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