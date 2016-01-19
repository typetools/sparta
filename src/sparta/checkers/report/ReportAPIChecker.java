package sparta.checkers.report;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.common.util.report.ReportChecker;
import org.checkerframework.common.util.report.ReportVisitor;
import org.checkerframework.common.util.report.qual.ReportUnqualified;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.SupportedOptions;

/**
 * Checker to report the usage of certain API methods
 * depending on what stub file is passed.
 * For example, states.astub, suspicious.astub, or api.astub.
 * used by ant target "report-suspicious".
 */
//Keep qualifiers & options in sync with superclass.
@SupportedOptions({ "reportTreeKinds", "reportModifiers" })
public class ReportAPIChecker extends ReportChecker {
    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new ReportAPIVisitor(this);
    }
}

class ReportAPIAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    public ReportAPIAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        postInit();
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        return Collections.unmodifiableSet(
            new HashSet<Class<? extends Annotation>>(Arrays.asList(ReportUnqualified.class)));
    }
}

class ReportAPIVisitor extends ReportVisitor {
    public ReportAPIVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    protected ReportAPIAnnotatedTypeFactory createTypeFactory() {
        return new ReportAPIAnnotatedTypeFactory(checker);
    }
}