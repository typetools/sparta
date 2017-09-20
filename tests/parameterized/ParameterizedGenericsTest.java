import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermissionString.*;

import java.util.*;

class ParameterizedGenericsTest {
    @Source(FILESYSTEM+"(literal)") String literal_string;
    @Source(FILESYSTEM+"(bad_literal)") String bad_literal;
    @Source(FILESYSTEM+"(*te*)") String wildcard_string;
    
    public void testSourceGenerics() {
        List<@Source(FILESYSTEM+"(literal)") String> strings = 
                new ArrayList<@Source(FILESYSTEM+"(literal)") String>();
        
        // Fill array with good strings
        for (int i = 0; i < 5; i++) {
            strings.add(literal_string);
        }
        
        // Remove them from the list, and check validity
        for (int i = 0; i < strings.size(); i++) {
            // good
            wildcard_string = strings.get(i);
            //:: error: (assignment.type.incompatible)
            bad_literal = strings.get(i);
        }
    }
}