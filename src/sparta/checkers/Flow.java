package sparta.checkers;

import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.AnnotationMirror;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PFPermission;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

public class Flow {
    private static PFPermission ANY = new PFPermission(FlowPermission.ANY);
    
    Set<PFPermission> sources;
    Set<PFPermission> sinks;

    public Flow(Set<PFPermission> sources, Set<PFPermission> sinks) {      
        this.sources = sources;
        this.sinks = sinks;
    }

    public Flow(PFPermission source, Set<PFPermission> sinks) {
        this.sources = new TreeSet<PFPermission>();
        if (source != null) {
            sources.add(source);
        }
        this.sinks = convertToAnySink(sinks, false);
    }

    public Flow(Set<PFPermission> sources, PFPermission sink) {
        this.sources = convertToAnySource(sources, false);
        this.sinks = new TreeSet<PFPermission>();
        sinks.add(sink);
    }

    public Flow(AnnotatedTypeMirror atm) {
        this.sinks = getSinks(atm);
        this.sources = getSources(atm);
    }

    public Flow() {
        this.sinks = new TreeSet<PFPermission>();
        this.sources = new TreeSet<PFPermission>();
    }

    public Flow(PFPermission source) {
        this.sinks = new TreeSet<PFPermission>();
        this.sources = new TreeSet<PFPermission>();
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

    public void addSink(PFPermission sink) {
        sinks.add(sink);
    }

    public boolean hasSink() {
        return !sinks.isEmpty();
    }

    public void addSink(Set<PFPermission> sinks) {
        this.sinks.addAll(convertToAnySink(sinks, false));

    }

    public void addSource(PFPermission source) {
        sources.add(source);
    }
    public void addSource(Set<PFPermission> source) {
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

    public static Set<PFPermission> getSinks(final AnnotatedTypeMirror type) {
        for(AnnotationMirror anno : type.getEffectiveAnnotations()){
            if(AnnotationUtils.areSameByClass(anno, Sink.class)){
                return getSinks(anno);
            }
        }
        return new TreeSet<PFPermission>();
    }

    public static Set<PFPermission> getSources(final AnnotatedTypeMirror type) {
        for(AnnotationMirror anno : type.getEffectiveAnnotations()){
            if(AnnotationUtils.areSameByClass(anno, Source.class)){
                return getSources(anno);
            }
        }
        return new TreeSet<PFPermission>();
    }
    
    public static Set<PFPermission> getSinks(final AnnotationMirror am) {
        if (am == null) {
            return new TreeSet<PFPermission>();
        }
        List<String> sinks = AnnotationUtils.getElementValueArray(am, "value",
                String.class, true);
        Set<PFPermission> sinkFlowPermissions = new TreeSet<PFPermission>();
        for (String permissionString : sinks) {
            sinkFlowPermissions.add(PFPermission.convertStringToPFPermission(permissionString));
        }

        return convertToAnySink(sinkFlowPermissions, false);
    }

    public static Set<PFPermission> getSources(final AnnotationMirror am) {
        if (am == null) {
            return new TreeSet<PFPermission>();
        }

        List<String> sources = AnnotationUtils.getElementValueArray(am,
                "value", String.class, true);
        Set<PFPermission> sourceFlowPermissions = new TreeSet<PFPermission>();
        for (String permissionString : sources) {
            sourceFlowPermissions.add(PFPermission.convertStringToPFPermission(permissionString));
        }

        return convertToAnySource(sourceFlowPermissions, false);
    }
    
    /**
     * Replace ANY with the list of all possible sinks
     * @param sinks
     * @param inPlace
     * @return
     */
    private static Set<PFPermission> convertAnyToAllSinks(final Set<PFPermission> sinks,
            boolean inPlace) {
        final Set<PFPermission> retSet = (inPlace) ? sinks : new TreeSet<PFPermission>(sinks);
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
    private static Set<PFPermission> convertAnytoAllSources(final Set<PFPermission> sources,
            boolean inPlace) {
        final Set<PFPermission> retSet = (inPlace) ? sources : new TreeSet<PFPermission>(
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
    public static Set<PFPermission> getSetOfAllSources() {
        if(setOfAllSources.isEmpty()){
            List<FlowPermission> coarseFlowList = Arrays.asList(FlowPermission.values());
            for (FlowPermission permission : coarseFlowList) {
                if (permission != FlowPermission.ANY) {
                    setOfAllSources.add(new PFPermission(permission)); 
                }
            } 
        }
        return setOfAllSources;
    }
    static Set<PFPermission> setOfAllSources = new TreeSet<>();

    /**
     * All possible sinks, excluding ANY
     * TODO: This returns all sources and sinks, not just sinks...fix this
     * @return
     */
    public static Set<PFPermission> getSetOfAllSinks() {
        if (setOfAllSinks.isEmpty()) {
            List<FlowPermission> coarseFlowList = Arrays.asList(FlowPermission
                    .values());
            for (FlowPermission permission : coarseFlowList) {
                if (permission != FlowPermission.ANY) {
                    setOfAllSinks.add(new PFPermission(permission));
                }
            }
        }
        return setOfAllSinks;
    }

    static Set<PFPermission> setOfAllSinks = new TreeSet<>();


    /**
     * If sources contains all possible sources, then return ANY.
     * If sources contains ANY and some other sources, then return ANY
     * @param sources
     * @param inPlace
     * @return
     */
    public static Set<PFPermission> convertToAnySource(final Set<PFPermission> sources,
            boolean inPlace) {
        final Set<PFPermission> retSet = (inPlace) ? sources : new TreeSet<PFPermission>(sources);
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
    public static Set<PFPermission> convertToAnySink(final Set<PFPermission> sinks, boolean inPlace) {
        final Set<PFPermission> retSet = (inPlace) ? sinks : new TreeSet<PFPermission>(sinks);
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
        Set<PFPermission> sources = getSources(atm);
        Set<PFPermission> sinks = getSinks(atm);
        return sources.contains(ANY) && sinks.isEmpty();
    }
    public static boolean isBottom(AnnotatedTypeMirror atm) {
        Set<PFPermission> sources = getSources(atm);
        Set<PFPermission> sinks = getSinks(atm);
        return sinks.contains(ANY) && sources.isEmpty();
    }
    /**
     * Return the set of sources that both annotations have.
     * If the intersection is all possible sources, {ANY} is returned
     * @param a1 AnnotationMirror, could be {ANY}
     * @param a2 AnnotationMirror, could be {ANY}
     * @return intersection of a1 and a2
     */
    public static Set<PFPermission> intersectSources(AnnotationMirror a1, AnnotationMirror a2){
        final Set<PFPermission> a1Set = getSources(a1);
        final Set<PFPermission> a2Set = getSources(a2);
        return intersectSources(a1Set, a2Set);

    }
    public static Set<PFPermission> intersectSources(Set<PFPermission> a1Set,
            Set<PFPermission> a2Set) {
        if(a1Set == null || a2Set == null) return new TreeSet<>();
       Set<PFPermission> retSet = new TreeSet<PFPermission>();
       Set<PFPermission> a1All =  convertAnytoAllSources(a1Set, false);
       Set<PFPermission> a2All =   convertAnytoAllSources(a2Set, false);
       for (PFPermission a1permission : a1All) {
           for (PFPermission a2permission : a2All) {
               // Match permission and match all parameters such that a2 is subsumed in a1
               if (a1permission.getPermission() == a2permission.getPermission() && 
                   FlowAnnotatedTypeFactory.allParametersMatch(a1permission.getParameters(), a2permission.getParameters())) {
                   retSet.add(a2permission);
               }
           }
       }
        return  convertToAnySource(retSet, false);
    }
    
    

    /**
     * Return the set of sinks that both annotations have.
     * If the intersection is all possible sinks, {ANY} is returned
     * @param a1 AnnotationMirror, could be {ANY}
     * @param a2 AnnotationMirror, could be {ANY}
     * @return intersection of a1 and a2
     */
    public static Set<PFPermission> intersectSinks(AnnotationMirror a1, AnnotationMirror a2){
        final Set<PFPermission> a1Set = getSinks(a1);
        final Set<PFPermission> a2Set = getSinks(a2);
        return intersectSinks(a1Set, a2Set);
    }
    public static Set<PFPermission> intersectSinks(Set<PFPermission> a1Set,
            Set<PFPermission> a2Set) {
        if(a1Set == null || a2Set == null) return new TreeSet<>();
        Set<PFPermission> retSet = new TreeSet<PFPermission>();
        a1Set = convertAnyToAllSinks(a1Set, false);
        a2Set = convertAnyToAllSinks(a2Set, false);
        for (PFPermission a1permission : a1Set) {
            for (PFPermission a2permission : a2Set) {
                // Match permission and match all parameters such that a2 is subsumed in a1
                if (a1permission.getPermission() == a2permission.getPermission() && 
                    FlowAnnotatedTypeFactory.allParametersMatch(a2permission.getParameters(), a1permission.getParameters())) {
                    retSet.add(a2permission);
                }
            }
        }
        return convertToAnySink(retSet, false);
    }
    
    /**
     * Returns the union of a1 and a2.
     * If the union is {ANY, ...} then just {ANY} is returned
     * @param a1
     * @param a2
     * @return
     */
    public static Set<PFPermission> unionSources(AnnotationMirror a1, AnnotationMirror a2){
        return unionSources(getSources(a1), getSources(a2));
    }
    public static Set<PFPermission> unionSources(Set<PFPermission> a1, Set<PFPermission> a2){
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
    public static Set<PFPermission> unionSinks(AnnotationMirror a1, AnnotationMirror a2){
        return unionSinks(getSinks(a1), getSinks(a2));
    }
    /**
     * Returns the union of a1 and a2.
     * If the union is {ANY, ...} then just {ANY} is returned
     * @param a1
     * @param a2
     * @return
     */
    public static Set<PFPermission> unionSinks(Set<PFPermission> a1, Set<PFPermission> a2){
        a1.addAll(a2);
        convertToAnySink(a1, true);
        return a1;
    }

    public static Set<PFPermission> convertToParameterizedFlowPermission(Set<FlowPermission> permissions) {
        Set<PFPermission> flowPermissions = new TreeSet<PFPermission>();
        for (FlowPermission p : permissions) {
            flowPermissions.add(new PFPermission(p));
        }
        return flowPermissions;
    }
    
    public static Set<FlowPermission> convertFromParameterizedFlowPermission(Set<PFPermission> permissions) {
        Set<FlowPermission> coarsePermissions = new TreeSet<FlowPermission>();
        for (PFPermission p : permissions) {
            coarsePermissions.add(p.getPermission());
        }
        return coarsePermissions;        
    }
}
