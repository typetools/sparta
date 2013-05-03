package sparta.checkers;

import java.util.HashSet;
import java.util.Set;


import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.SpartaPermission;

public class Flow {
    Set<SpartaPermission> sources;
    Set<SpartaPermission> sinks;
    
    
    public Flow(Set<SpartaPermission> sources, Set<SpartaPermission> sinks) {
	this.sources = sources;
	this.sinks = sinks;
    }
    
    public Flow(SpartaPermission source, Set<SpartaPermission> sinks) {
	this.sources = new HashSet<SpartaPermission>();
	sources.add(source);
	this.sinks = sinks;
    }
    
    public Flow(Set<SpartaPermission> sources, SpartaPermission sink) {
	this.sources = sources;
	this.sinks = new HashSet<SpartaPermission>();
	sinks.add(sink);
    }
    public Flow(Set<SpartaPermission> sources) {
	this.sinks = new HashSet<SpartaPermission>();
	this.sources = sources;
    }
    public Flow(){
	this.sinks = new HashSet<SpartaPermission>();
	this.sources = new HashSet<SpartaPermission>();
    }
    public Flow(SpartaPermission source) {
	this.sinks = new HashSet<SpartaPermission>();
	this.sources = new HashSet<SpartaPermission>();    
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

    public void addSink(SpartaPermission sink) {
	sinks.add(sink);
    }

    public boolean hasSinks() {
	return !sinks.isEmpty();
    }
   
   
}
