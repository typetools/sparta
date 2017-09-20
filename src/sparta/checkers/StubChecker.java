package sparta.checkers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedWildcardType;
import org.checkerframework.framework.type.visitor.AnnotatedTypeScanner;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.Pair;

import sparta.checkers.quals.AddsSourceData;
import sparta.checkers.quals.PFPermission;

/**
 * This class is used to check that the stub files have the following
 * properties:
 *
 * 1. @AddsSourceData are well formed. Namely that all methods with @Source
 * annotations on parameters or receivers have an @AddsSourceData annotation
 * with the correct parameter index listed.
 *
 * To turn on, see comment in FlowAnnotatedTypeFactory.postInit().
 */
public class StubChecker {

    private BaseTypeChecker checker;
    private Map<String, Set<AnnotationMirror>> indexDeclAnnos;
    private Map<Element, AnnotatedTypeMirror> indexTypes;
    private AnnotatedTypeFactory factory;
    private ProcessingEnvironment env;

    public static void checkStubs(
            Map<String, Set<AnnotationMirror>> indexDeclAnnos,
            Map<Element, AnnotatedTypeMirror> indexTypes,
            BaseTypeChecker checker, AnnotatedTypeFactory factory,
            ProcessingEnvironment env) {

        StubChecker stub = new StubChecker(checker, factory, env,
                indexDeclAnnos, indexTypes);
        stub.checkAddsSourceData();
    }

    public StubChecker(BaseTypeChecker checker, AnnotatedTypeFactory factory,
            ProcessingEnvironment env,
            Map<String, Set<AnnotationMirror>> indexDeclAnnos,
            Map<Element, AnnotatedTypeMirror> indexTypes) {
        this.checker = checker;
        this.factory = factory;
        this.indexDeclAnnos = indexDeclAnnos;
        this.indexTypes = indexTypes;
        this.env = env;

    }

    Map<String, List<Pair<Element, String>>> elementMap = new HashMap<>();

    public void checkAddsSourceData() {
        for (Element ele : indexTypes.keySet()) {
            if (ele.getKind() == ElementKind.METHOD
                    || ele.getKind() == ElementKind.CONSTRUCTOR) {
                AnnotatedTypeMirror atm = indexTypes.get(ele);
                List<Integer> paramIndex = getParamIndexWithSource((AnnotatedExecutableType) atm);
                if (paramIndex != null
                        && !existingAddSourceIsCorrect(paramIndex, ele)) {
                    addToMap(ele, createAddSource(paramIndex));
                }
            }
        }
        printMap();
    }

    private void printMap() {
        List<String> keyList = alphabetizeList(elementMap.keySet());
        for (String className : keyList) {
            List<Pair<Element, String>> list = elementMap.get(className);
            System.out.println(className + "{");
            for (Pair<Element, String> pair : list) {
                System.out.println(pair.second.toString() + " " + pair.first);
            }
            System.out.println("}");
        }
    }

    private List<String> alphabetizeList(Set<String> keySet) {
        List<String> arrayList = new ArrayList<>();
        arrayList.addAll(keySet);
        Collections.sort(arrayList);
        return arrayList;
    }

    private String createAddSource(List<Integer> paramIndex) {
        if (paramIndex.isEmpty())
            return "";
        String anno = "@AddsSourceData";
        if (paramIndex.size() != 1 || paramIndex.get(0) != 1) {
            if (paramIndex.size() > 1) {
                String params = paramIndex.toString();
                params = params.replace("[", "{");
                params = params.replace("]", "}");
                anno += "(" + params + ")";
            } else if (paramIndex.size() == 1) {
                anno += "(" + paramIndex.get(0) + ")";
            }
        }
        return anno;
    }

    private boolean existingAddSourceIsCorrect(List<Integer> paramIndex,
            Element ele) {
        AnnotationMirror existingAnno = factory.getDeclAnnotation(ele,
                AddsSourceData.class);
        if (existingAnno == null && paramIndex.isEmpty())
            return true;
        if (existingAnno == null)
            return false;
        List<Integer> currentIndex = AnnotationUtils.getElementValueArray(
                existingAnno, "value", Integer.class, true);
        return currentIndex.equals(paramIndex);
    }

    private void addToMap(Element ele, String annotationMirror) {
        String className = ElementUtils.getQualifiedClassName(ele).toString();
        List<Pair<Element, String>> list = elementMap.get(className);
        if (list == null) {
            list = new ArrayList<>();
            elementMap.put(className, list);
        }
        list.add(Pair.of(ele, annotationMirror));
    }

    private List<Integer> getParamIndexWithSource(AnnotatedExecutableType atm) {
        SourceScanner scanner = new SourceScanner();
        scanner.visitExecutable(atm, null);
        return scanner.getParams();
    }

    /**
     * Creates a list of parameter indexes that have @Source annotations
     * anywhere in its type.
     *
     * Call visitExecutable(...) to start then getParams to get list of parameters.
     */
    class SourceScanner extends AnnotatedTypeScanner<Boolean, Void> {
        private ArrayList<Integer> params;

        @Override
        protected Boolean scan(AnnotatedTypeMirror type, Void p) {
            if (type == null)
                return false;

            Set<PFPermission> sources = Flow.getSources(type);
            if (!sources.isEmpty()) {
               return true;
            }

            Boolean b = type.accept(this, p);
            return (b == null) ? false : b;
        }

        @Override
        protected Boolean reduce(Boolean r1, Boolean r2) {
            if (r1 == null && r2 == null)
                return false;
            if (r1 == null)
                return r2;
            if (r2 == null)
                return r1;
            return r1 || r2;
        }

        @Override
        public Boolean visitExecutable(AnnotatedExecutableType type, Void p) {
            params = new ArrayList<>();
            if (type == null)
                return false;
            boolean receiverSource = false;
            if (type.getReceiverType() != null)
                receiverSource = scan(type.getReceiverType(), p);
            if (receiverSource)
                params.add(0);

            int i = 1;
            for (AnnotatedTypeMirror param : type.getParameterTypes()) {
                if (scan(param, p))
                    params.add(i);
                i++;
            }

            return params.isEmpty();
        }

        @Override
        public Boolean visitTypeVariable(AnnotatedTypeVariable type, Void p) {
            //do nothing, currently this is causing infinite recursion
            return false;
        }

        @Override
        public Boolean visitWildcard(AnnotatedWildcardType type, Void p) {
            //do nothing, currently this is causing infinite recursion
            return false;
        }


        public ArrayList<Integer> getParams() {
            return params;
        }

    }
}
