package sparta.checkers.intents;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.InternalUtils;
import org.checkerframework.javacutil.TreeUtils;

import sparta.checkers.Flow;
import sparta.checkers.quals.Extra;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.GetExtra;
import sparta.checkers.quals.GetIntentFilter;
import sparta.checkers.quals.IntentMap;
import sparta.checkers.quals.IntentMapBottom;
import sparta.checkers.quals.IntentMapNew;
import sparta.checkers.quals.PFPermission;
import sparta.checkers.quals.PutExtra;
import sparta.checkers.quals.ReceiveIntent;
import sparta.checkers.quals.SendIntent;
import sparta.checkers.quals.SetIntentFilter;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;

public class IntentUtils {

    public static final String qualsPackage = "sparta.checkers.quals.";
    //List of annotations not allowed in source code.
    public static final List<String> notAllowedAnnos = Arrays.asList(
            new String[]{qualsPackage + "SendIntent", qualsPackage +
                    "IntentMapNew", qualsPackage + "PutExtra", qualsPackage +
                    "GetExtra", qualsPackage + "SetIntentFilter"});

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

    /**
     * Method that returns the @Extra AM from an @IntentMap ATM with a certain key.
     * @param atm AnnotatedTypeMirror that might contain an @IntentMap
     * @param keyName Key
     * @param declaredType If true, look for the @Extra in the declared type.
     * @return AnnotationMirror of the @Extra with that key, or null if not found.
     */

    public static AnnotationMirror getIExtra(AnnotatedTypeMirror atm,
            String keyName) {
        if (atm.hasAnnotation(IntentMap.class)) {
            return getIExtra(atm.getAnnotation(IntentMap.class), keyName);
        }
        return null;
    }

    /**
     * Given an ATM and a String "k", returns an @Extra in the declared type
     * of ATM with key "k" if it exists, and null otherwise.
     * @param atm
     * @param keyName
     * @return AnnotationMirror of the @Extra with that key, or null if not found.
     */
    public static AnnotationMirror getDeclaredTypeIExtra(AnnotatedTypeMirror
            atm, String keyName) {
        Set<AnnotationMirror> annos = atm.getExplicitAnnotations();
        for (AnnotationMirror anno : annos) {
            if (AnnotationUtils.areSameByClass(anno, IntentMap.class)) {
                return getIExtra(anno, keyName);
            }
        }
        return null;
    }

    /**
     * Return the union of sources from 2 @Extra annotations
     */

    public static Set<PFPermission> unionSourcesIExtras(AnnotationMirror iExtra1,
            AnnotationMirror iExtra2) {
        return  Flow.unionSources(getSourcesPFP(iExtra1), getSourcesPFP(iExtra2));

    }

    public static Set<PFPermission> getSourcesPFP(AnnotationMirror iExtra) {
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

    public static Set<PFPermission> unionSinksIExtras(AnnotationMirror iExtra1,
            AnnotationMirror iExtra2) {
        return Flow.unionSinks(getSinksPFP(iExtra1), getSinksPFP(iExtra2));
    }

    public static Set<PFPermission> getSinksPFP(AnnotationMirror iExtra) {
        return Flow.convertToParameterizedFlowPermission(getSinks(iExtra));
    }

    public static Set<FlowPermission> getSinks(AnnotationMirror iExtra) {
        return new TreeSet<FlowPermission>(
            AnnotationUtils.getElementValueEnumArray(
                iExtra, "sink", FlowPermission.class, true));
    }

    /**
     * Creates a new @Extra AnnotationMirror
     */
    public static AnnotationMirror createIExtraAnno(String key,
            Set<PFPermission> sources,
            Set<PFPermission> sinks,
            ProcessingEnvironment processingEnv) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
            Extra.class);
        Set<FlowPermission> sourcesSet = Flow.
                convertFromParameterizedFlowPermission(sources);
        Set<FlowPermission> sinksSet = Flow.
                convertFromParameterizedFlowPermission(sinks);

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

