package sparta.checkers;

import java.util.HashSet;
import java.util.Set;

import sparta.checkers.quals.FlowPermission;

public class Flow {
    Set<FlowPermission> sources;
    Set<FlowPermission> sinks;

    public Flow(Set<FlowPermission> sources, Set<FlowPermission> sinks) {
        this.sources = sources;
        this.sinks = sinks;
    }

    public Flow(FlowPermission source, Set<FlowPermission> sinks) {
        this.sources = new HashSet<FlowPermission>();
        if (source != null) {
            sources.add(source);
        }
        this.sinks = FlowUtil.allToAnySink(sinks, false);
    }

    public Flow(Set<FlowPermission> sources, FlowPermission sink) {
        this.sources = FlowUtil.allToAnySource(sources, false);
        this.sinks = new HashSet<FlowPermission>();
        sinks.add(sink);
    }

    public Flow(Set<FlowPermission> sources) {
        this.sinks = new HashSet<FlowPermission>();
        this.sources = sources;
    }

    public Flow() {
        this.sinks = new HashSet<FlowPermission>();
        this.sources = new HashSet<FlowPermission>();
    }

    public Flow(FlowPermission source) {
        this.sinks = new HashSet<FlowPermission>();
        this.sources = new HashSet<FlowPermission>();
        sources.add(source);
    }

    public String toString() {
        StringBuffer flow = new StringBuffer();
        if (sources.isEmpty()) {
            flow.append(" {}");
        } else {
            flow.append(sources);
        }
        flow.append("->");
        if (sinks.isEmpty()) {
            flow.append(" {}");
        } else {
            flow.append(sinks);
        }
        String flowstring = flow.toString().replace('[', ' ');
        flowstring = flowstring.toString().replace(']', ' ');
        return flowstring;
    }

    public void addSink(FlowPermission sink) {
        sinks.add(sink);
    }

    public boolean hasSink() {
        return !sinks.isEmpty();
    }

    public void addSink(Set<FlowPermission> sinks) {
        this.sinks.addAll(FlowUtil.allToAnySink(sinks, false));

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sinks == null) ? 0 : sinks.hashCode());
        result = prime * result + ((sources == null) ? 0 : sources.hashCode());
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
        Flow other = (Flow) obj;
        if (sinks == null) {
            if (other.sinks != null)
                return false;
        } else if (!sinks.equals(other.sinks))
            return false;
        if (sources == null) {
            if (other.sources != null)
                return false;
        } else if (!sources.equals(other.sources))
            return false;
        return true;
    }

}
