import sparta.checkers.quals.*;
import sparta.checkers.quals.Source;
import sparta.checkers.quals.FlowPermission;

class PolyOverride {

    interface Comparator<T extends Object> {
        @PolySource @PolySink int compare(@PolySource @PolySink T lhs, @PolySource @PolySink T rhs);
    }

    class ObjectComparator implements Comparator<@Source(FlowPermission.ACCESS_FINE_LOCATION) Object> {
        // TODO: We would want the following to be a valid override:
        @SuppressWarnings("flow")
        public @Source(FlowPermission.ACCESS_FINE_LOCATION) int compare(@Source(FlowPermission.ACCESS_FINE_LOCATION) Object left,
                @Source(FlowPermission.ACCESS_FINE_LOCATION) Object right) {
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
