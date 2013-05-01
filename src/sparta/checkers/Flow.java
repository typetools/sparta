package sparta.checkers;

import java.util.HashSet;
import java.util.Set;


import sparta.checkers.quals.SPARTA_Permission;
import sparta.checkers.quals.SPARTA_Permission;

public class Flow {
    Set<SPARTA_Permission> sources;
    Set<SPARTA_Permission> sinks;
    
    
    public Flow(Set<SPARTA_Permission> sources, Set<SPARTA_Permission> sinks) {
	this.sources = sources;
	this.sinks = sinks;
    }
    
    public Flow(SPARTA_Permission source, Set<SPARTA_Permission> sinks) {
	this.sources = new HashSet<SPARTA_Permission>();
	sources.add(source);
	this.sinks = sinks;
    }
    
    public Flow(Set<SPARTA_Permission> sources, SPARTA_Permission sink) {
	this.sources = sources;
	this.sinks = new HashSet<SPARTA_Permission>();
	sinks.add(sink);
    }
    public Flow(Set<SPARTA_Permission> sources) {
	this.sinks = new HashSet<SPARTA_Permission>();
	this.sources = sources;
    }
    public Flow(){
	this.sinks = new HashSet<SPARTA_Permission>();
	this.sources = new HashSet<SPARTA_Permission>();
    }
    public Flow(SPARTA_Permission source) {
	this.sinks = new HashSet<SPARTA_Permission>();
	this.sources = new HashSet<SPARTA_Permission>();    
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

    public void addSink(SPARTA_Permission sink) {
	sinks.add(sink);
    }

    public boolean hasSinks() {
	return !sinks.isEmpty();
    }
   
   
}
