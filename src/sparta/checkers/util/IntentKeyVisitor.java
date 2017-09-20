package sparta.checkers.util;


import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.common.value.ValueVisitor;
import org.checkerframework.common.value.qual.StringVal;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.InternalUtils;

import sparta.checkers.quals.Extra;
import sparta.checkers.quals.GetExtra;
import sparta.checkers.quals.IntentMap;
import sparta.checkers.quals.PutExtra;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

public class IntentKeyVisitor extends ValueVisitor {
    IntentKeyChecker ikChecker = (IntentKeyChecker) checker;

    public IntentKeyVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    protected ValueAnnotatedTypeFactory createTypeFactory() {
        return new ValueAnnotatedTypeFactory(checker);
    }

    @Override
    protected void checkMethodInvocability(AnnotatedExecutableType method, MethodInvocationTree node) {
        if (hasDeclAnnotation(node, GetExtra.class)) {
            whatkind(node);
            countGetExtra(node);
            return;
        } else if (hasDeclAnnotation(node, PutExtra.class)) {
           putOrIncrementMap(method.getParameterTypes().get(0).getUnderlyingType().toString(), ikChecker.putExtraTypes);
            countPutExtra(node);
            return;
        }
        super.checkMethodInvocability(method, node);
    }

    private void putOrIncrementMap(String s, Map<String, Integer> map) {
        int count = 0;
        if (map.containsKey(s)) {
            count = map.get(s);
        }
        count++;
        map.put(s, count);
    }

    private void whatkind(MethodInvocationTree node) {
        String method = node.toString();

        Pattern pat = Pattern.compile(".*get(.*)Extra.*");
        Matcher m = pat.matcher(method);
        if (m.matches()) {
            String type = m.group(1);
            putOrIncrementMap(type, ikChecker.getExtraTypes);

        }
    }

    private void countGetExtra(MethodInvocationTree node) {
        ikChecker.numGetExtra++;
        List<String> keys = getKeys(node);
        if (keys.isEmpty()) {
            checker.report(Result.warning("intent.key.error"), node);
            ikChecker.getExtraUnknownKey++;
        } else if (keys.size() > 1) {
            ikChecker.getExtraMultiKey++;
        } else {
            ikChecker.getExtraOneKey++;
        }
        addKeysToList(keys);
    }

    private void countPutExtra(MethodInvocationTree node) {
        ikChecker.numPutExtra++;
        List<String> keys = getKeys(node);
        if (keys.isEmpty()) {
            checker.report(Result.warning("intent.key.error"), node);
            ikChecker.putExtraUnknownKey++;
        } else if (keys.size() > 1) {
            ikChecker.putExtraMultiKey++;
        } else {
            ikChecker.putExtraOneKey++;
        }
        addKeysToList(keys);
    }

    private void addKeysToList(List<String> keys) {
        for (String s : keys) {
           putOrIncrementMap(s, ikChecker.keys);
        }
    }

    public boolean hasDeclAnnotation(MethodInvocationTree tree, Class<? extends Annotation> anno) {
        Element ele = InternalUtils.symbol(tree);
        return atypeFactory.getDeclAnnotation(ele, anno) != null;
    }

    private List<String> getKeys(MethodInvocationTree tree) {
        List<String> keys = null;
        AnnotationMirror stringValAnno = atypeFactory.getAnnotationMirror(tree.getArguments().get(0), StringVal.class);
        if (stringValAnno != null) {
            keys = AnnotationUtils.getElementValueArray(stringValAnno, "value", String.class, true);
        }
        if (keys == null){
            keys= new ArrayList<>();
        }
        if(keys.isEmpty()){
            //ValueChecker doesn't work for if node is inside an anonymous class
            //and key is a public static final field in the outer class
            //example:
            //class Outer{
            //public static final String key = "key";
            //private final OnClickListener cancelButtonSelectListener = new OnClickListener( ) {
//            @Override
//            public void onClick( final View v ) {
//              getIntent.putExtra(key);
//            }
//          };

            ExpressionTree t = tree.getArguments().get(0);
            if (t instanceof JCTree.JCLiteral) {
                String k = t.toString();
                keys.add(k.substring(1, k.length()-1));
            } else {
                Element elt = InternalUtils.symbol(t);
                if (ElementUtils.isCompileTimeConstant(elt)) {
                    keys.add(((VariableElement) elt).getConstantValue()
                            .toString());
                }
            }
        }
        return keys;

    }

    @Override
    public Void visitAnnotation(AnnotationTree node, Void p) {
        List<? extends ExpressionTree> args = node.getArguments();
        if (args.isEmpty()) {
            // Nothing to do if there are no annotation arguments.
            return null;
        }

        Element anno = TreeInfo.symbol((JCTree) node.getAnnotationType());
        if (anno.toString().equals(IntentMap.class.getName()) || anno.toString().equals(Extra.class.getName())) {
            return null;
        }
        return super.visitAnnotation(node, p);
    }

}
