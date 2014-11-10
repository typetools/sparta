package sparta.checkers.poly;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import org.checkerframework.common.value.qual.StringVal;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

import sparta.checkers.Flow;
import sparta.checkers.FlowAnnotatedTypeFactory;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.InferPermissionParameter;
import sparta.checkers.quals.PFPermission;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;

/**
 * This class implements parameterized permission polymorphism a.k.a local
 * inference for permission parameters.
 */
public class ParameterizedPermissonPolymorphism {

    private FlowAnnotatedTypeFactory atypeFactory;

    public ParameterizedPermissonPolymorphism(ProcessingEnvironment env,
            FlowAnnotatedTypeFactory factory) {
        this.atypeFactory = factory;
    }

    /**
     * Infers the parameter for the return type permission.
     * 
     * @param tree
     * @param type
     */
    public void annotate(MethodInvocationTree tree, AnnotatedExecutableType type) {
        inferParameters(type, tree.getArguments(),
                TreeUtils.getReceiverTree(tree));
    }

    /**
     * Infers the parameter for the result type permission.
     * 
     * @param tree
     * @param type
     */
    public void annotate(NewClassTree tree, AnnotatedExecutableType type) {
        inferParameters(type, tree.getArguments(),
                TreeUtils.getReceiverTree(tree));
    }

    private void inferParameters(AnnotatedExecutableType execType,
            List<? extends ExpressionTree> treeArgs, Tree receiver) {

        InferedPermInfo info = new InferedPermInfo(execType.getElement());
        if (info.hasInferedPermInfo()) {
            List<String> values = getValuesFromArguments(info, treeArgs,
                    receiver);
            addPermissionParameters(values, execType, info);
        }
    }

    private void addPermissionParameters(List<String> values,
            AnnotatedExecutableType execType, InferedPermInfo info) {
        if (info.flowPermission == null || values == null || values.isEmpty())
            return;
        // Add strings to return type parameters
        AnnotatedTypeMirror atm = execType.getReturnType();
        if (atm == null)
            return;
        if (info.inferSource || info.inferBoth) {
            Set<PFPermission> sources = Flow.getSources(atm);

            for (PFPermission pfp : sources) {
                if (pfp.getPermission() == info.flowPermission) {
                    pfp.removeStar();
                    pfp.addParameters(values);
                }
            }

            atm.replaceAnnotation(atypeFactory.createAnnoFromSource(sources));

            // The refined source may be able to flow to more sinks,
            // so recalculate the sinks.
            // For example, if the type was @Source(FILE) @Sink(A)
            // and the flow policy is:
            // FILE(*) -> A
            // FILE(f) -> B
            // Then the new type is @Source(FILE(f)) @Sink({A,B})
            if (!info.inferBoth) {
                Set<PFPermission> sinks = atypeFactory
                        .getFlowPolicy().getIntersectionAllowedSinks(sources);
                atm.replaceAnnotation(atypeFactory.createAnnoFromSink(sinks));
            }
        }
        if (info.inferSink || info.inferBoth) {
            Set<PFPermission> sinks = Flow.getSinks(atm);
            for (PFPermission pfp : sinks) {
                if (pfp.getPermission() == info.flowPermission) {
                    pfp.removeStar();
                    pfp.addParameters(values);
                }
            }
            atm.replaceAnnotation(atypeFactory.createAnnoFromSink(sinks));

            // More sources may be allowed to flow to the widen sink,
            // so recalculate the sources.
            // For example, if the type was @Source(A) @Sink(FILE)
            // and the flow policy is:
            // A->FILE(*)
            // B->FILE(f)
            // Then the new type is @Source({A,B}) @Sink(FILE(f))
            if (!info.inferBoth) {
                Set<PFPermission> sources = atypeFactory
                        .getFlowPolicy().getIntersectionAllowedSources(sinks);
                atm.replaceAnnotation(atypeFactory
                        .createAnnoFromSource(sources));
            }
        }
    }

