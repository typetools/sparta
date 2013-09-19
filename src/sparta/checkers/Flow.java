package sparta.checkers;

import checkers.types.AnnotatedTypeMirror;

import javacutils.AnnotationUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

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
        this.sinks = convertToAnySink(sinks, false);
    }

    public Flow(Set<FlowPermission> sources, FlowPermission sink) {
        this.sources = convertToAnySource(sources, false);
        this.sinks = new HashSet<FlowPermission>();
        sinks.add(sink);
    }

    public Flow(AnnotatedTypeMirror atm) {
        this.sinks = getSinks(atm);
        this.sources = getSources(atm);
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
        return new HashSet<FlowPermission>();
    }

    public static Set<FlowPermission> getSources(final AnnotatedTypeMirror type) {
        for(AnnotationMirror anno : type.getEffectiveAnnotations()){
            if(AnnotationUtils.areSameByClass(anno, Source.class)){
               return getSources( anno);
            }
        }
        return new HashSet<FlowPermission>();
    }

    public static Set<FlowPermission> getSinks(final AnnotationMirror am) {
        if (am == null) {
            return new HashSet<FlowPermission>();
        }

        List<FlowPermission> sinks = AnnotationUtils.getElementValueEnumArray(am, "value",
                FlowPermission.class, true);
        Set<FlowPermission> set = convertToAnySink(new HashSet<FlowPermission>(sinks), false);
        return set;
    }

    public static Set<FlowPermission> getSources(final AnnotationMirror am) {
        if (am == null) {
            return new HashSet<FlowPermission>();
        }
        List<FlowPermission> sources = AnnotationUtils.getElementValueEnumArray(am, "value",
                FlowPermission.class, true);
        Set<FlowPermission> set = convertToAnySource(new HashSet<FlowPermission>(sources),
                false);
        return set;
    }

    /**
     * Replace ANY with the list of all possible sinks
     * @param sinks
     * @param inPlace
     * @return
     */
    private static Set<FlowPermission> convertAnyToAllSinks(final Set<FlowPermission> sinks,
            boolean inPlace) {
        final Set<FlowPermission> retSet = (inPlace) ? sinks : new HashSet<FlowPermission>(sinks);
        if (retSet.contains(FlowPermission.ANY)) {
            retSet.addAll(getSetOfAllSinks());
            retSet.remove(FlowPermission.ANY);
        }
        return retSet;
    }

    /**
     * Replace ANY with the list of all possible sources
     * @param sources
     * @param inPlace
     * @return
     */
    private static Set<FlowPermission> convertAnytoAllSources(final Set<FlowPermission> sources,
            boolean inPlace) {
        final Set<FlowPermission> retSet = (inPlace) ? sources : new HashSet<FlowPermission>(
                sources);
        if (retSet.contains(FlowPermission.ANY)) {
            retSet.addAll(getSetOfAllSources());
            retSet.remove(FlowPermission.ANY);
        }
        return retSet;
    }


    /**
     * All possible sources, excluding ANY
     * TODO: This returns all sources and sinks, not just sources...fix this
     * @return
     */
    public static Set<FlowPermission> getSetOfAllSources() {
        Set<FlowPermission> set = new HashSet<>(Arrays.asList(FlowPermission.values()));
        set.remove(FlowPermission.ANY);
        return set;
    }

    /**
     * All possible sinks, excluding ANY
     * TODO: This returns all sources and sinks, not just sinks...fix this
     * @return
     */
    public static Set<FlowPermission> getSetOfAllSinks() {
        Set<FlowPermission> set = new HashSet<>(Arrays.asList(FlowPermission.values()));
        set.remove(FlowPermission.ANY);
        return set;
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
        final Set<FlowPermission> retSet = (inPlace) ? sources : new HashSet<FlowPermission>(sources);
        if(sources.equals(getSetOfAllSources())) {
            retSet.clear();
            retSet.add(FlowPermission.ANY);
        }else if(retSet.contains(FlowPermission.ANY)){
            retSet.clear();
            retSet.add(FlowPermission.ANY);
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
    public static Set<FlowPermission> convertToAnySink(final Set<FlowPermission> sinks, boolean inPlace) {
            final Set<FlowPermission> retSet = (inPlace) ? sinks : new HashSet<FlowPermission>(sinks);
            if(sinks.equals(getSetOfAllSinks())) {
                retSet.clear();
                retSet.add(FlowPermission.ANY);
            }else if(retSet.contains(FlowPermission.ANY)){
                retSet.clear();
                retSet.add(FlowPermission.ANY);
            }
            return retSet;
    }

    public static boolean isTop(AnnotatedTypeMirror atm) {
        Set<FlowPermission> sources = getSources(atm);
        Set<FlowPermission> sinks = getSinks(atm);
        return sources.contains(FlowPermission.ANY) && sinks.isEmpty();
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
        if(a1Set == null || a2Set == null) return new HashSet<>();
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
        if(a1Set == null || a2Set == null) return new HashSet<>();
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
        Set<FlowPermission> superset = getSources(a1);
        superset.addAll(getSources(a2));
        convertToAnySource(superset, true);
        return superset;
    }
    /**
     * Returns the union of a1 and a2.
     * If the union is {ANY, ...} then just {ANY} is returned
     * @param a1
     * @param a2
     * @return
     */
    public static Set<FlowPermission> unionSinks(AnnotationMirror a1, AnnotationMirror a2){
        Set<FlowPermission> superset = getSinks(a1);
        superset.addAll(getSinks(a2));
        convertToAnySink(superset, true);
        return superset;
    }
}
