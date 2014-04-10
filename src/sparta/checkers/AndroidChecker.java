package sparta.checkers;

import org.checkerframework.framework.source.AggregateChecker;
import org.checkerframework.framework.source.SourceChecker;

import sparta.checkers.permission.AndroidFenumChecker;
import sparta.checkers.permission.RequiredPermissionsChecker;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An aggregate checker calling all Android type checkers.
 */
public class AndroidChecker extends AggregateChecker {

    @Override
    protected Collection<Class<? extends SourceChecker>> getSupportedCheckers() {
        Collection<Class<? extends SourceChecker>> checkers = new ArrayList<Class<? extends SourceChecker>>(
                2);
        checkers.add(AndroidFenumChecker.class);
        checkers.add(RequiredPermissionsChecker.class);
        checkers.add(AndroidReportChecker.class);
        checkers.add(FlowChecker.class);
        return checkers;
    }
}