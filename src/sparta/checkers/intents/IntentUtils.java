package sparta.checkers.intents;

import checkers.types.AnnotatedTypeMirror;
import checkers.util.AnnotationBuilder;

import javacutils.AnnotationUtils;
import javacutils.InternalUtils;
import javacutils.TreeUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import sparta.checkers.Flow;
import sparta.checkers.FlowAnnotatedTypeFactory;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.IExtra;
import sparta.checkers.quals.IntentExtras;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreePath;

public class IntentUtils {

    //Methods below are on the format NAME,#ofParameters
    public static List<String> SERVICE_CALLBACK_METHODS = Arrays
        .asList(new String[] { "onBind,1", "onRebind,1", "onStart,2",
            "onStartCommand,3", "onTaskRemoved,1", "onUnBind,1"});
    
    public static List<String> BRECEIVER_CALLBACK_METHODS = Arrays
            .asList(new String[] { "onReceive,2", "peekService,2"});
    
    public static List<String> GET_INTENT = Arrays
            .asList(new String[] { "getIntent,0"});
    
    private static List<String> GETEXTRA_SIGNATURES_NO_DEFAULT = Arrays
        .asList(new String[]{"getBooleanArrayExtra", "getBundleExtra", 
            "getByteArrayExtra", "getCharArrayExtra", "getFloatArrayExtra",
            "getCharSequenceArrayExtra", "getCharSequenceArrayListExtra",
            "getCharSequenceExtra", "getDoubleArrayExtra", "getIntArrayExtra",
            "getIntegerArrayListExtra", "getLongArrayExtra", 
            "getParcelableArrayExtra", "getParcelableArrayListExtra",
            "getParcelableExtra", "getSerializableExtra", "getShortArrayExtra",
            "getStringArrayExtra", "getStringArrayListExtra", "getStringExtra",
            });
    
    private static List<String> GETEXTRA_SIGNATURES_WITH_DEFAULT = Arrays
        .asList(new String[] { "getBooleanExtra", "getByteExtra",
            "getCharExtra", "getDoubleExtra", "getFloatExtra",
            "getIntExtra", "getLongExtra", "getShortExtra"});

    private static List<String> PUTEXTRA_SIGNATURES = Arrays
        .asList(new String[] { 
            "putExtra", "putCharSequenceArrayListExtra",
            "putIntegerArrayListExtra", "putParcelableArrayListExtra",
            "putStringArrayListExtra" });
    
    private static List<String> PAYLOAD_SIGNATURES = Arrays
            .asList(new String[] { 
                "setAction", "addCategory",
                "setData", "setType", "setDataAndType",
                "removeCategory"});

    /**
     * Method that receives an @IntentExtras and a <code> key </code>
     * and return the @IExtra with that key and <code>null</code> if it 
     * does not contain the key.
     */
    public static AnnotationMirror getIExtraWithKey(AnnotationMirror intentExtras, 
            String key) {
        List<AnnotationMirror> iExtrasList = AnnotationUtils
            .getElementValueArray(intentExtras, "value",
                AnnotationMirror.class, true);
        for(AnnotationMirror iExtra : iExtrasList) {
            String iExtraKey = AnnotationUtils.getElementValue(
                iExtra, "key", String.class, true);
            if(iExtraKey.equals(key)) {
                return iExtra;
            }
        }
        return null;
    }
    public static AnnotationMirror getIExtra(String keyName, AnnotatedTypeMirror receiverType) {
        AnnotationMirror iExtra = null;
        AnnotationMirror receiverIntentAnnotation = null;
        if (receiverType.hasAnnotation(IntentExtras.class)) {
             receiverIntentAnnotation = receiverType
                .getAnnotation(IntentExtras.class);
            
            if(IntentUtils.hasKey(receiverIntentAnnotation, keyName)) {
                 iExtra = IntentUtils.
                    getIExtraWithKey(receiverIntentAnnotation, keyName); 
            } 
        }
        return iExtra;
    }

    /**
     * Return true if @IntentExtras has this key
     */

