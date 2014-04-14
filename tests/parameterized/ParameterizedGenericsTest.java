import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;

import java.util.*;

class ParameterizedGenericsTest {
    @Source(value={}, finesources={@FineSource(value=LITERAL, params={"literal"})}) String literal_string;
    @Source(value={}, finesources={@FineSource(value=LITERAL, params={"bad_literal"})}) String bad_literal;
    @Source(value={}, finesources={@FineSource(value=LITERAL, params={"*te*"})}) String wildcard_string;
    
    public void testSourceGenerics() {
        List<@Source(value={}, finesources={@FineSource(value=LITERAL, params={"literal"})})String> strings = 
                new ArrayList<@Source(value={}, finesources={@FineSource(value=LITERAL, params={"literal"})})String>();
        
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