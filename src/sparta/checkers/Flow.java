package sparta.checkers;

import checkers.types.AnnotatedTypeMirror;

import javacutils.AnnotationUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.AnnotationMirror;

import sparta.checkers.quals.CoarseFlowPermission;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

public class Flow {
    private static FlowPermission ANY = new FlowPermission(CoarseFlowPermission.ANY);
    
    Set<FlowPermission> sources;
    Set<FlowPermission> sinks;

    public Flow(Set<FlowPermission> sources, Set<FlowPermission> sinks) {      
        this.sources = sources;
        this.sinks = sinks;
    }

    public Flow(FlowPermission source, Set<FlowPermission> sinks) {
        this.sources = new TreeSet<FlowPermission>();
        if (source != null) {
            sources.add(source);
        }
        this.sinks = convertToAnySink(sinks, false);
    }

    public Flow(Set<FlowPermission> sources, FlowPermission sink) {
        this.sources = convertToAnySource(sources, false);
        this.sinks = new TreeSet<FlowPermission>();
        sinks.add(sink);
    }

    public Flow(AnnotatedTypeMirror atm) {
        this.sinks = getSinks(atm);
        this.sources = getSources(atm);
    }

    public Flow() {
        this.sinks = new TreeSet<FlowPermission>();
        this.sources = new TreeSet<FlowPermission>();
    }

