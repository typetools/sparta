import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources;
import sparta.checkers.quals.FlowSources.FlowSource;

class PolyOverride {

    class ObjectComparator implements Comparator</*@FlowSources(FlowSource.LOCATION)*/ Object> {
        public @FlowSources(FlowSource.LOCATION) int compare(@FlowSources(FlowSource.LOCATION) Object left,
        		@FlowSources(FlowSource.LOCATION) Object right) {
            return 0;
        }
    }
	interface Comparator<T extends @FlowSources(sparta.checkers.quals.FlowSources.FlowSource.ANY) Object> {
		@PolyFlowSources @PolyFlowSinks int compare(@PolyFlowSources @PolyFlowSinks T lhs, @PolyFlowSources @PolyFlowSinks T rhs);
	}	
}
