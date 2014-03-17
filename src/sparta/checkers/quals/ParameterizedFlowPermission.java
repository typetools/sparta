package sparta.checkers.quals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ParameterizedFlowPermission implements Comparable<ParameterizedFlowPermission> {
    private FlowPermission permission;
    private List<String> parameters; 
    
    public ParameterizedFlowPermission(FlowPermission permission) {
        this( permission, new ArrayList<String>());     
    }

    
    public ParameterizedFlowPermission(FlowPermission permission, List<String> parameters) {
        this.permission = permission;
        this.parameters = parameters; 
        if(parameters.isEmpty()) {
            parameters.add("*");
        }
    }
    
    public FlowPermission getPermission() {
        return this.permission;
    }
    
    public List<String> getParameters() {
        return Collections.unmodifiableList(parameters);
    }
    
    
    @Override
    public String toString() {
        // Easy case, not parameterized 
        if (getParameters() == null || getParameters().size() == 0) {
            return getPermission().toString();
        }
        String parameterizedToString = getPermission().toString() + "(";
        for (String param : getParameters()) {
            parameterizedToString += param + ","; 
        }
        // Return the built string, removing the final comma, and closing it with the parenthesis
        return parameterizedToString.substring(0, parameterizedToString.length() - 1) + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result
                + ((permission == null) ? 0 : permission.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParameterizedFlowPermission other = (ParameterizedFlowPermission) obj;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (permission != other.permission)
            return false;
        return true;
    }

    @Override
    public int compareTo(ParameterizedFlowPermission other) {
        if (this.permission == other.permission && this.parameters.equals(other.parameters)) {
            return 0;
        } else if (this.permission.compareTo(other.permission) > 0) {
            return 1;
        }
        return -1;
    }

    // TODO: Ask where this method belongs. Static method in another class?
    static public boolean coarsePermissionExists(ParameterizedFlowPermission target, Set<ParameterizedFlowPermission> permissions) {
        for (ParameterizedFlowPermission permission : permissions) {
            if (permission.equals(target) ) {
                return true;
            }
        }
        return false;
    }

    public boolean isSink() {
        return getPermission().isSink();
    }
}
