import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources;
import sparta.checkers.quals.FlowSources.FlowSource;

class PolyOverride {

    interface Comparator<T extends @FlowSources(FlowSource.ANY) Object> {
        @PolyFlowSources @PolyFlowSinks int compare(@PolyFlowSources @PolyFlowSinks T lhs, @PolyFlowSources @PolyFlowSinks T rhs);
    }

    class ObjectComparator implements Comparator<@FlowSources(FlowSource.LOCATION) Object> {
        // TODO: We would want the following to be a valid override:
        @SuppressWarnings("flow")
        public @FlowSources(FlowSource.LOCATION) int compare(@FlowSources(FlowSource.LOCATION) Object left,
                @FlowSources(FlowSource.LOCATION) Object right) {
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
