package sparta.checkers;

import checkers.types.AnnotatedTypeMirror;

import javacutils.AnnotationUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.AnnotationMirror;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.ParameterizedFlowPermission;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

public class Flow {
    private static ParameterizedFlowPermission ANY = new ParameterizedFlowPermission(FlowPermission.ANY);
    
    Set<ParameterizedFlowPermission> sources;
    Set<ParameterizedFlowPermission> sinks;

    public Flow(Set<ParameterizedFlowPermission> sources, Set<ParameterizedFlowPermission> sinks) {      
        this.sources = sources;
        this.sinks = sinks;
    }

    public Flow(ParameterizedFlowPermission source, Set<ParameterizedFlowPermission> sinks) {
        this.sources = new TreeSet<ParameterizedFlowPermission>();
        if (source != null) {
            sources.add(source);
        }
        this.sinks = convertToAnySink(sinks, false);
    }

    public Flow(Set<ParameterizedFlowPermission> sources, ParameterizedFlowPermission sink) {
        this.sources = convertToAnySource(sources, false);
        this.sinks = new TreeSet<ParameterizedFlowPermission>();
        sinks.add(sink);
    }

    public Flow(AnnotatedTypeMirror atm) {
        this.sinks = getSinks(atm);
        this.sources = getSources(atm);
    }

    public Flow() {
        this.sinks = new TreeSet<ParameterizedFlowPermission>();
        this.sources = new TreeSet<ParameterizedFlowPermission>();
    }

    public Flow(ParameterizedFlowPermission source) {
        this.sinks = new TreeSet<ParameterizedFlowPermission>();
        this.sources = new TreeSet<ParameterizedFlowPermission>();
        sources.add(source);
    }

    @Override
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

    public void addSink(ParameterizedFlowPermission sink) {
        sinks.add(sink);
    }

    public boolean hasSink() {
        return !sinks.isEmpty();
    }

    public void addSink(Set<ParameterizedFlowPermission> sinks) {
        this.sinks.addAll(convertToAnySink(sinks, false));

    }

