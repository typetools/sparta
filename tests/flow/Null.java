import sparta.checkers.quals.Sources;
import sparta.checkers.quals.Sources.FlowSource;

class Null {
  // null can be assigned to any source.
  @Sources(FlowSource.USER_INPUT) Object o = null;
}
