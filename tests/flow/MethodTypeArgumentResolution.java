import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import java.util.*;

class MethodTypeArgumentResolution {
	void foo(List<@FlowSources(FlowSource.LOCATION) Object> l, Comparator<@FlowSources(FlowSource.LOCATION) Object> c) {
		
		//Unexpected incompatible type error, because method type argument is not inferred correctly.
		Collections.sort(l, c);
		Collections.</*@FlowSources(FlowSource.LOCATION)*/ Object>sort(l, c);
	}
}
