package sparta.checkers.intents;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.checkerframework.common.aliasing.qual.Unique;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.InternalUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

import sparta.checkers.FlowVisitor;
import sparta.checkers.intents.componentmap.ProcessEpicOutput;
import sparta.checkers.quals.Extra;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.IntentMap;
import sparta.checkers.quals.IntentMapBottom;
import sparta.checkers.quals.IntentMapNew;
import sparta.checkers.quals.PFPermission;
import sparta.checkers.quals.ReceiveIntent;
import sparta.checkers.quals.SendIntent;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;

public class IntentVisitor extends FlowVisitor {

    IntentAnnotatedTypeFactory intentATF;
    AnnotatedTypeFactory aliasingATF;

    private final Pair<Copyable,String> isCopyable;

    public IntentVisitor(BaseTypeChecker checker) {
        super(checker);
        isCopyable = new Pair<IntentVisitor.Copyable, String>(Copyable.
                IS_COPYABLE, null);
    }

    @Override
    protected IntentAnnotatedTypeFactory createTypeFactory() {
        try {
            intentATF = new IntentAnnotatedTypeFactory(checker);
            aliasingATF = intentATF.aliasingATF;
            return intentATF;
        } catch (Exception e) {
            // The SourceVisitor cuts off the stack trace,
            // so print it here and the throw again.
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 1. Check that methods overriding sendIntent or receiveIntent have the
     * correct
     * 
     * @ReceiveIntent or @SendIntent annotation 2. For receiveIntent methods,
     *                bypass the usual overriding rules.
     * 
     */
    @Override
    protected boolean checkOverride(MethodTree overriderTree,
            AnnotatedDeclaredType enclosingType,
            AnnotatedExecutableType overridden,
            AnnotatedDeclaredType overriddenType, Void p) {
        AnnotatedExecutableType implementingMethod = atypeFactory
                .getAnnotatedType(overriderTree);
        AnnotationMirror receiveAnnoOverriden = atypeFactory.getDeclAnnotation(
                overridden.getElement(), ReceiveIntent.class);
        AnnotationMirror sendAnnoOverridden = atypeFactory.getDeclAnnotation(
                overridden.getElement(), SendIntent.class);

        if (intentATF.getIntent.equals(overridden.getElement())) {
            //It is OK to override the getIntent() method.
            //When the setIntent() method is overridden, the visitor will check
            //if the return type of getIntent() is a supertype of the setIntent 
            //parameter type.
            return true;
        } else if (null != receiveAnnoOverriden) {
            return checkReceiveIntentOverride(overriderTree, overridden,
                    implementingMethod, receiveAnnoOverriden);
            // Don't call super because the receiveIntent
            // does not follow standard Java overriding rules
        } else if (null != sendAnnoOverridden) {
            boolean sendIntentCheck = checkSendIntentOverride(overriderTree,
                    implementingMethod, sendAnnoOverridden);
            boolean standardCheck = super.checkOverride(overriderTree,
                    enclosingType, overridden, overriddenType, p);
            return sendIntentCheck && standardCheck;
        } else {
            // This method isn't a send or receive method
            // so check overriding as usual
            return super.checkOverride(overriderTree, enclosingType,
                    overridden, overriddenType, p);
        }
    }

    private boolean checkSendIntentOverride(MethodTree implementingTree,
            AnnotatedExecutableType implementingMethod,
            AnnotationMirror overriddenAnno) {
        AnnotationMirror implementerAnno = atypeFactory.getDeclAnnotation(
                implementingMethod.getElement(), SendIntent.class);
        if (null == implementerAnno) {
            checker.report(
                    Result.failure("intent.override.sendintent",
                            overriddenAnno.toString()), implementingTree);
            return false;
        } else if (!AnnotationUtils.areSame(overriddenAnno, implementerAnno)) {
            checker.report(Result.failure(
                    "intent.override.sendintent.incorrect",
                    overriddenAnno.toString()), implementingTree);
            return false;
        }
        return true;
    }

    private boolean checkReceiveIntentOverride(MethodTree implementingTree,
            AnnotatedExecutableType overridden, 
            AnnotatedExecutableType implementingMethod,
            AnnotationMirror overridenAnno) {
        checkReceiveIntentDefaulting(implementingTree);

        if (intentATF.setIntent.equals(overridden.getElement())) { 
            checkSetIntentOverride(implementingTree,
                    implementingMethod, overridenAnno);
        }
        AnnotationMirror implementerAnno = atypeFactory.getDeclAnnotation(
                implementingMethod.getElement(), ReceiveIntent.class);

        if (null == implementerAnno) {
            checker.report(Result.failure("intent.override.receiveintent",
                    overridenAnno.toString()), implementingTree);
            return false;

        } else if (!AnnotationUtils.areSame(overridenAnno, implementerAnno)) {
            checker.report(Result.failure(
                    "intent.override.receiveintent.incorrect",
                    overridenAnno.toString()), implementingTree);
            return false;
        }

        return true;
    }

    /**
     * Checks for all @Extra in the @IntentMap if it has both a source and a sink.
     * Defaulting cannot be applied to Intent types of parameters of a
     * @ReceiveIntent. That happens because in a scenario
     * where the app being analyzed sends an intent to a component in another app,
     * the flow policy that should be used to perform the defaulting in the receiver
     * intent is the flow policy from the app that contains the receiver component.
     * It would be complex and confusing to handle several flow policy files,
     * and because of that we opted for not allowing defaulting of @Extra for
     * receiver intents.
     *
     * This method raises a warning asking for the user to be explicit about
     * both sources and sinks if a receive intent doesn't have both for every
     * @Extra.
     */
    private void checkReceiveIntentDefaulting(MethodTree implementingTree) {
        AnnotatedExecutableType aet = atypeFactory.getAnnotatedType(implementingTree);
        List<AnnotatedTypeMirror> params = aet.getParameterTypes();
        //@ReceiveIntent method 1st parameter always has type Intent.
        assert (params.size() > 0); //@ReceiveIntent method must have an Intent type param.
        AnnotatedTypeMirror atm = params.get(0);
        List<AnnotationMirror> extras = IntentUtils.getIExtras(atm.
                getAnnotation(IntentMap.class));
        for (AnnotationMirror extra : extras) {
            if (IntentUtils.getSources(extra).contains(FlowPermission.EXTRA_DEFAULT)) {
                checker.report(Result.failure(
                        "intent.defaulting.receiveintent.source",
                        IntentUtils.getKeyName(extra)), implementingTree.getParameters().get(0));
            } else if (IntentUtils.getSinks(extra).contains(FlowPermission.EXTRA_DEFAULT)) {
                checker.report(Result.failure(
                        "intent.defaulting.receiveintent.sink",
                        IntentUtils.getKeyName(extra)), implementingTree.getParameters().get(0));
            }
        }
    }

    /**
     * Method used to verify that if a ReceiveIntent setIntent method is overridden, the first parameter
     * of this method (an intent) needs to be a subtype of the return type of the method getIntent()
     * on the same class.
     *
     * @param implementingTree
     * @param implementingMethod
     * @param overriddenAnno
     */

    private void checkSetIntentOverride(MethodTree implementingTree,
            AnnotatedExecutableType implementingMethod,
            AnnotationMirror overriddenAnno) {
        ClassTree classTree = TreeUtils.enclosingClass(getCurrentPath());  
        ClassSymbol ele = (ClassSymbol) InternalUtils.symbol(classTree);
        ExecutableElement getIntentMethod = IntentUtils.getMethodGetIntent(
                checker,ele.flatname.toString());
        
        if (getIntentMethod == null) {
            checker.report(Result.failure("intent.getintent.notfound"), implementingTree);
            return;
        }
        
        AnnotationMirror getIntentReturnIntentMap = atypeFactory
                .getAnnotatedType(getIntentMethod).getReturnType()
                .getAnnotationInHierarchy(intentATF.TOP_INTENT_MAP);
        AnnotationMirror setIntentParameterIntentMap = implementingMethod
                .getParameterTypes().get(0)
                .getAnnotationInHierarchy(intentATF.TOP_INTENT_MAP);

        
        if (!atypeFactory.getQualifierHierarchy()
                .isSubtype(setIntentParameterIntentMap, getIntentReturnIntentMap)) {
            checker.report(
                    Result.failure("intent.getintent.setintent",
                            setIntentParameterIntentMap.toString(),
                            getIntentReturnIntentMap.toString()), implementingTree);
        }
    }

    @Override
    protected void checkMethodInvocability(AnnotatedExecutableType method,
            MethodInvocationTree node) {
        //Code copied from superclass method
        if (method.getReceiverType() == null) {
            // Static methods don't have a receiver.
            return;
        }
        if (method.getElement().getKind() == ElementKind.CONSTRUCTOR) {
            // TODO: Explicit "this()" calls of constructors have an implicit passed
            // from the enclosing constructor. We must not use the self type, but
            // instead should find a way to determine the receiver of the enclosing constructor.
            // rcv = ((AnnotatedExecutableType)atypeFactory.getAnnotatedType(atypeFactory.getEnclosingMethod(node))).getReceiverType();
            return;
        }
        if (isTypeOf(method, android.content.Intent.class)
                || isTypeOf(method, android.os.Bundle.class)) {
            checkIntentExtraMethods(method, node);
            return;
        } else if (IntentUtils.isSendIntent(node, atypeFactory)) {
            String receiverName = getReceiverNameFromSendIntendAnnotation(node);
            checkSendIntent(method, node, receiverName);
            return;
        } else if (IntentUtils.isReceiveIntent(node, atypeFactory)) { 
            checker.report(Result.failure("intent.invoking.receiveintent"),node);
            return;
        }
        super.checkMethodInvocability(method, node);
    }

    private boolean isTypeOf(AnnotatedExecutableType type, Class<?> clazz) {
        String classname = clazz.getCanonicalName();
        return TypesUtils.isDeclaredOfName(type.getReceiverType()
                .getUnderlyingType(), classname);
        // TODO: check if is a super type of too?
        // getUnderlyingType()
        // .asElement().getClass().isAssignableFrom(android.app.Activity.class))
    }

    private String getReceiverNameFromSendIntendAnnotation(
            MethodInvocationTree node) {
        AnnotationMirror sendAnno = atypeFactory.getDeclAnnotation(
                InternalUtils.symbol(node), SendIntent.class);
        String receiverName = AnnotationUtils.getElementValue(sendAnno,
                "value", String.class, true);
        if ("".equals(receiverName)) {
            checker.report(Result.failure("intent.error",
                    "Missing receiving method name " + sendAnno.toString()),
                    node);
        }
        return receiverName;
    }

    /**
     * Method used to type-check any sendIntent call.
     * @param method
     *            AnnotatedExecutableType
     * @param node
     *            MethodInvocationTree
     */
    private void checkSendIntent(AnnotatedExecutableType method,
            MethodInvocationTree node, String receiverMethodName) {

        Set<String> receiversFromSender = getReceiversFromSender(IntentUtils.
                retrieveSendIntentPath(getCurrentPath()),node);

        if (receiversFromSender.contains(ProcessEpicOutput.NOT_FOUND)) {
            //No receiver found for this sender.
            //Assuming the component map is correct, this should type-check.
            return;
        }

        //All send intents have an intent parameter, find it
        int paramIndex = -1;
        for (AnnotatedTypeMirror atm : method.getParameterTypes()) {
            paramIndex++;
            if (TypesUtils.isDeclaredOfName(atm.getUnderlyingType(),
                    android.content.Intent.class.getCanonicalName())) {
                break;
            }
        }
        if (paramIndex == -1) {
            checker.report(Result.failure("intent.error",
                    "Send intent method does not have an intent parameter"),
                    node);
            return;
        }

        //Get the @IntentMap annotation for the correct parameter
        ExpressionTree intentObject = node.getArguments().get(paramIndex);
        AnnotatedTypeMirror rhs = atypeFactory.getAnnotatedType(intentObject);
        AnnotationMirror rhsIntentExtras = rhs.getAnnotationInHierarchy(intentATF.TOP_INTENT_MAP);

        if (IntentUtils.isIntentMapBottom(rhs)) {
            // sendIntent(null) case. Raise error and return.
            checker.report(Result.failure("intent.not.initialized"), node);
            return;
        }

        for (Pair<String, MethodInvocationTree> receiveIntentTree : getReceiversMethods(node,
                receiverMethodName, receiversFromSender)) {
            ExecutableElement methodElt = TreeUtils
                    .elementFromUse(receiveIntentTree.snd);
            AnnotatedTypeMirror receiverType = atypeFactory
                    .getReceiverType(receiveIntentTree.snd);
            AnnotatedExecutableType receiveIntentMethod = AnnotatedTypes
                    .asMemberOf(types, atypeFactory, receiverType, methodElt);
            
            //Find the intent parameter, and get the @IntenMap annotation
            AnnotatedTypeMirror receiveIntentAnno = null;
            for (AnnotatedTypeMirror atm : receiveIntentMethod
                    .getParameterTypes()) {
                if (TypesUtils.isDeclaredOfName(atm.getUnderlyingType(),
                        android.content.Intent.class.getCanonicalName())) {
                    receiveIntentAnno = atm;
                    break;
                }
            }
            if (receiveIntentAnno == null) {
                checker.report(
                        Result.failure("intent.error",
                                "Receive intent method does not have an intent parameter"),
                        node);
                return;
            }
            AnnotationMirror lhsIntentExtras = receiveIntentAnno
                    .getAnnotationInHierarchy(intentATF.TOP_INTENT_MAP);

            //Read javadoc of isCopyableTo method 
            Pair<Copyable, String> result = isCopyableTo(rhsIntentExtras,
                    lhsIntentExtras);

            //A string for a particular receiveIntent method of the format:
            //com.package.ReceiverComponent.receiveIntent()
            String receiveIntentMethodString = receiveIntentTree.fst + "." +
                    receiverMethodName.split(",")[0] + "()";

            switch(result.fst) {
            case MISSING_KEY:
                //if missing key String == null, the sender has type IntentMapBottom 
                checker.report(Result.failure("send.intent.missing.key",
                        intentObject.toString(), receiveIntentMethodString,
                        result.snd, intentObject.toString(), 
                        receiveIntentMethodString, intentObject.toString(), 
                        rhsIntentExtras.toString(), receiveIntentMethodString, 
                        lhsIntentExtras.toString()), node);
                break;  
            case INCOMPATIBLE_TYPES:
                checker.report(Result.failure("send.intent.incompatible.types",
                        intentObject.toString(), receiveIntentMethodString,
                        result.snd, intentObject.toString(), 
                        receiveIntentMethodString, intentObject.toString(), 
                        rhsIntentExtras.toString(), receiveIntentMethodString, 
                        lhsIntentExtras.toString()), node);
                break;
            case IS_COPYABLE:
            default:
                break;
            }
        }
    }

    /**
     * Get MethodSymbol based on class name, method name, and parameter length.
     * Note: This code was reused from the Reflection analysis.
     * 
     * @param className
     *            is the name of the receiver component
     * @param methodName
     *            is always getIntent() for now, but later will be used for
     *            other callback methods.
     * @return the corresponding method symbol or <code>null</code> if the
     *         method is not unique or cannot be determined
     */
    private Symbol getMethodSymbolFor(String className, String methodName,
            int paramLength, Env<AttrContext> env) {
        Context context = ((JavacProcessingEnvironment) checker
                .getProcessingEnvironment()).getContext();
        Resolve resolve = Resolve.instance(context);
        Names names = Names.instance(context);

        Symbol result = null;
        Method loadClass;
        try {
            loadClass = Resolve.class.getDeclaredMethod("loadClass", Env.class,
                    Name.class);
            loadClass.setAccessible(true);
            Symbol symClass;
            symClass = (Symbol) loadClass.invoke(resolve, env,
                    names.fromString(className));
            if (!symClass.exists()) {
                return result;
            }

            // TODO: We have to iterate over all super classes as the method(s)
            // we are looking for might be declared there
            for (Symbol s : symClass.getEnclosedElements()) {
                // Check all member methods
                if (s.getKind() == ElementKind.METHOD) {
                    // Check for method name and number of arguments
                    if (names.fromString(methodName).equals(s.name)
                            && ((MethodSymbol) s).getParameters().size() == paramLength) {
                        if (result != null) {
                            // Method name and parameter length is not unique in
                            // given class
                            result = null;
                            break;// outer;
                        }
                        result = s;
                    }
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException
                | NoSuchMethodException | SecurityException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Read the component map file and returns the receivers from a sender
     * 
     * @param sender
     *            sender name
     * @param tree
     * @return
     */

    public Set<String> getReceiversFromSender(String sender, Object tree) {
        Map<String, Set<String>> map = intentATF.getComponentMap().getIntentMap();
        Set<String> receivers = intentATF.getComponentMap().getIntentMap().get(sender);
        if (receivers == null)
            receivers = new HashSet<>();
        // TODO: this is ignoring actions in the sending component.
        for (Entry<String, Set<String>> entry : map.entrySet()) {
            if (entry.getKey().startsWith(sender)) {
                receivers.addAll(entry.getValue());
            }
        }
        if (receivers == null || receivers.isEmpty()) {
            checker.report(
                    Result.failure("intent.receiver.notfound",
                            sender.toString()), tree);
            return new HashSet<>();
        }
        return receivers;
    }

    /**
     * This method is responsible for obtaining the list of all receivers
     * methods from all possible receivers from a certain sendIntent() call.
     * getIntent() method for Activities, and callback methods for Services and
     * BroadcastReceivers.
     * 
     * @param tree
     *            the Tree of the sendIntent() call
     * @return A list of getIntent() methods
     */
    private List<Pair<String,MethodInvocationTree>> getReceiversMethods(
            MethodInvocationTree tree, String method, Set<String> receiversStrList) {

        List<Pair<String,MethodInvocationTree>> receiversList = 
                new ArrayList<Pair<String,MethodInvocationTree>>();

        // Resolve the Symbol for the current method
        for (String receiverStr : receiversStrList) {

            String[] methodAndParamLength = method.split(",");
            if (methodAndParamLength.length != 2) {
                checker.report(Result.failure("intent.error","\nmethod not in NAME,#Param format.  Correct the @SendIntent annotation.\n"+method), tree);
                return receiversList;
            }
            String methodName = methodAndParamLength[0];
            int paramLength = Integer.parseInt(methodAndParamLength[1]);
            JCMethodInvocation spoofedMethod = getSpoofedMethodCall(tree,
                    receiverStr, methodName, paramLength);
            if (spoofedMethod == null) {
                checker.report(Result.failure("intent.receiveintent.notfound",
                        methodName), tree);
            } else {
                receiversList.add(new Pair<String,MethodInvocationTree>
                        (receiverStr, spoofedMethod));
            }
        }

        return receiversList;
    }

    /**
     * Created a spoofed method invocation tree from a string method name TODO:
     * this code is duplicated from the reflection checker
     * 
     * @param tree
     * @param classname
     *            Name of the receiver of the spoofed method call
     * @param methodName
     *            Name of the spoofed method
     * @param paramLength
     *            Number of parameters in spoofed method
     * @param methodInvocation
     * @param context
     * @return
     */
    private JCMethodInvocation getSpoofedMethodCall(MethodInvocationTree tree,
            String classname, String methodName, int paramLength) {
        JCMethodInvocation methodInvocation = (JCMethodInvocation) tree;
        Context context = ((JavacProcessingEnvironment) checker
                .getProcessingEnvironment()).getContext();
        // make is used to select the getIntent() method on the loop below.
        TreeMaker make = TreeMaker.instance(context);
        TreePath path = atypeFactory.getPath(tree);
        JavacScope scope = (JavacScope) trees.getScope(path);
        Env<AttrContext> env = scope.getEnv();
        // Get receiver, which is always the first argument of the invoke method
        JCExpression receiver = methodInvocation.args.head;
        // The remaining list contains the arguments
        com.sun.tools.javac.util.List<JCExpression> args = methodInvocation.args.tail;
        Symbol symbol = getMethodSymbolFor(classname, methodName, paramLength,
                env);
        if (symbol != null) {
            JCExpression newMethod = make.Select(receiver, symbol);
            // Build method invocation tree depending on the number of
            // parameters
            JCMethodInvocation syntTree = paramLength > 0 ? make.App(newMethod,
                    args) : make.App(newMethod);
            // add method invocation tree to the list of possible methods
            return syntTree;
        } else {
            return null;
        }
    }

    /**
     * This method is called every time we are visiting a method from the Intent
     * class. If the method being visited is a putExtra or getExtra call, we
     * type check it.
     * 
     * @param method
     *            Annotations on the method
     * @param node
     *            The tree of the method call
     */
    private void checkIntentExtraMethods(AnnotatedExecutableType method,
            MethodInvocationTree node) {
        if (!(IntentUtils.isPutExtra(node, atypeFactory) || IntentUtils
                .isGetExtra(node, atypeFactory))) {
            return;
        }

        ExpressionTree receiver = TreeUtils.getReceiverTree(node);
        AnnotatedTypeMirror receiverType = atypeFactory
                .getAnnotatedType(receiver);
        if (IntentUtils.isIntentMapBottom(receiverType)) {
            checker.report(Result.failure("intent.not.initialized"), node);
            return;
        }

        // The first argument in putExtra and getExtra calls are keys
        List<String> keys = intentATF.getKeysFromPutExtraOrGetExtraCall(node);

        if (keys.isEmpty()) {
            checker.report(Result.failure("intent.key.variable", node
                    .getArguments().get(0)), node);
            return;
        }
        for (String key : keys) {
            checkKeyIsInIntentMap(method, node, key, receiver, receiverType);
        }
    }

    /**
     * Method used to verify if in <code>intent.getExtra(key)</code> and 
     * <code>intent.putExtra(key,val)</code> method calls, <code>key</code> can
     * be found in the @IntentMap from <code>intent</code>.
     * 
     * The modification of the return type of a getExtra(...)
     * method call is made in the IntentAnnotatedTypeFactory.methodFromUse().
     * 
     * @param method
     *            Actual getExtra or putExtra method
     * @param node
     *            Tree of the getExtra or putExtra method call, used only to
     *            show where a warning is being raised, in case it is.
     * @param keyName
     *            The key parameter of the getExtra(...) or putExtra(...) call.
     *            Ex: getExtra("key").
     * @param receiver
     *            Receiver component name in the String format. Ex: "ActivityA"
     * @param receiverType
     *            Annotations of the receiver calling intent.getExtra(...) or
     *            intent.putExtra(...).
     */

    private void checkKeyIsInIntentMap(AnnotatedExecutableType method,
            MethodInvocationTree node, String keyName, ExpressionTree receiver,
            AnnotatedTypeMirror receiverType) {
        if (receiverType.hasAnnotation(IntentMapBottom.class)) {
            return;
        }

        boolean isGetExtra = IntentUtils.isGetExtra(node, atypeFactory);
        if (receiverType.hasAnnotation(IntentMapNew.class)) {
            // In this case, the intent is always @Unique.
            // Allow any putExtra call and forbid any getExtra call (there is
            // no refinement for getExtra calls, so the key must exist in the
            // IntentMap.)
            if (isGetExtra) {
                checker.report(Result.failure("intent.key.notfound", keyName,
                        receiver.toString()), node);
            }
            return;
        }
        AnnotationMirror receiverIntentAnnotation = receiverType
                .getAnnotation(IntentMap.class);
        AnnotatedTypeMirror uniqueType = aliasingATF.getAnnotatedType(receiver);
        if (IntentUtils.getIExtra(receiverIntentAnnotation, keyName) == null) {
            // If key could not be found in the @IntentMap, raise a warning
            // if the type of the receiver is not @Unique.
            if (!uniqueType.hasAnnotation(Unique.class) || isGetExtra) {
                checker.report(Result.failure("intent.key.notfound", keyName,
                                receiver.toString()), node);
            }
        }
    }

    @Override
    public Void visitAnnotation(AnnotationTree node, Void p) {
        Element anno = TreeInfo.symbol((JCTree) node.getAnnotationType());
        if (anno.toString().equals(IntentMap.class.getName())
                || anno.toString().equals(Extra.class.getName())) {
            return null;
        }
        if (IntentUtils.notAllowedAnnos.contains(anno.toString())) {
            checker.report(Result.failure("annotation.not.allowed.in.src",
                            anno.toString()), node);
            return null;
        }
        return super.visitAnnotation(node, p);
    }

    /**
     * Any putExtra method call whose receiver is @Unique and the
     * key of the putExtra call is not in an @Extra of the declared type of the
     * receiver must type-check, because of the dataflow inference
     * feature which allows a value of any type to be added to an intent type
     * that is @Unique and whose key is missing from the declared type.
     * To avoid visiting checkArguments() in the scenario above,
     * this method is overridden and its super
     * implementation is called when it falls in the common method invocation
     * scenario.
     */
    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        if (!IntentUtils.isPutExtra(node, atypeFactory)) {
            return super.visitMethodInvocation(node, p);
        } else {
            ExpressionTree receiver = TreeUtils.getReceiverTree(node);
            AnnotatedTypeMirror receiverType = atypeFactory
                    .getAnnotatedType(receiver);
            AnnotatedTypeMirror aliasingType = aliasingATF
                    .getAnnotatedType(receiver);
            if (!aliasingType.hasAnnotation(Unique.class) || IntentUtils.
                    isIntentMapBottom(receiverType)) {
                return super.visitMethodInvocation(node, p);
            }

            List<String> keys = intentATF.getKeysFromPutExtraOrGetExtraCall(node);
            for (String key : keys) {
                if (IntentUtils.getDeclaredTypeIExtra(receiverType, key) != null) {
                    return super.visitMethodInvocation(node, p);
                }
            }
            return null;
        }
    }

    /**
     * This method receives annotations of 2 Intents and returns true if rhs can
     * be sent to lhs. For that to happen, every key in lhs needs to exist in
     * rhs, and the @Source and @Sink with that key in rhs needs to be a subtype
     * of the @Source and @Sink with that same key in lhs.
     * @param rhs
     *            Sender intent annotations
     * @param lhs
     *            Receiver intent annotations
     * @return true if the intent with annotations rhs can be sent to the intent
     *         with annotations lhs.
     */

    private Pair<Copyable,String> isCopyableTo(AnnotationMirror rhs, AnnotationMirror lhs) {
        assert !AnnotationUtils.areSameByClass(rhs, IntentMapBottom.class) :
            "rhs can't be @IntentMapBottom.";

        if (rhs == null || lhs == null) {
            return new Pair<IntentVisitor.Copyable, String>(Copyable.
                    INCOMPATIBLE_TYPES, null);
        }

        if (AnnotationUtils.areSameByClass(rhs, IntentMapNew.class)) {
            return isCopyable;
        }

        List<AnnotationMirror> rhsIExtrasList = AnnotationUtils
                .getElementValueArray(rhs, "value", AnnotationMirror.class,
                        true);
        List<AnnotationMirror> lhsIExtrasList = AnnotationUtils
                .getElementValueArray(lhs, "value", AnnotationMirror.class,
                        true);
        if (lhsIExtrasList.isEmpty()) {
            return isCopyable;
        }

        // Iterating on the @Extra from lhs
        for (AnnotationMirror lhsIExtra : lhsIExtrasList) {
            boolean found = false;
            String leftKey = IntentUtils.getKeyName(lhsIExtra);
            for (AnnotationMirror rhsIExtra : rhsIExtrasList) {
                String rightKey = IntentUtils.getKeyName(rhsIExtra);
                if (rightKey.equals(leftKey)) {
                    // Found 2 @Extra with same keys in rhs and lhs.
                    // Now we need to make sure that @Source and @Sink of
                    // the @Extra in rhs are subtypes of @Source and @Sink
                    // of @Extra in lhs.
                    found = true;
                    if (!hostIsCopyableTo(lhsIExtra, rhsIExtra)) {
                        return new Pair<IntentVisitor.Copyable, String>(Copyable.
                                INCOMPATIBLE_TYPES, leftKey);
                    } else {
                        // if it is a subtype, exit the inner loop and
                        // check another @Extra in lhs
                        break;
                    }
                }
            }
            if (!found) {
                // If key is missing in rhs, return a pair containing the key
                return new Pair<IntentVisitor.Copyable, String>(Copyable.
                        MISSING_KEY, leftKey);
            }
        }
        return isCopyable;
    }

    /**
     *  Enum that represents the result of a isCopyable method call.
     *  0 = is copyable.
     *  1 = is not copyable because LHS has more keys.
     *  2 = is not copyable because types are incompatible.
     */
    
    private enum Copyable {
        IS_COPYABLE (0),
        MISSING_KEY (1),
        INCOMPATIBLE_TYPES (2);
        
        Copyable(int isCopyable) { 
        }
        
    }
    
    /**
     * temporary auxiliar method used to type-check the isCopyableTo rule for
     * information flow analysis on intents.
     * 
     * @param lhsIExtra
     * @param rhsIExtra
     * @return
     */

    private boolean hostIsCopyableTo(AnnotationMirror lhsIExtra,
            AnnotationMirror rhsIExtra) {
        Set<PFPermission> lhsAnnotatedSources = IntentUtils
                .getSourcesPFP(lhsIExtra);
        Set<PFPermission> lhsAnnotatedSinks = IntentUtils
                .getSinksPFP(lhsIExtra);
        Set<PFPermission> rhsAnnotatedSources = IntentUtils
                .getSourcesPFP(rhsIExtra);
        Set<PFPermission> rhsAnnotatedSinks = IntentUtils
                .getSinksPFP(rhsIExtra);

        // Creating dummy type to add annotations to it and check if isSubtype
        TypeMirror dummy = atypeFactory.getProcessingEnv().getTypeUtils()
                .getPrimitiveType(TypeKind.BOOLEAN);
        AnnotatedTypeMirror lhsAnnotatedType = AnnotatedTypeMirror.createType(
                dummy, atypeFactory,false);
        AnnotatedTypeMirror rhsAnnotatedType = AnnotatedTypeMirror.createType(
                dummy, atypeFactory,false);

        AnnotationMirror lhsSourceAnnotation = atypeFactory
                .createAnnoFromSource(lhsAnnotatedSources);
        AnnotationMirror lhsSinkAnnotation = atypeFactory
                .createAnnoFromSink(lhsAnnotatedSinks);
        AnnotationMirror rhsSourceAnnotation = atypeFactory
                .createAnnoFromSource(rhsAnnotatedSources);
        AnnotationMirror rhsSinkAnnotation = atypeFactory
                .createAnnoFromSink(rhsAnnotatedSinks);

        lhsAnnotatedType.addAnnotation(lhsSourceAnnotation);
        lhsAnnotatedType.addAnnotation(lhsSinkAnnotation);
        rhsAnnotatedType.addAnnotation(rhsSourceAnnotation);
        rhsAnnotatedType.addAnnotation(rhsSinkAnnotation);

        // This is necessary to guarantee that the number of annotations is 3 
        // (@Souce, @Sink, @IntentMap/@IntentMapBottom) when
        //performing isSubtype operation.
        lhsAnnotatedType
                .addAnnotation(intentATF.TOP_INTENT_MAP);
        rhsAnnotatedType
                .addAnnotation(intentATF.TOP_INTENT_MAP);
        return atypeFactory.getTypeHierarchy().isSubtype(rhsAnnotatedType,
                lhsAnnotatedType);
    }

}
