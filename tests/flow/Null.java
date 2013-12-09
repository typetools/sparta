import sparta.checkers.quals.Source;
import static sparta.checkers.quals.CoarseFlowPermission.*;


class Null {
  // null can be assigned to any source.
  @Source(USER_INPUT) Object o = null;
  
  void test(){
      Object nullObject = null;
      param(nullObject);
      param(null);
  }
  void param(Object o){
      
  }
}
