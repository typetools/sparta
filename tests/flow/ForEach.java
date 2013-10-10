import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;


 public class ForEach {
//     public void test() {
//         for (Object obj : new ArrayList<Object>()) {               
//          }
//        }
//     public void testSMS() {
//         for (Object obj : new ArrayList<@Source(READ_SMS) Object>()) {   
//             sendToInternet(obj);
//          }
//         
//         for (Object obj : new ArrayList<@Source(INTERNET) Object>()) {   
//             //:: error: (argument.type.incompatible)
//             sendToInternet(obj);
//          }
//        }
//     
//     void sendToInternet(@Source(READ_SMS) Object obj){}
//     //Below are copied from CF all-systems tests.
//     void m1() {
//       Set<? extends @Source(LITERAL) CharSequence> s = new HashSet<@Source(LITERAL) CharSequence>( );
//       for( CharSequence cs : s ) {
//         cs.toString();
//       }
//     }
//
//     void m2() {
//       Set<CharSequence> s = new HashSet<CharSequence>( );
//       for( CharSequence cs : s ) {
//         cs.toString();
//       }
//     }
//
//     <T extends @Source(LITERAL) Object> void m3(T p) {
//       Set<T> s = new HashSet<T>( );
//       for( T cs : s ) {
//         cs.toString();
//       }
//     }
//
//     <T extends @Source(LITERAL) Object> void m4(T p) {
//       Set<T> s = new HashSet<T>( );
//       for( Object cs : s ) {
//         cs.toString();
//       }
//     }

     // An example taken from plume-lib's UtilMDE
     public static <T extends Object> List<T> removeDuplicates(List<T> l) {
       // There are shorter solutions that do not maintain order.
       HashSet<T> hs = new HashSet<T>(l.size());
       List<T> result = new ArrayList<T>();
       for (T t : l) {
         if (hs.add(t)) {
           result.add(t);
         }
       }
       return result;
     }

   }


