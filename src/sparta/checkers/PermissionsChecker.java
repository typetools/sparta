package sparta.checkers;

import org.checkerframework.framework.source.AggregateChecker;
import org.checkerframework.framework.source.SourceChecker;

import java.util.ArrayList;
import java.util.Collection;

public class PermissionsChecker extends AggregateChecker {

    @Override
    protected Collection<Class<? extends SourceChecker>> getSupportedCheckers() {
        Collection<Class<? extends SourceChecker>> checkers = new ArrayList<>();
        checkers.add(RequiredPermissionsChecker.class);
        checkers.add(DependentPermissionsChecker.class);

        return checkers;
    }
}
