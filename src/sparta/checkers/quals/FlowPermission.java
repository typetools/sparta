package sparta.checkers.quals;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class FlowPermission implements Comparable<FlowPermission> {
    private CoarseFlowPermission permission;
    private Set<String> parameters; 
    
    public FlowPermission(CoarseFlowPermission permission) {
        this.permission = permission;
        this.parameters = new TreeSet<String>();
    }
    
    public FlowPermission(CoarseFlowPermission permission, Set<String> parameters) {
        this.permission = permission;
        this.parameters = parameters; 
    }
    
    public CoarseFlowPermission getPermission() {
        return this.permission;
    }
    
    public Set<String> getParameters() {
        return Collections.unmodifiableSet(parameters);
    }
    
    
    @Override
    public String toString() {
        return "FlowPermission [permission=" + permission + ", parameters="
                + parameters + "]";
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
        FlowPermission other = (FlowPermission) obj;
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
    public int compareTo(FlowPermission other) {
        if (this.permission == other.permission && this.parameters.equals(other.parameters)) {
            return 0;
        } else if (this.permission.compareTo(other.permission) > 0) {
            return 1;
        }
        return -1;
    }

    // TODO: Ask where this method belongs. Static method in another class?
    static public boolean coarsePermissionExists(FlowPermission target, Set<FlowPermission> permissions) {
        for (FlowPermission permission : permissions) {
            if (permission.equals(target) ) {
                return true;
            }
        }
        return false;
    }
}
