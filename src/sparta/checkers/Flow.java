package sparta.checkers;

import java.util.HashSet;
import java.util.Set;


import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.FlowSources.FlowSource;

public class Flow {
    Set<FlowSource> sources;
    Set<FlowSink> sinks;
    
    
    public Flow(Set<FlowSource> sources, Set<FlowSink> sinks) {
	this.sources = sources;
	this.sinks = sinks;
    }
    
    public Flow(FlowSource source, Set<FlowSink> sinks) {
	this.sources = new HashSet<FlowSource>();
	sources.add(source);
	this.sinks = sinks;
    }
    
    public Flow(Set<FlowSource> sources, FlowSink sink) {
	this.sources = sources;
	this.sinks = new HashSet<FlowSink>();
	sinks.add(sink);
    }
    public Flow(Set<FlowSource> sources) {
	this.sinks = new HashSet<FlowSink>();
	this.sources = sources;
    }
    public Flow(){
	this.sinks = new HashSet<FlowSink>();
	this.sources = new HashSet<FlowSource>();
    }
    public Flow(FlowSource source) {
	this.sinks = new HashSet<FlowSink>();
	this.sources = new HashSet<FlowSource>();    
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

    public void addSink(FlowSink sink) {
	sinks.add(sink);
    }

    public boolean hasSinks() {
	return !sinks.isEmpty();
    }
   
   
}
