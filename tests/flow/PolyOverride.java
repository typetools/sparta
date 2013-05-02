import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources;
import sparta.checkers.quals.SPARTA_Permission;

class PolyOverride {

    interface Comparator<T extends @Sources(SPARTA_Permission.ANY) Object> {
        @PolySources @PolySinks int compare(@PolySources @PolySinks T lhs, @PolySources @PolySinks T rhs);
    }

    class ObjectComparator implements Comparator<@Sources(SPARTA_Permission.LOCATION) Object> {
        // TODO: We would want the following to be a valid override:
        @SuppressWarnings("flow")
        public @Sources(SPARTA_Permission.LOCATION) int compare(@Sources(SPARTA_Permission.LOCATION) Object left,
                @Sources(SPARTA_Permission.LOCATION) Object right) {
            return 0;
        }
        // However, we only substitute the type variables, we do not bind polymorphic qualifiers.
        // Could/should we perform that binding?
        // Basically, what we want is a way to bind the qualifiers from the instantiation and use
        // that in the return type.
        // The signature in super could be:
        //    @QualifiersOf("T") int compare(T lhs, T rhs);
        // Any other possible solutions?

    }
}
