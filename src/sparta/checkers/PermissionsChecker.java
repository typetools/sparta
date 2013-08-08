package sparta.checkers;

import java.util.ArrayList;
import java.util.Collection;

import checkers.source.AggregateChecker;
import checkers.source.SourceChecker;

public class PermissionsChecker extends AggregateChecker {

    @Override
    protected Collection<Class<? extends SourceChecker<?>>> getSupportedCheckers() {
        Collection<Class<? extends SourceChecker<?>>> checkers = new ArrayList<>();
        checkers.add(RequiredPermissionsChecker.class);
        checkers.add(DependentPermissionsChecker.class);

        return checkers;
    }
}
