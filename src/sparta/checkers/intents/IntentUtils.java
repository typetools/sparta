package sparta.checkers.intents;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.framework.util.AnnotationBuilder;

import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.InternalUtils;
import org.checkerframework.javacutil.TreeUtils;

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
import javax.lang.model.element.Name;

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
import com.sun.tools.javac.code.Symbol.ClassSymbol;

public class IntentUtils {
    
    public final static String GET_INTENT =  "getIntent,0";
    
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
    

    public static AnnotationMirror getIExtra(AnnotatedTypeMirror intentMapAnno, String keyName) {
        if (intentMapAnno.hasAnnotation(IntentMap.class)) {
            return getIExtra(
                    intentMapAnno.getAnnotation(IntentMap.class), keyName);
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

    public static boolean isReceiveIntent(MethodInvocationTree tree, AnnotatedTypeFactory atypeFactory) {
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

    
    
    public static String retrieveSendIntentPath(TreePath treePath) {
      String senderString = "";
      //senderString = package.class
      ClassTree classTree = TreeUtils.enclosingClass(treePath);  
      ClassSymbol ele = (ClassSymbol) InternalUtils.symbol(classTree);
      senderString = ele.flatname.toString();
      
      //senderString += .method(args)
      MethodTree methodTree = TreeUtils.enclosingMethod(treePath);
      ExecutableElement a = TreeUtils.elementFromDeclaration(methodTree);
      Element b = a.getEnclosingElement();
      Name c = a.getSimpleName();
      senderString += "." + TreeUtils.elementFromDeclaration(methodTree).toString();
      
      //Component map does not have entries with Generics parameters
      //due to epicc's limitations. Need to remove <?> from parameters.
      senderString = senderString.replaceAll("<([^;]*)>", "");
      
      //Removing annotation types from parameters
      senderString = senderString.replaceAll("\\(@([^:]*)\\:\\: ([^)]*)\\)", "$2");
      
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
