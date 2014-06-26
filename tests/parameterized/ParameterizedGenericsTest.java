import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;

import java.util.*;

class ParameterizedGenericsTest {
    @Source(finesources=@FineSource(value=FILESYSTEM, params="literal")) String literal_string;
    @Source(finesources=@FineSource(value=FILESYSTEM, params="bad_literal")) String bad_literal;
    @Source(finesources=@FineSource(value=FILESYSTEM, params="*te*")) String wildcard_string;
    
    public void testSourceGenerics() {
        List<@Source(value={}, finesources={@FineSource(value=FILESYSTEM, params={"literal"})})String> strings = 
                new ArrayList<@Source(value={}, finesources={@FineSource(value=FILESYSTEM ,params={"literal"})})String>();
        
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