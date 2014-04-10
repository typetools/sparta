package sparta.checkers.permission;

import org.checkerframework.checker.fenum.FenumChecker;
import org.checkerframework.framework.qual.TypeQualifiers;

import javax.annotation.processing.SupportedOptions;

import sparta.checkers.permission.qual.Permission;
@SupportedOptions( { "quals" } )
@TypeQualifiers({ Permission.class })
public class AndroidFenumChecker extends FenumChecker {
}