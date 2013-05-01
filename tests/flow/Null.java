import sparta.checkers.quals.Sources;
import sparta.checkers.quals.Sources.SPARTA_Permission;

class Null {
  // null can be assigned to any source.
  @Sources(SPARTA_Permission.USER_INPUT) Object o = null;
}