    public void addSource(ParameterizedFlowPermission source) {
        sources.add(source);
    }
    public void addSource(Set<ParameterizedFlowPermission> source) {
        this.sources.addAll(convertToAnySource(source, false));
    }
    public boolean isBottom(){
        return sinks.contains(ANY) && sources.isEmpty();
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

    public static Set<ParameterizedFlowPermission> getSinks(final AnnotatedTypeMirror type) {
        for(AnnotationMirror anno : type.getEffectiveAnnotations()){
            if(AnnotationUtils.areSameByClass(anno, Sink.class)){
               return getSinks(anno);
            }
        }
        return new TreeSet<ParameterizedFlowPermission>();
    }

    public static Set<ParameterizedFlowPermission> getSources(final AnnotatedTypeMirror type) {
        for(AnnotationMirror anno : type.getEffectiveAnnotations()){
            if(AnnotationUtils.areSameByClass(anno, Source.class)){
               return getSources( anno);
            }
        }
        return new TreeSet<ParameterizedFlowPermission>();
    }
    
    public static Set<ParameterizedFlowPermission> getSinks(final AnnotationMirror am) {
        if (am == null) {
            return new TreeSet<ParameterizedFlowPermission>();
        }

        Set<ParameterizedFlowPermission> sinkFlowPermissions = new TreeSet<ParameterizedFlowPermission>();
        List<FlowPermission> sinks = AnnotationUtils.getElementValueEnumArray(am, "value",
                FlowPermission.class, true);
        for (FlowPermission coarsePermission : sinks)
            sinkFlowPermissions.add(new ParameterizedFlowPermission(coarsePermission));
        Set<ParameterizedFlowPermission> retSet = convertToAnySink(sinkFlowPermissions, false);
        return retSet;
    }

    public static Set<ParameterizedFlowPermission> getSources(final AnnotationMirror am) {
        if (am == null) {
            return new TreeSet<ParameterizedFlowPermission>();
        }

        Set<ParameterizedFlowPermission> sourceFlowPermissions = new TreeSet<ParameterizedFlowPermission>();
        List<FlowPermission> sinks = AnnotationUtils.getElementValueEnumArray(am, "value",
                FlowPermission.class, true);
        for (FlowPermission coarsePermission : sinks)
            sourceFlowPermissions.add(new ParameterizedFlowPermission(coarsePermission));
        Set<ParameterizedFlowPermission> retSet = convertToAnySource(sourceFlowPermissions, false);
        return retSet;
    }

    /**
     * Replace ANY with the list of all possible sinks
     * @param sinks
     * @param inPlace
     * @return
     */
    private static Set<ParameterizedFlowPermission> convertAnyToAllSinks(final Set<ParameterizedFlowPermission> sinks,
            boolean inPlace) {
        final Set<ParameterizedFlowPermission> retSet = (inPlace) ? sinks : new TreeSet<ParameterizedFlowPermission>(sinks);
        if (sinks.contains(ANY)) {
            retSet.addAll(getSetOfAllSinks());
            retSet.remove(ANY);
        }
        return retSet;
    }

    /**
     * Replace ANY with the list of all possible sources
     * @param sources
     * @param inPlace
     * @return
     */
    private static Set<ParameterizedFlowPermission> convertAnytoAllSources(final Set<ParameterizedFlowPermission> sources,
            boolean inPlace) {
        final Set<ParameterizedFlowPermission> retSet = (inPlace) ? sources : new TreeSet<ParameterizedFlowPermission>(
                sources);
        if (sources.contains(ANY)) {
            retSet.addAll(getSetOfAllSinks());
            retSet.remove(ANY);
        }
        return retSet;
    }

    /**
     * All possible sources, excluding ANY
     * TODO: This returns all sources and sinks, not just sources...fix this
     * @return
     */
    public static Set<ParameterizedFlowPermission> getSetOfAllSources() {
        List<FlowPermission> coarseFlowList = Arrays.asList(FlowPermission.values());
        Set<ParameterizedFlowPermission> flowPermissionSet = new TreeSet<>();
        for (FlowPermission permission : coarseFlowList) {
            if (permission != FlowPermission.ANY) {
                flowPermissionSet.add(new ParameterizedFlowPermission(permission)); 
            }
        }
        return flowPermissionSet;
    }

    /**
     * All possible sinks, excluding ANY
     * TODO: This returns all sources and sinks, not just sinks...fix this
     * @return
     */
    public static Set<ParameterizedFlowPermission> getSetOfAllSinks() {
        List<FlowPermission> coarseFlowList = Arrays.asList(FlowPermission.values());
        Set<ParameterizedFlowPermission> flowPermissionSet = new TreeSet<>();
        for (FlowPermission permission : coarseFlowList) {
            if (permission != FlowPermission.ANY) {
                flowPermissionSet.add(new ParameterizedFlowPermission(permission)); 
            }
        }
        return flowPermissionSet;
    }

    /**
     * If sources contains all possible sources, then return ANY.
     * If sources contains ANY and some other sources, then return ANY
     * @param sources
     * @param inPlace
     * @return
     */
    public static Set<ParameterizedFlowPermission> convertToAnySource(final Set<ParameterizedFlowPermission> sources,
            boolean inPlace) {
        final Set<ParameterizedFlowPermission> retSet = (inPlace) ? sources : new TreeSet<ParameterizedFlowPermission>(sources);
        if(retSet.equals(getSetOfAllSources())) {
            retSet.clear();
            retSet.add(ANY);
        }else if(retSet.contains(ANY)){
            retSet.clear();
            retSet.add(ANY);
        }
        return retSet;
    }

    /**
     * If sinks contains all possible sinks, then return ANY.
     * If sinks contains ANY and some other sinks, then return ANY
     * @param sinks
     * @param inPlace
     * @return either {ANY} or sinks
     */
    public static Set<ParameterizedFlowPermission> convertToAnySink(final Set<ParameterizedFlowPermission> sinks, boolean inPlace) {
        final Set<ParameterizedFlowPermission> retSet = (inPlace) ? sinks : new TreeSet<ParameterizedFlowPermission>(sinks);
        if(sinks.equals(getSetOfAllSinks())) {
            retSet.clear();
            retSet.add(ANY);
        }else if(retSet.contains(ANY)){
            retSet.clear();
            retSet.add(ANY);
        }
        return retSet;
    }

    public static boolean isTop(AnnotatedTypeMirror atm) {
        Set<ParameterizedFlowPermission> sources = getSources(atm);
        Set<ParameterizedFlowPermission> sinks = getSinks(atm);
        return sources.contains(ANY) && sinks.isEmpty();
    }
    public static boolean isBottom(AnnotatedTypeMirror atm) {
        Set<ParameterizedFlowPermission> sources = getSources(atm);
        Set<ParameterizedFlowPermission> sinks = getSinks(atm);
        return sinks.contains(ANY) && sources.isEmpty();
    }
    /**
     * Return the set of sources that both annotations have.
     * If the intersection is all possible sources, {ANY} is returned
     * @param a1 AnnotationMirror, could be {ANY}
     * @param a2 AnnotationMirror, could be {ANY}
     * @return intersection of a1 and a2
     */
    public static Set<ParameterizedFlowPermission> intersectSources(AnnotationMirror a1, AnnotationMirror a2){
        final Set<ParameterizedFlowPermission> a1Set = getSources(a1);
        final Set<ParameterizedFlowPermission> a2Set = getSources(a2);
        return intersectSources(a1Set, a2Set);

    }
    public static Set<ParameterizedFlowPermission> intersectSources(Set<ParameterizedFlowPermission> a1Set,
            Set<ParameterizedFlowPermission> a2Set) {
        if(a1Set == null || a2Set == null) return new TreeSet<>();
       Set<ParameterizedFlowPermission> a1All =  convertAnytoAllSources(a1Set, false);
       Set<ParameterizedFlowPermission> a2All =   convertAnytoAllSources(a2Set, false);
       a1All.retainAll(a2All);
        return  convertToAnySource(a1All, false);
    }
    
    

    /**
     * Return the set of sinks that both annotations have.
     * If the intersection is all possible sinks, {ANY} is returned
     * @param a1 AnnotationMirror, could be {ANY}
     * @param a2 AnnotationMirror, could be {ANY}
     * @return intersection of a1 and a2
     */
    public static Set<ParameterizedFlowPermission> intersectSinks(AnnotationMirror a1, AnnotationMirror a2){
        final Set<ParameterizedFlowPermission> a1Set = getSinks(a1);
        final Set<ParameterizedFlowPermission> a2Set = getSinks(a2);
        return intersectSinks(a1Set, a2Set);
    }
    public static Set<ParameterizedFlowPermission> intersectSinks(Set<ParameterizedFlowPermission> a1Set,
            Set<ParameterizedFlowPermission> a2Set) {
        if(a1Set == null || a2Set == null) return new TreeSet<>();
        a1Set = convertAnyToAllSinks(a1Set, false);
        a2Set = convertAnyToAllSinks(a2Set, false);
        a1Set.retainAll(a2Set);
        return convertToAnySink(a1Set, false);
    }
    
    /**
     * Returns the union of a1 and a2.
     * If the union is {ANY, ...} then just {ANY} is returned
     * @param a1
     * @param a2
     * @return
     */
    public static Set<ParameterizedFlowPermission> unionSources(AnnotationMirror a1, AnnotationMirror a2){
        return unionSources(getSources(a1), getSources(a2));
    }
    public static Set<ParameterizedFlowPermission> unionSources(Set<ParameterizedFlowPermission> a1, Set<ParameterizedFlowPermission> a2){
        a1.addAll(a2);
        convertToAnySource(a1, true);
        return a1;
    }

    
    /**
     * Returns the union of a1 and a2.
     * If the union is {ANY, ...} then just {ANY} is returned
     * @param a1
     * @param a2
     * @return
     */
    public static Set<ParameterizedFlowPermission> unionSinks(AnnotationMirror a1, AnnotationMirror a2){
        return unionSinks(getSinks(a1), getSinks(a2));
    }
    /**
     * Returns the union of a1 and a2.
     * If the union is {ANY, ...} then just {ANY} is returned
     * @param a1
     * @param a2
     * @return
     */
    public static Set<ParameterizedFlowPermission> unionSinks(Set<ParameterizedFlowPermission> a1, Set<ParameterizedFlowPermission> a2){
        a1.addAll(a2);
        convertToAnySink(a1, true);
        return a1;
    }

    public static Set<ParameterizedFlowPermission> convertToParameterizedFlowPermission(Set<FlowPermission> permissions) {
        Set<ParameterizedFlowPermission> flowPermissions = new TreeSet<ParameterizedFlowPermission>();
        for (FlowPermission p : permissions) {
            flowPermissions.add(new ParameterizedFlowPermission(p));
        }
        return flowPermissions;
    }
    
    public static Set<FlowPermission> convertFromParameterizedFlowPermission(Set<ParameterizedFlowPermission> permissions) {
        Set<FlowPermission> coarsePermissions = new TreeSet<FlowPermission>();
        for (ParameterizedFlowPermission p : permissions) {
            coarsePermissions.add(p.getPermission());
        }
        return coarsePermissions;        
    }
}
