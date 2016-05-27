package sparta.checkers.permission;

import org.checkerframework.checker.fenum.FenumAnnotatedTypeFactory;
import org.checkerframework.checker.fenum.FenumChecker;
import org.checkerframework.checker.fenum.FenumVisitor;
import org.checkerframework.checker.fenum.qual.Fenum;
import org.checkerframework.checker.fenum.qual.FenumBottom;
import org.checkerframework.checker.fenum.qual.FenumTop;
import org.checkerframework.checker.fenum.qual.FenumUnqualified;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.qual.PolyAll;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.SupportedOptions;

import sparta.checkers.permission.qual.Permission;

@SupportedOptions({ "quals", "qualDirs" })
public class AndroidFenumChecker extends FenumChecker {
    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new AndroidFenumVisitor(this);
    }
}

class AndroidFenumVisitor extends FenumVisitor{
    public AndroidFenumVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    protected AndroidFenumAnnotatedTypeFactory createTypeFactory() {
        return new AndroidFenumAnnotatedTypeFactory(checker);
    }
}

class AndroidFenumAnnotatedTypeFactory extends FenumAnnotatedTypeFactory {
    public AndroidFenumAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {

        Set<Class<? extends Annotation>> set = new HashSet<>();
        // Load top, bottom, unqualified, and fake enum
        set.add(FenumTop.class);
        set.add(FenumUnqualified.class);
        set.add(FenumBottom.class);
        set.add(Permission.class);
        return Collections.unmodifiableSet(set);
    }
}