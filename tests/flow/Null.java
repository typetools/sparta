import sparta.checkers.quals.FlowSources;
import sparta.checkers.quals.FlowSources.FlowSource;

class Null {
  // null can be assigned to any source.
  @FlowSources(FlowSource.USER_INPUT) Object o = null;
}
