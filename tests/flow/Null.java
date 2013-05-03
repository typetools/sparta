import sparta.checkers.quals.Sources;
import sparta.checkers.quals.SpartaPermission;

class Null {
  // null can be assigned to any source.
  @Sources(SpartaPermission.USER_INPUT) Object o = null;
}