    public static AnnotationMirror addIExtraInIntentExtras(
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
     * Returns a new @IntentMap with the same @Extras contained in
     * <code>intentExtras</code> except for the @Extra with key <code>key</code>.
     * Instead, the result will contain a new @Extra with key <code>key</code>,
     * sources <code>sources</code> and sinks <code>sinks</code>. This method
     * requires <code>intentExtras</code> to have an @Extra with key <code>key</code>.
     *
     * @param intentExtras
     * @param key
     * @param sources
     * @param sinks
     * @param processingEnv
     * @return
     */
    public static AnnotationMirror getNewIMapWithExtra(AnnotationMirror intentExtras,
            String key, Set<PFPermission> sources,
            Set<PFPermission> sinks,
            ProcessingEnvironment processingEnv) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
                IntentMap.class);
        AnnotationMirror originalExtra = getIExtra(intentExtras, key);
        assert (originalExtra != null); // @Extra with key must exist.
        List<AnnotationMirror> iExtrasList = getIExtras(intentExtras);
        iExtrasList.remove(originalExtra);
        iExtrasList.add(createIExtraAnno(key, sources, sinks, processingEnv));
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
            result = addIExtraInIntentExtras(result, iExtra, processingEnv);
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
     * Returns the ExecutableElement of the getIntent() method declaration
     * for the class passed as parameter.
     *
     */
    public static ExecutableElement getMethodGetIntent(BaseTypeChecker checker, String canonicalClassName) {
        TypeElement mapElt = checker.getProcessingEnvironment().getElementUtils().getTypeElement(canonicalClassName);
        ExecutableElement getIntentMethod = null;
        for (ExecutableElement exec : ElementFilter.methodsIn(mapElt.getEnclosedElements())) {
            if (exec.getSimpleName().contentEquals("getIntent")
                    && exec.getParameters().size() == 0)
                getIntentMethod = exec;
        }

        return getIntentMethod;
    }

    /**
     * Returns the ExecutableElement of the setIntent(intent) method declaration
     * for the class passed as parameter.
     *
     */
    public static ExecutableElement getMethodSetIntent(BaseTypeChecker checker, String canonicalClassName) {
        TypeElement mapElt = checker.getProcessingEnvironment().getElementUtils().getTypeElement(canonicalClassName);
        ExecutableElement setIntentMethod = null;
        for (ExecutableElement exec : ElementFilter.methodsIn(mapElt.getEnclosedElements())) {
            if (exec.getSimpleName().contentEquals("setIntent")
                    && exec.getParameters().size() == 1)
                setIntentMethod = exec;
        }

        return setIntentMethod;
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

    /**
     * Returns true if the MethodInvocationTree corresponds to one of the methods that gets an intent filter
     * of an intent:
     * E.g.: getAction(); getType();
     * @param tree
     * @return
     */

    public static boolean isGetIntentFilter(MethodInvocationTree tree, AnnotatedTypeFactory atypeFactory) {
        Element ele = InternalUtils.symbol(tree);
        return atypeFactory.getDeclAnnotation(ele, GetIntentFilter.class) != null;
    }

    public static boolean isIntentMapBottom(AnnotatedTypeMirror atm) {
        return atm.hasAnnotation(IntentMapBottom.class);
    }

    public static boolean isIntentMap(AnnotatedTypeMirror atm) {
        return atm.hasAnnotation(IntentMap.class) || isIntentMapBottom(atm)
                || isIntentMapNew(atm);
    }

    public static boolean isIntentMapNew(AnnotatedTypeMirror atm) {
        return atm.hasAnnotation(IntentMapNew.class);
    }

    public static String retrieveSendIntentPath(TreePath treePath) {
        String senderString = "";
        //senderString = package.class
        ClassTree classTree = TreeUtils.enclosingClass(treePath);
        ClassSymbol ele = (ClassSymbol) InternalUtils.symbol(classTree);
        senderString = ele.flatname.toString();

        //senderString += .method(
        MethodTree methodTree = TreeUtils.enclosingMethod(treePath);
        senderString += "." + methodTree.getName() + "(";

        //senderString += args)
        //Removing annotation types from parameters
        List<? extends VariableTree> args = methodTree.getParameters();
        for(VariableTree arg : args) {
            Type type = (Type) InternalUtils.typeOf(arg.getType());
            String typeStringFormat = "";
            //TODO: Remove loop below when unannotatedType gets implemented for
            //com.sun.tools.javac.code.Type.ArrayType.
            //Use only type.unannotatedType().toString().
            while (type.getKind() == TypeKind.ARRAY) {
                if (type.getKind() == TypeKind.ARRAY) {
                    typeStringFormat += "[]";
                    type = ((ArrayType)type).getComponentType();
                }
            }
            typeStringFormat = type.unannotatedType().toString() + typeStringFormat;
            String typeNoAnnotations = typeStringFormat;
            senderString += typeNoAnnotations + ",";
        }
        //Removing last comma in case of args.size() != 0.
        if(args.size() > 0) {
            senderString = senderString.substring(0,senderString.length()-1);
        }
        senderString += ')';

        //Component map does not have entries with Generics parameters
        //due to epicc's limitations. Need to remove <?> from parameters.
        senderString = senderString.replaceAll("<([^;]*)>", "");

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
