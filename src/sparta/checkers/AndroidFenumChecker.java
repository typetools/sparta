package sparta.checkers;

import checkers.fenum.FenumChecker;
import checkers.quals.TypeQualifiers;

import javax.annotation.processing.SupportedOptions;

import sparta.checkers.quals.Permission;
@SupportedOptions( { "quals" } )
@TypeQualifiers({ Permission.class })
public class AndroidFenumChecker extends FenumChecker {
}