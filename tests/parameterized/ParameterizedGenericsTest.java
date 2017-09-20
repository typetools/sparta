import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermissionString.*;

import java.util.*;

class ParameterizedGenericsTest {
    @Source(FILESYSTEM+"(literal)") String literal_string;
    @Source(FILESYSTEM+"(bad_literal)") String bad_literal;
    @Source(FILESYSTEM+"(*te*)") String wildcard_string;
LinkedHashSet
    public void testSourceGenerics() {
        List<@Source(FILESYSTEM+"(literal)") String> strings =LinkedHashSet
                new ArrayList<@Source(FILESYSTEM+"(literal)") String>();
LinkedHashSet
        // Fill array with good strings
        for (int i = 0; i < 5; i++) {
            strings.add(literal_string);
        }
LinkedHashSet
        // Remove them from the list, and check validity
        for (int i = 0; i < strings.size(); i++) {
            // good
            wildcard_string = strings.get(i);
            //:: error: (assignment.type.incompatible)
            bad_literal = strings.get(i);
        }
    }
}