    /**
     * Gets the list of strings associated with the AnnotationMirror's parameter
     * list.
     */
    private List<String> getValuesFromArguments(InferedPermInfo info,
            List<? extends ExpressionTree> list, Tree receiver) {
        List<String> values = new ArrayList<>();
        for (Integer paramIndex : info.paramIndexs) {
            List<String> newValues = new ArrayList<>();
            if (paramIndex != 0) {
                ExpressionTree treeParm = list.get(paramIndex - 1);
                newValues = getValues(info, treeParm);
            } else {
                if (receiver != null) {
                    newValues = getValues(info, receiver);
                }
            }
            if (values.isEmpty())
                values = newValues;
            else
                values = appendCartesian(values, newValues, info.seperator);
        }
        return values;
    }

    private List<String> getValues(InferedPermInfo info, Tree tree) {
        List<String> values = getStringValues(tree);
        if (values.isEmpty()) {
            values = getValuesFromPermission(info, tree);
        }
        return values;
    }

    /**
     * If the tree's source or sink annotation has the correct flow permission
     * with a parameter, return those parameters
     * 
     * @param info
     * @param tree
     * @return
     */
    private List<String> getValuesFromPermission(InferedPermInfo info, Tree tree) {
        AnnotatedTypeMirror atm = atypeFactory.getAnnotatedType(tree);

        Set<PFPermission> perms;
        if (info.inferSource) {
            perms = Flow.getSources(atm);
        } else {
            perms = Flow.getSinks(atm);
        }
        List<String> values = new ArrayList<String>();
        for (PFPermission perm : perms) {
            if (perm.getPermission().equals(info.flowPermission)) {
                values = perm.getParameters();
                if (values.contains("*")) {
                    values = new ArrayList<String>();
                }
                break;
            }
        }
        return values;
    }

    private List<String> appendCartesian(List<String> list1,
            List<String> list2, String seperator) {
        List<String> product = new ArrayList<>();
        for (String s1 : list1) {
            for (String s2 : list2) {
                product.add(s1 + seperator + s2);
            }
        }
        return product;
    }

    private List<String> getStringValues(Tree receiver) {
        List<String> values = new ArrayList<>();
        AnnotationMirror treeAM = atypeFactory.getAnnotationMirror(receiver,
                StringVal.class);
        if (treeAM != null) {
            values = AnnotationUtils.getElementValueArray(treeAM, "value",
                    String.class, true);
        }
        return values;
    }

    /**
     * Retrieves and stores all information in an @InferPermissionParameter
     */
    class InferedPermInfo {
        String seperator;
        List<Integer> paramIndexs = null;
        FlowPermission flowPermission = null;
        boolean inferSource = false;
        boolean inferSink = false;
        boolean inferBoth = false;

        public InferedPermInfo(Element element) {
            AnnotationMirror am = atypeFactory.getDeclAnnotation(element,
                    InferPermissionParameter.class);
            if (am != null) {
                String perm = AnnotationUtils.getElementValue(am,
                        "value", String.class, false);
                flowPermission = PFPermission.convertStringToPFPermission(perm).getPermission();
                paramIndexs = AnnotationUtils.getElementValueArray(am, "param",
                        Integer.class, true);

                String isA = AnnotationUtils.getElementValue(am, "isA",
                        String.class, true);
                inferSource = isA.equalsIgnoreCase("source");
                inferSink = isA.equalsIgnoreCase("sink");
                inferBoth = isA.equalsIgnoreCase("both");
                seperator = AnnotationUtils.getElementValue(am, "seperator",
                        String.class, true);
            }
            if (seperator == "")
                seperator = getSeperator(flowPermission);
        }

        private String getSeperator(FlowPermission flowPermission) {
            String sep = sepMap.get(flowPermission);
            if (sep == null)
                sep = "";
            return sep;
        }

        /**
         * Returns true if element pass to construct this object did not have an @InferPermissionParameter
         * annotation Otherwise, returns false
         * 
         * @return
         */
        public boolean hasInferedPermInfo() {
            return paramIndexs != null && flowPermission != null;
        }

        public List<Integer> getParamIndexs() {
            return paramIndexs;
        }

        public FlowPermission getFlowPermission() {
            return flowPermission;
        }

        public boolean isInferSource() {
            return inferSource;
        }

    }

    static Map<FlowPermission, String> sepMap = new HashMap<>();
    {
        sepMap.put(FlowPermission.FILESYSTEM, File.separator);
    }
}