    public Flow(FlowPermission source) {
        this.sinks = new TreeSet<FlowPermission>();
        this.sources = new TreeSet<FlowPermission>();
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

    public void addSink(FlowPermission sink) {
        sinks.add(sink);
    }

    public boolean hasSink() {
        return !sinks.isEmpty();
    }

    public void addSink(Set<FlowPermission> sinks) {
        this.sinks.addAll(convertToAnySink(sinks, false));

    }

    public void addSource(FlowPermission source) {
        sources.add(source);
    }
    public void addSource(Set<FlowPermission> source) {
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

    public static Set<FlowPermission> getSinks(final AnnotatedTypeMirror type) {
        for(AnnotationMirror anno : type.getEffectiveAnnotations()){
            if(AnnotationUtils.areSameByClass(anno, Sink.class)){
               return getSinks(anno);
            }
        }
        return new TreeSet<FlowPermission>();
    }

    public static Set<FlowPermission> getSources(final AnnotatedTypeMirror type) {
        for(AnnotationMirror anno : type.getEffectiveAnnotations()){
            if(AnnotationUtils.areSameByClass(anno, Source.class)){
               return getSources( anno);
            }
        }
        return new TreeSet<FlowPermission>();
    }
    
    public static Set<FlowPermission> getSinks(final AnnotationMirror am) {
        if (am == null) {
            return new TreeSet<FlowPermission>();
        }

        Set<FlowPermission> sinkFlowPermissions = new TreeSet<FlowPermission>();
        List<CoarseFlowPermission> sinks = AnnotationUtils.getElementValueEnumArray(am, "value",
                CoarseFlowPermission.class, true);
        for (CoarseFlowPermission coarsePermission : sinks)
            sinkFlowPermissions.add(new FlowPermission(coarsePermission));
        Set<FlowPermission> retSet = convertToAnySink(sinkFlowPermissions, false);
        return retSet;
        
        /* Old implementation
         *
            if (am == null) {
                return new TreeSet<FlowPermission>();
            }
    
            List<FlowPermission> sinks = AnnotationUtils.getElementValueEnumArray(am, "value",
                    FlowPermission.class, true);
            Set<FlowPermission> set = convertToAnySink(new TreeSet<FlowPermission>(sinks), false);
            return set;
         */
    }

    public static Set<FlowPermission> getSources(final AnnotationMirror am) {
        if (am == null) {
            return new TreeSet<FlowPermission>();
        }

        Set<FlowPermission> sourceFlowPermissions = new TreeSet<FlowPermission>();
        List<CoarseFlowPermission> sinks = AnnotationUtils.getElementValueEnumArray(am, "value",
                CoarseFlowPermission.class, true);
        for (CoarseFlowPermission coarsePermission : sinks)
            sourceFlowPermissions.add(new FlowPermission(coarsePermission));
        Set<FlowPermission> retSet = convertToAnySource(sourceFlowPermissions, false);
        return retSet;
        
        /* Old Implementation
        if (am == null) {
            return new TreeSet<FlowPermission>();
        }
        List<FlowPermission> sources = AnnotationUtils.getElementValueEnumArray(am, "value",
                FlowPermission.class, true);
        Set<FlowPermission> set = convertToAnySource(new TreeSet<FlowPermission>(sources),
                false);
        return set;
        */
    }

    /**
     * Replace ANY with the list of all possible sinks
     * @param sinks
     * @param inPlace
     * @return
     */
    private static Set<FlowPermission> convertAnyToAllSinks(final Set<FlowPermission> sinks,
            boolean inPlace) {
        
        // TODO: Check and make sure .contains still works with static ANY, and change when confirmed
        boolean addAll = false;
        final Set<FlowPermission> retSet = (inPlace) ? sinks : new TreeSet<FlowPermission>(sinks);
        if (sinks.contains(ANY)) {
            retSet.addAll(getSetOfAllSinks());
            retSet.remove(ANY);
        }
        return retSet;
    }
    
        /*
         * First implementation
         
            Set<FlowPermission> allSinks = getSetOfAllSinks();
            for (FlowPermission fp : retSet) {
                if (fp.getPermission() == CoarseFlowPermission.ANY) {
                    addAll = true;
                }
                allSinks.remove(fp.getPermission());
            }
            if (addAll) {
                for (FlowPermission permissionToAdd : allSinks) {
                    retSet.add(permissionToAdd);
                }
            }
            return retSet;
        */
        
        /*
           OLD IMPLEMENTATION
           if (retSet.contains(FlowPermission.ANY)) {
               retSet.addAll(getSetOfAllSinks());
               retSet.remove(FlowPermission.ANY);
           }
        */

    /**
     * Replace ANY with the list of all possible sources
     * @param sources
     * @param inPlace
     * @return
     */
    private static Set<FlowPermission> convertAnytoAllSources(final Set<FlowPermission> sources,
            boolean inPlace) {
        boolean addAll = false;
        final Set<FlowPermission> retSet = (inPlace) ? sources : new TreeSet<FlowPermission>(
                sources);
        if (sources.contains(ANY)) {
            retSet.addAll(getSetOfAllSinks());
            retSet.remove(ANY);
        }
        return retSet;
    }
        
        /* First implementation
            Set<FlowPermission> allSources = getSetOfAllSinks();
            for (FlowPermission fp : retSet) {
                if (fp.getPermission() == CoarseFlowPermission.ANY) {
                    addAll = true;
                }
                allSources.remove(fp.getPermission());
            }
            if (addAll) {
                for (FlowPermission permissionToAdd : allSources) {
                    retSet.add(permissionToAdd);
                }
            }
        */ 
           
        /*
         ORIGINAL IMPLEMENTATION
        if (retSet.contains(FlowPermission.ANY)) {
            retSet.addAll(getSetOfAllSources());
            retSet.remove(FlowPermission.ANY);
        }
        */


    /**
     * All possible sources, excluding ANY
     * TODO: This returns all sources and sinks, not just sources...fix this
     * @return
     */
    public static Set<FlowPermission> getSetOfAllSources() {
        List<CoarseFlowPermission> coarseFlowList = Arrays.asList(CoarseFlowPermission.values());
        Set<FlowPermission> flowPermissionSet = new TreeSet<>();
        for (CoarseFlowPermission permission : coarseFlowList) {
            if (permission != CoarseFlowPermission.ANY) {
                flowPermissionSet.add(new FlowPermission(permission)); 
            }
        }
        return flowPermissionSet;
        
        /* Old implementation
            Set<FlowPermission> set = new TreeSet<>(Arrays.asList(CoarseFlowPermission.values()));
            set.remove(CoarseFlowPermission.ANY);
            return set;
        */
    }

    /**
     * All possible sinks, excluding ANY
     * TODO: This returns all sources and sinks, not just sinks...fix this
     * @return
     */
    public static Set<FlowPermission> getSetOfAllSinks() {
        List<CoarseFlowPermission> coarseFlowList = Arrays.asList(CoarseFlowPermission.values());
        // TODO: Ask why this doesn't work
        // coarseFlowList.remove(CoarseFlowPermission.ANY);
        Set<FlowPermission> flowPermissionSet = new TreeSet<>();
        for (CoarseFlowPermission permission : coarseFlowList) {
            if (permission != CoarseFlowPermission.ANY) {
                flowPermissionSet.add(new FlowPermission(permission)); 
            }
        }
        return flowPermissionSet;
        
        /* Old implementation
            Set<FlowPermission> set = new TreeSet<>(Arrays.asList(CoarseFlowPermission.values()));
            set.remove(CoarseFlowPermission.ANY);
            return set;
         */
    }

    /**
     * If sources contains all possible sources, then return ANY.
     * If sources contains ANY and some other sources, then return ANY
     * @param sources
     * @param inPlace
     * @return
     */
    public static Set<FlowPermission> convertToAnySource(final Set<FlowPermission> sources,
            boolean inPlace) {
        final Set<FlowPermission> retSet = (inPlace) ? sources : new TreeSet<FlowPermission>(sources);
        Set<CoarseFlowPermission> coarsePermissions = new TreeSet<CoarseFlowPermission>();
        for (FlowPermission flowPermission : sources) {
            CoarseFlowPermission current = flowPermission.getPermission();
            if (current == CoarseFlowPermission.ANY) {
                retSet.clear();
                retSet.add(new FlowPermission(CoarseFlowPermission.ANY));
                return retSet;
            }
            coarsePermissions.add(current);
        }
        if (coarsePermissions.equals(getSetOfAllSources())) {
            retSet.clear();
            retSet.add(new FlowPermission(CoarseFlowPermission.ANY, null));
        }
        return retSet;
        
        /* Old implementation
        if(sources.equals(getSetOfAllSources())) {
            retSet.clear();
            retSet.add(FlowPermission.ANY);
        }else if(retSet.contains(FlowPermission.ANY)){
            retSet.clear();
            retSet.add(FlowPermission.ANY);
        }
        return retSet;
        */
    }

    /**
     * If sinks contains all possible sinks, then return ANY.
     * If sinks contains ANY and some other sinks, then return ANY
     * @param sinks
     * @param inPlace
     * @return either {ANY} or sinks
     */
    public static Set<FlowPermission> convertToAnySink(final Set<FlowPermission> sinks, boolean inPlace) {
        final Set<FlowPermission> retSet = (inPlace) ? sinks : new TreeSet<FlowPermission>(sinks);
        Set<CoarseFlowPermission> coarsePermissions = new TreeSet<CoarseFlowPermission>();
        if(sinks.equals(getSetOfAllSinks())) {
            retSet.clear();
            retSet.add(ANY);
        }else if(retSet.contains(ANY)){
            retSet.clear();
            retSet.add(ANY);
        }
        return retSet;
    }
        /* First implementation
        for (FlowPermission flowPermission : sinks) {
            CoarseFlowPermission current = flowPermission.getPermission();
            if (current == CoarseFlowPermission.ANY) {
                retSet.clear();
                retSet.add(new FlowPermission(CoarseFlowPermission.ANY));
                return retSet;
            }
            coarsePermissions.add(current);
        }
        if (coarsePermissions.equals(getSetOfAllSinks())) {
            retSet.clear();
            retSet.add(new FlowPermission(CoarseFlowPermission.ANY, null));
        }
        return retSet;
        */
        
        /*
        if(sinks.equals(getSetOfAllSinks())) {
            retSet.clear();
            retSet.add(FlowPermission.ANY);
        }else if(retSet.contains(FlowPermission.ANY)){
            retSet.clear();
            retSet.add(FlowPermission.ANY);
        }
        return retSet;
        */

    public static boolean isTop(AnnotatedTypeMirror atm) {
        Set<FlowPermission> sources = getSources(atm);
        Set<FlowPermission> sinks = getSinks(atm);
        return sources.contains(ANY) && sinks.isEmpty();
    }
    public static boolean isBottom(AnnotatedTypeMirror atm) {
        Set<FlowPermission> sources = getSources(atm);
        Set<FlowPermission> sinks = getSinks(atm);
        return sinks.contains(ANY) && sources.isEmpty();
    }
    /**
     * Return the set of sources that both annotations have.
     * If the intersection is all possible sources, {ANY} is returned
     * @param a1 AnnotationMirror, could be {ANY}
     * @param a2 AnnotationMirror, could be {ANY}
     * @return intersection of a1 and a2
     */
    public static Set<FlowPermission> intersectSources(AnnotationMirror a1, AnnotationMirror a2){
        final Set<FlowPermission> a1Set = getSources(a1);
        final Set<FlowPermission> a2Set = getSources(a2);
        return intersectSources(a1Set, a2Set);

    }
    public static Set<FlowPermission> intersectSources(Set<FlowPermission> a1Set,
            Set<FlowPermission> a2Set) {
        if(a1Set == null || a2Set == null) return new TreeSet<>();
        convertAnytoAllSources(a1Set, true);
        convertAnytoAllSources(a2Set, true);
        a1Set.retainAll(a2Set);
        convertToAnySource(a1Set, true);
        return a1Set;
    }

    /**
     * Return the set of sinks that both annotations have.
     * If the intersection is all possible sinks, {ANY} is returned
     * @param a1 AnnotationMirror, could be {ANY}
     * @param a2 AnnotationMirror, could be {ANY}
     * @return intersection of a1 and a2
     */
    public static Set<FlowPermission> intersectSinks(AnnotationMirror a1, AnnotationMirror a2){
        final Set<FlowPermission> a1Set = getSinks(a1);
        final Set<FlowPermission> a2Set = getSinks(a2);
        return intersectSinks(a1Set, a2Set);
    }
    public static Set<FlowPermission> intersectSinks(Set<FlowPermission> a1Set,
            Set<FlowPermission> a2Set) {
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
    public static Set<FlowPermission> unionSources(AnnotationMirror a1, AnnotationMirror a2){
        return unionSources(getSources(a1), getSources(a2));
    }
    public static Set<FlowPermission> unionSources(Set<FlowPermission> a1, Set<FlowPermission> a2){
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
    public static Set<FlowPermission> unionSinks(AnnotationMirror a1, AnnotationMirror a2){
        return unionSinks(getSinks(a1), getSinks(a2));
    }
    /**
     * Returns the union of a1 and a2.
     * If the union is {ANY, ...} then just {ANY} is returned
     * @param a1
     * @param a2
     * @return
     */
    public static Set<FlowPermission> unionSinks(Set<FlowPermission> a1, Set<FlowPermission> a2){
        a1.addAll(a2);
        convertToAnySink(a1, true);
        return a1;
    }

    public static Set<FlowPermission> convertCoarseToFlowPermission(Set<CoarseFlowPermission> permissions) {
        Set<FlowPermission> flowPermissions = new TreeSet<FlowPermission>();
        for (CoarseFlowPermission p : permissions) {
            flowPermissions.add(new FlowPermission(p));
        }
        return flowPermissions;
    }
    
    public static Set<CoarseFlowPermission> convertFlowToCoarsePermission(Set<FlowPermission> permissions) {
        Set<CoarseFlowPermission> coarsePermissions = new TreeSet<CoarseFlowPermission>();
        for (FlowPermission p : permissions) {
            coarsePermissions.add(p.getPermission());
        }
        return coarsePermissions;        
    }
}
