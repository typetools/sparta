import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources;
import sparta.checkers.quals.Sources.FlowSource;

class PolyOverride {

    interface Comparator<T extends @Sources(FlowSource.ANY) Object> {
        @PolySources @PolySinks int compare(@PolySources @PolySinks T lhs, @PolySources @PolySinks T rhs);
    }

    class ObjectComparator implements Comparator<@Sources(FlowSource.LOCATION) Object> {
        // TODO: We would want the following to be a valid override:
        @SuppressWarnings("flow")
        public @Sources(FlowSource.LOCATION) int compare(@Sources(FlowSource.LOCATION) Object left,
                @Sources(FlowSource.LOCATION) Object right) {
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
