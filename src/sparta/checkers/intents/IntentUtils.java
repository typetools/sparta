package sparta.checkers.intents;

import checkers.types.AnnotatedTypeFactory;
import checkers.types.AnnotatedTypeMirror;
import checkers.util.AnnotationBuilder;

import javacutils.AnnotationUtils;
import javacutils.InternalUtils;
import javacutils.TreeUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import sparta.checkers.Flow;
import sparta.checkers.FlowAnnotatedTypeFactory;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.GetExtra;
import sparta.checkers.quals.ParameterizedFlowPermission;
import sparta.checkers.quals.Extra;
import sparta.checkers.quals.IntentMap;
import sparta.checkers.quals.PolyFlow;
import sparta.checkers.quals.PutExtra;
import sparta.checkers.quals.ReceiveIntent;
import sparta.checkers.quals.SendIntent;
import sparta.checkers.quals.SetIntentFilter;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
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
    
    public static List<String> RECEIVE_INTENT_METHODS = Arrays
            .asList(new String[] {"onBind", "onRebind", "onStart",
                    "onStartCommand", "onTaskRemoved", "onUnBind",
                    "onReceive", "peekService", "getIntent" });
    
    /**
     * Method that receives an @IntentMap and a <code> key </code>
     * and return the @Extra with that key and <code>null</code> if it 
     * does not contain the key.
     */
    public static AnnotationMirror getIExtra(AnnotationMirror intentExtras, 
            String key) {
        List<AnnotationMirror> iExtrasList = getIExtras(intentExtras);
        for(AnnotationMirror iExtra : iExtrasList) {
            if(key.equals(getKeyName(iExtra))) {
                return iExtra;
            }
        }
        return null;
    }
    
    public static List<AnnotationMirror> getIExtras(AnnotationMirror intentExtra) {
        return AnnotationUtils
            .getElementValueArray(intentExtra, "value", AnnotationMirror.class, true);
    }
    
    public static AnnotationMirror getIExtra(AnnotatedTypeMirror intentExtra, String keyName) {
        if (intentExtra.hasAnnotation(IntentMap.class)) {
            return getIExtra(
                    intentExtra.getAnnotation(IntentMap.class), keyName);
        }
        return null;
    }

    /**
     * Return true if @IntentMap has this key
     */

    public static boolean hasKey(AnnotationMirror intentExtras, String key) {
        List<AnnotationMirror> iExtrasList = getIExtras(intentExtras);
        for(AnnotationMirror iExtra : iExtrasList) {
            String iExtraKey = getKeyName(iExtra);
            if(iExtraKey.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the union of sources from 2 @Extra annotations
     */

    public static Set<ParameterizedFlowPermission> unionSourcesIExtras(AnnotationMirror iExtra1, 
            AnnotationMirror iExtra2) {
        return  Flow.unionSources(getSourcesPFP(iExtra1), getSourcesPFP(iExtra2));

    }

    public static Set<ParameterizedFlowPermission> getSourcesPFP(AnnotationMirror iExtra) {
        return Flow.convertToParameterizedFlowPermission(getSources(iExtra));
    }
    
    public static Set<FlowPermission> getSources(AnnotationMirror iExtra) {
        return new TreeSet<FlowPermission>(
            AnnotationUtils.getElementValueEnumArray(
                iExtra, "source", FlowPermission.class, true));
    }

    /**
     * Return the union of sinks from 2 @Extra annotations
     */

    public static Set<ParameterizedFlowPermission> unionSinksIExtras(AnnotationMirror iExtra1, 
            AnnotationMirror iExtra2) {
        return Flow.unionSinks(getSinksPFP(iExtra1), getSinksPFP(iExtra2));
    }

    public static Set<ParameterizedFlowPermission> getSinksPFP(AnnotationMirror iExtra) {
        return Flow.convertToParameterizedFlowPermission(getSinks(iExtra));
    }
    
    public static Set<FlowPermission> getSinks(AnnotationMirror iExtra) {
        return new TreeSet<FlowPermission>(
            AnnotationUtils.getElementValueEnumArray(
                iExtra, "sink", FlowPermission.class, true));
    }




    /**
     * Return the intersection of sources from 2 @Extra annotations
     */

    public static Set<ParameterizedFlowPermission> intersectionSourcesIExtras(AnnotationMirror iExtra1, 
            AnnotationMirror iExtra2) {
        return Flow.intersectSinks(getSourcesPFP(iExtra1), getSourcesPFP(iExtra2));
    }

    /**
     * Return the intersection of sinks from 2 @Extra annotations
     */

    public static Set<ParameterizedFlowPermission> intersectionSinksIExtras(AnnotationMirror iExtra1, 
            AnnotationMirror iExtra2) {
        return Flow.intersectSinks(getSinksPFP(iExtra1),  getSinksPFP(iExtra2));

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
            Extra.class);
        Set<FlowPermission> sourcesSet = Flow.convertFromParameterizedFlowPermission(Flow.getSources(sources));
        Set<FlowPermission> sinksSet = Flow.convertFromParameterizedFlowPermission(Flow.getSinks(sinks));
        
        builder.setValue("key", key);
        builder.setValue("source",
            sourcesSet.toArray(new FlowPermission[sourcesSet.size()]));
        builder.setValue("sink",
            sinksSet.toArray(new FlowPermission[sinksSet.size()]));
        return builder.build();
    }

    /**
     * Returns a new @IntentMap containing all @Extra from <code>intentExtras</code>
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
            IntentMap.class);
        List<AnnotationMirror> iExtrasList = getIExtras(intentExtras);
        iExtrasList.add(iExtra);
        builder.setValue("value", iExtrasList.toArray());
        return builder.build();
    }

    /**
     * Returns a new @IntentMap containing all @Extra from <code>intentExtras</code>
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

    public static boolean isGetExtra(MethodInvocationTree tree, AnnotatedTypeFactory atypeFactory) {
        Element ele = InternalUtils.symbol(tree);
        return atypeFactory.getDeclAnnotation(ele, GetExtra.class) != null;
    }

    /**
     * Returns true if the MethodInvocationTree corresponds to one of the <code>Intent.putExtra()</code> calls
     * @param tree
     * @return
     */

    public static boolean isPutExtra(MethodInvocationTree tree, AnnotatedTypeFactory atypeFactory) {
        Element ele = InternalUtils.symbol(tree);
        return atypeFactory.getDeclAnnotation(ele, PutExtra.class) != null;
    }
    
    /**
     * Returns true if the MethodInvocationTree corresponds to one of the <code>sendIntent()</code> calls:
     * E.g.: startActivity(); startService(); sendBroadcast();
     * @param tree
     * @return
     */

    public static boolean isSendIntent(MethodInvocationTree tree, AnnotatedTypeFactory atypeFactory) {
        Element ele = InternalUtils.symbol(tree);
        return atypeFactory.getDeclAnnotation(ele, SendIntent.class) != null;
    }
    
    /**
     * Returns true if the MethodInvocationTree corresponds to one of the <code>receiveIntent()</code> calls:
     * E.g.: onBind(); onReceive(); getIntent();
     * @param tree
     * @return
     */

    public static boolean isReceiveIntent(MethodTree tree, AnnotatedTypeFactory atypeFactory) {
        Element ele = InternalUtils.symbol(tree);
        return atypeFactory.getDeclAnnotation(ele, ReceiveIntent.class) != null;
    }
    
    /**
     * Returns true if the MethodInvocationTree corresponds to one of the methods that sets an intent filter
     * to an intent:
     * E.g.: setAction(); addCategory(); setData();
     * @param tree
     * @return
     */

    public static boolean isSetIntentFilter(MethodInvocationTree tree, AnnotatedTypeFactory atypeFactory) {
        Element ele = InternalUtils.symbol(tree);
        return atypeFactory.getDeclAnnotation(ele, SetIntentFilter.class) != null;
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
            tree.getArguments().get(0)).getAnnotation(IntentMap.class);
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
    
    public static String getKeyName(AnnotationMirror iExtra) {
        return AnnotationUtils.getElementValue(iExtra, "key",
            String.class, true);
    }

}
