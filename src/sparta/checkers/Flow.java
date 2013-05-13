package sparta.checkers;

import java.util.HashSet;
import java.util.Set;


import sparta.checkers.quals.FlowPermission;
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
	sources.add(source);
	this.sinks = sinks;
    }
    
    public Flow(Set<FlowPermission> sources, FlowPermission sink) {
	this.sources = sources;
	this.sinks = new HashSet<FlowPermission>();
	sinks.add(sink);
    }
    public Flow(Set<FlowPermission> sources) {
	this.sinks = new HashSet<FlowPermission>();
	this.sources = sources;
    }
    public Flow(){
	this.sinks = new HashSet<FlowPermission>();
	this.sources = new HashSet<FlowPermission>();
    }
    public Flow(FlowPermission source) {
	this.sinks = new HashSet<FlowPermission>();
	this.sources = new HashSet<FlowPermission>();    
	sources.add(source);
    }

    public String toString(){
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
   
   
}