    public static boolean hasKey(AnnotationMirror intentExtras, String key) {
        List<AnnotationMirror> iExtrasList = AnnotationUtils
            .getElementValueArray(intentExtras, "value",
                AnnotationMirror.class, true);
        for(AnnotationMirror iExtra : iExtrasList) {
            String iExtraKey = AnnotationUtils.getElementValue(
                iExtra, "key", String.class, true);
            if(iExtraKey.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the union of sources from 2 @IExtra annotations
     */

    public static Set<FlowPermission> unionSourcesIExtras(AnnotationMirror iExtra1, 
            AnnotationMirror iExtra2) {
        return  Flow.unionSources(getSources(iExtra1), getSources(iExtra2));

    }

    private static HashSet<FlowPermission> getSources(AnnotationMirror iExtra) {
        return new HashSet<FlowPermission>(
            AnnotationUtils.getElementValueEnumArray(iExtra, "source",
                FlowPermission.class, true));
    }

    /**
     * Return the union of sinks from 2 @IExtra annotations
     */

    public static Set<FlowPermission> unionSinksIExtras(AnnotationMirror iExtra1, 
            AnnotationMirror iExtra2) {
        return Flow.unionSinks(getSinks(iExtra1), getSinks(iExtra2));
    }

    private static HashSet<FlowPermission> getSinks(AnnotationMirror iExtra) {
        return new HashSet<FlowPermission>(
            AnnotationUtils.getElementValueEnumArray(iExtra, "sink",
                FlowPermission.class, true));
    }

    /**
     * Return the intersection of sources from 2 @IExtra annotations
     */

    public static Set<FlowPermission> intersectionSourcesIExtras(AnnotationMirror iExtra1, 
            AnnotationMirror iExtra2) {
        return Flow.intersectSinks(getSources(iExtra1), getSources(iExtra2));
    }

    /**
     * Return the intersection of sinks from 2 @IExtra annotations
     */

    public static Set<FlowPermission> intersectionSinksIExtras(AnnotationMirror iExtra1, 
            AnnotationMirror iExtra2) {
        return Flow.intersectSinks(getSinks(iExtra1),  getSinks(iExtra2));

    }

    /**
     * Creates a new IExtra AnnotationMirror
     * @param key
     * @param sources
     * @param sinks
     * @param processingEnv
     * @return
     */

    public static AnnotationMirror createIExtraAnno(String key,
            AnnotationMirror sources, AnnotationMirror sinks, 
            ProcessingEnvironment processingEnv) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
            IExtra.class);
        Set<FlowPermission> sourcesSet = Flow.getSources(sources);
        Set<FlowPermission> sinksSet = Flow.getSinks(sinks);
        
        builder.setValue("key", key);
        builder.setValue("source",
            sourcesSet.toArray(new FlowPermission[sourcesSet.size()]));
        builder.setValue("sink",
            sinksSet.toArray(new FlowPermission[sinksSet.size()]));
        return builder.build();
    }

    /**
     * Returns a new @IntentExtras containing all @IExtra from <code>intentExtras</code>
     * and a new <code>IExtra</code>.
     * @param intentExtras
     * @param iExtra
     * @param processingEnv
     * @return
     */

    public static AnnotationMirror addIExtraToIntentExtras(
            AnnotationMirror intentExtras, AnnotationMirror iExtra, 
            ProcessingEnvironment processingEnv) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
            IntentExtras.class);
        List<AnnotationMirror> iExtrasList = AnnotationUtils
            .getElementValueArray(intentExtras, "value",
                AnnotationMirror.class, true);
        iExtrasList.add(iExtra);
        builder.setValue("value", iExtrasList.toArray());
        return builder.build();
    }

    /**
     * Returns a new @IntentExtras containing all @IExtra from <code>intentExtras</code>
     * and all <code>IExtras</code>. 
     * @param intentExtras
     * @param iExtras
     * @param processingEnv
     * @return
     */

    public static AnnotationMirror addIExtrasToIntentExtras(AnnotationMirror intentExtras, 
            List<AnnotationMirror> iExtras, ProcessingEnvironment processingEnv) {
        AnnotationMirror result = intentExtras;
        for (AnnotationMirror iExtra : iExtras) {
            result = addIExtraToIntentExtras(result, iExtra, processingEnv);
        }
        return result;
    }

    /**
     * Returns true if the MethodInvocationTree corresponds to one of the <code>Intent.getExtra()</code> calls
     * @param tree
     * @return
     */

    public static boolean isGetExtraMethod(MethodInvocationTree tree, 
            ProcessingEnvironment processingEnv) {
        //The getExtra call can have 1 or 2 parameters,
        //2 when there is a use of default parameter, 1 otherwise.
        for (String getExtraSignature : GETEXTRA_SIGNATURES_WITH_DEFAULT) {
            ExecutableElement getExtraWithDefault = TreeUtils.getMethod(
                "android.content.Intent", getExtraSignature, 2, processingEnv);
            if (getExtraWithDefault != null
                    && TreeUtils.isMethodInvocation(tree, getExtraWithDefault,
                        processingEnv)) {
                return true;
            }
        }
        
        for (String getExtraSignature : GETEXTRA_SIGNATURES_NO_DEFAULT) {
            ExecutableElement getExtra = TreeUtils.getMethod(
                "android.content.Intent", getExtraSignature, 1, processingEnv);
            if (getExtra != null
                    && TreeUtils.isMethodInvocation(tree, getExtra,
                        processingEnv)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the MethodInvocationTree corresponds to one of the <code>Intent.putExtra()</code> calls
     * TODO: It cannot be implemented the same way the isGetExtraMethod() was implemented.
     * The problem is that there are several putExtra signatures with the same amount of parameters and name
     * and the TreeUtils.getMethod() cannot differentiate between them, it always returns the putExtra(String,boolean).
     * If tree is putExtra(String,String) it won't pass this method.
     * @param tree
     * @return
     */

    public static boolean isPutExtraMethod(MethodInvocationTree tree) {
        Element ele = (Element) InternalUtils.symbol(tree);
        if(ele instanceof ExecutableElement) {
            ExecutableElement method = (ExecutableElement) ele;
            return PUTEXTRA_SIGNATURES.contains(method.getSimpleName().toString());
        }
        return false;
        //				// correct way to do it. the problem is that there are several
        //				// putExtra methods with the same name and all
        //				// of them has the same number of paremeters. How to get each
        //				// one of them? TreeUtils.getMethod returns only
        //				// the first one.
        //				// ExecutableElement putExtra = TreeUtils.getMethod(
        //				// "android.content.Intent", s, 2, processingEnv);
        //				// if (putExtra != null
        //				// && TreeUtils.isMethodInvocation(tree, putExtra,
        //				// processingEnv)) {
        //				// return true;
        //				// }
    }
    
    public static boolean isIntentPayloadMethod(MethodInvocationTree tree) {
        Element ele = (Element) InternalUtils.symbol(tree);
        if(ele instanceof ExecutableElement) {
            ExecutableElement method = (ExecutableElement) ele;
            return PAYLOAD_SIGNATURES.contains(method.getSimpleName().toString());
        }
        return false;
        
    }

    /**
     * This method receives a tree for a sendIntent(intent) call and returns the
     * Component name with its action,category and parameter, if any. The format
     * is "Component(Action,[Category1,Category2],Data)". If there is none of
     * these elements, returns only "Component".
     * 
     * @param tree
     *            the sendIntent() tree
     * @param treePath 
     * @param atypeFactory 
     * @return intent being sent in a String format, with Action,Category and
     *         Data, if any.
     */
    public static String resolveIntentFilters(MethodInvocationTree tree, 
            FlowAnnotatedTypeFactory atypeFactory, TreePath treePath) {
        String senderString = "";
        ClassTree classTree = TreeUtils.enclosingClass(treePath);
        senderString += classTree.getSimpleName().toString();
        AnnotationMirror am = atypeFactory.getAnnotatedType(
            tree.getArguments().get(0)).getAnnotation(IntentExtras.class);
        String action = AnnotationUtils.getElementValue(am, "action",
            String.class, true);
        List<String> categories = AnnotationUtils.getElementValueArray(am,
            "categories", String.class, true);
        String data = AnnotationUtils.getElementValue(am, "data", String.class,
            true);
        if (action.length() == 0 && categories.size() == 0
                && data.length() == 0) {
            return senderString;
        } else {
            // action
            senderString += "(" + action;
            // categories
            if (!categories.isEmpty()) {
                senderString += ",[";
                for (String category : categories) {
                    senderString += category + ",";
                }
                senderString = senderString.substring(0,
                    senderString.length() - 1); // removing last comma
                senderString += "]";
            }
            // data
            if (data.length() > 0) {
                senderString += "," + data;
            }
            senderString += ")";
        }
        return senderString;
    }

    public  AnnotationMirror createAnnoFromValues(ProcessingEnvironment processingEnv, 
        Class<? extends Annotation> anno, 
        List<CharSequence> elementsNames, List<Enum<?>> values) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv, anno);
        if(elementsNames.size() != values.size()) {
            //Both need to have the same amount of elements.
            throw new RuntimeException();
        }
        for(int i = 0; i < elementsNames.size(); i++) {
            builder.setValue(elementsNames.get(i), values.get(i));
        }
        return builder.build();
    }

}
