import sparta.checkers.quals.Source;
import sparta.checkers.quals.FlowPermission;

class Null {
  // null can be assigned to any source.
  @Source(FlowPermission.USER_INPUT) Object o = null;
  
  void test(){
      Object nullObject = null;
      param(nullObject);
      param(null);
  }
  void param(Object o){
      
  }
}
