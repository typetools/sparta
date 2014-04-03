package sparta.checkers.intents;


import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.util.AnnotatedTypes;

import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.InternalUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

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

import sparta.checkers.FlowAnnotatedTypeFactory;
import sparta.checkers.FlowVisitor;
import sparta.checkers.quals.Extra;
import sparta.checkers.quals.IntentMap;
import sparta.checkers.quals.ParameterizedFlowPermission;
import sparta.checkers.quals.ReceiveIntent;
import sparta.checkers.quals.SendIntent;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.code.Symbol;
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

public class IntentVisitor extends FlowVisitor {

    public IntentVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    protected FlowAnnotatedTypeFactory createTypeFactory() {
        try {
            return new IntentAnnotatedTypeFactory(checker);
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

        if (null != receiveAnnoOverriden) {
            return checkReceiveIntentOverride(overriderTree,
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
            AnnotatedExecutableType implementingMethod,
            AnnotationMirror overridenAnno) {
        AnnotationMirror implementerAnno = atypeFactory.getDeclAnnotation(
                implementingMethod.getElement(), ReceiveIntent.class);

        if (null == implementerAnno) {
            checker.report(Result.failure("intent.override.receiveintent",
                    overridenAnno.toString()), implementingTree);
            return false;

        } else if (!AnnotationUtils.areSame(overridenAnno, implementerAnno)) {
            checker.report(Result.failure(
                    "intent.override.sendintent.incorrect",
                    overridenAnno.toString()), implementingTree);
            return false;
        }

        return true;
    }

    @Override
    protected void checkMethodInvocability(AnnotatedExecutableType method,
            MethodInvocationTree node) {
        
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
        
        //Get the @IntenMap annotation for the correct parameter
        ExpressionTree intentObject = node.getArguments().get(paramIndex);
        AnnotatedTypeMirror rhs = atypeFactory.getAnnotatedType(intentObject);
        AnnotationMirror rhsIntentExtras = rhs.getAnnotation(IntentMap.class);
        
        //the getIntent is the only receiveIntent method 
        //where the receiving intent is the return type,
        //so we handle it separately 
        if (receiverMethodName.equals(IntentUtils.GET_INTENT)) {
            checkReceivingGetIntent(method, node, rhsIntentExtras);
            return;
        }

        for (MethodInvocationTree receiveIntentTree : getReceiversMethods(node,
                receiverMethodName)) {
            ExecutableElement methodElt = TreeUtils
                    .elementFromUse(receiveIntentTree);
            AnnotatedTypeMirror receiverType = atypeFactory
                    .getReceiverType(receiveIntentTree);
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
                    .getAnnotation(IntentMap.class);
            if (!isCopyableTo(rhsIntentExtras, lhsIntentExtras)) {
                checker.report(
                        Result.failure("send.intent",
                                rhsIntentExtras.toString(),
                                lhsIntentExtras.toString()), node);
            }
        }
    }

    /**
     * When receiveIntent is getIntent, it has to be handled differently because
     * the receiving Intent is the return type rather than a parameter.
     * 
     * @param method
     * @param node
     * @param rhsIntentExtras
     */
    private void checkReceivingGetIntent(AnnotatedExecutableType method,
            MethodInvocationTree node, AnnotationMirror rhsIntentExtras) {

        for (MethodInvocationTree getIntentCall : getReceiversMethods(node,
                IntentUtils.GET_INTENT)) {
            AnnotatedTypeMirror lhs = atypeFactory
                    .getAnnotatedType(getIntentCall);
            AnnotationMirror lhsIntentExtras = lhs
                    .getAnnotation(IntentMap.class);
            if (!isCopyableTo(rhsIntentExtras, lhsIntentExtras)) {
                checker.report(
                        Result.failure("send.intent",
                                rhsIntentExtras.toString(),
                                lhsIntentExtras.toString()), node);
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
        Map<String, Set<String>> map = ((IntentAnnotatedTypeFactory) atypeFactory)
                .getComponentMap().getIntentMap();
        Set<String> receivers = ((IntentAnnotatedTypeFactory) atypeFactory)
                .getComponentMap().getIntentMap().get(sender);
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
    private List<MethodInvocationTree> getReceiversMethods(
            MethodInvocationTree tree, String method) {

        JCMethodInvocation methodInvocation = (JCMethodInvocation) tree;
        Context context = ((JavacProcessingEnvironment) checker
                .getProcessingEnvironment()).getContext();
        // Return the sender intent in the String format. Check method @Javadoc.
        String senderString = IntentUtils.retrieveSendIntentPath(getCurrentPath());
        // Getting the receivers components in the String format from the
        // Component map
        Set<String> receiversStrList = getReceiversFromSender(senderString,
                tree);
        List<MethodInvocationTree> receiversList = new ArrayList<MethodInvocationTree>();

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
                    receiverStr, methodName, paramLength, methodInvocation,
                    context);
            if (spoofedMethod == null) {
                checker.report(Result.failure("intent.receiveintent.notfound",
                        methodName), tree);
            } else {
                receiversList.add(spoofedMethod);
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
            String classname, String methodName, int paramLength,
            JCMethodInvocation methodInvocation, Context context) {
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
        // The first argument in putExtra and getExtra calls are keys
        List<String> keys = ((IntentAnnotatedTypeFactory) atypeFactory)
                .getKeysFromPutExtraOrGetExtraCall(node);

        if (keys.isEmpty()) {
            checker.report(Result.failure("intent.key.variable", node
                    .getArguments().get(0)), node);
            return;
        }
        for (String key : keys) {
            ExpressionTree receiver = TreeUtils.getReceiverTree(node);
            AnnotatedTypeMirror receiverType = atypeFactory
                    .getAnnotatedType(receiver);
            if (IntentUtils.isPutExtra(node, atypeFactory)) {
                //Check not required since it is done by the checkAssignment() now that the 
                //type of value in a putExtra(key,value) call is assigned in the IAFT.
//                checkPutExtra(node, key, receiver, receiverType);
            } else if (IntentUtils.isGetExtra(node, atypeFactory)) {
                checkGetExtra(method, node, key, receiver, receiverType);
            }
        }
    }

    /**
     * Method used to type-check a <code>intent.getExtra(...)</code> method
     * call. Here we just need to check if the key can be found in the @IntentMap
     * from <code>intent</code>. The modification of the return type of this
     * method is made in the IntentAnnotatedTypeFactory.
     * 
     * @param method
     *            Actual getExtra method
     * @param node
     *            Tree of the getExtra() method call, used only to show where a
     *            warning is being raised, in case it is.
     * @param keyName
     *            The key parameter of the getExtra(...) call. Ex:
     *            getExtra("key").
     * @param receiver
     *            Receiver component name in the String format. Ex: "ActivityA"
     * @param receiverType
     *            Annotations of the receiver calling intent.getExtra(...).
     */

    private void checkGetExtra(AnnotatedExecutableType method,
            MethodInvocationTree node, String keyName, ExpressionTree receiver,
            AnnotatedTypeMirror receiverType) {
        if (receiverType.hasAnnotation(IntentMap.class)) {
            AnnotationMirror receiverIntentAnnotation = receiverType
                    .getAnnotation(IntentMap.class);
            if (!IntentUtils.hasKey(receiverIntentAnnotation, keyName)) {
                // If key could not be found in the @IntentMap, raise a warning.
                checker.report(
                        Result.failure("intent.key.notfound", keyName,
                                receiver.toString()), node);
            }
        }
    }

    /**
     * Method to type check the putExtra call. In every putExtra("key",value)
     * call the "key" needs to be found in one of the @Extra elements from the @IntentMap
     * of the intent, and the @Source and @Sink from the @Extra need to be a
     * supertype of the @Source and @Sink of value. TODO In the future, we want
     * to add inference to the putExtra() call. If the key cannot be found in
     * the @IntentMap annotation, intent.putExtra("key",value) should update the
     * intent's @IntentExtra annotation adding a new @Extra with key "key" and @Source
     * and @Sink of value.
     * 
     * @param node
     *            The tree of the putExtra method call
     * @param keyName
     *            The key of the element being added to the intent bundle
     * @param receiver
     *            Receiver component name in the String format. Ex: ActivityA
     * @param receiverType
     *            Annotations of the receiver calling intent.putExtra(...).
     */
    private void checkPutExtra(MethodInvocationTree node, String keyName,
            ExpressionTree receiver, AnnotatedTypeMirror receiverType) {
        AnnotationMirror iExtra = IntentUtils.getIExtra(receiverType, keyName);
        if (iExtra == null) {
            checker.report(
                    Result.failure("intent.key.notfound", keyName,
                            receiver.toString()), node);
            return;
        }

        // Getting annotations of the value being added to the intent
        // bundle.
        AnnotatedTypeMirror valueType = atypeFactory.getAnnotatedType(node
                .getArguments().get(1));
        AnnotatedTypeMirror declaredKeyType = hostGetTypeOfKey(iExtra,
                valueType.getUnderlyingType());

        // Check if the value being added to the intent bundle is a
        // subtype
        // of what the @IntentMap annotation contains.
        if (!atypeFactory.getTypeHierarchy().isSubtype(valueType,
                declaredKeyType)) {
            checker.report(Result.failure("intent.type.incompatible",
                    receiver.toString(), keyName,
                    declaredKeyType.getAnnotation(Source.class),
                    declaredKeyType.getAnnotation(Sink.class),
                    valueType.getAnnotation(Source.class),
                    valueType.getAnnotation(Sink.class)), node);
        }
        // TODO: Inference on putExtra
        // If key couldn't be found, add it to the @IntentMap with the
        // flow of the object being inserted on the hashmap.
        // if(!found) {
        // IntentChecker intentChecker = (IntentChecker) checker;
        // AnnotationMirror newIExtra =
        // intentChecker.createIExtraAnno(keyName,
        // valueType.getAnnotation(Source.class),
        // valueType.getAnnotation(Sink.class));
        // AnnotationMirror newIntentExtras =
        // intentChecker.addIExtraToIntentExtras(receiverIntentAnnotation,
        // newIExtra);
        // receiverType.replaceAnnotation(newIntentExtras);
        // System.out.println();
        // }

    }

    private AnnotatedTypeMirror hostGetTypeOfKey(AnnotationMirror iExtra,
            TypeMirror javaType) {
        Set<ParameterizedFlowPermission> annotatedSources = IntentUtils
                .getSourcesPFP(iExtra);
        Set<ParameterizedFlowPermission> annotatedSinks = IntentUtils
                .getSinksPFP(iExtra);

        // Creating a type with the @Source and @Sink from value in
        // putExtra(key,value).
        AnnotationMirror sourceAnnotation = atypeFactory
                .createAnnoFromSource(annotatedSources);
        AnnotationMirror sinkAnnotation = atypeFactory
                .createAnnoFromSink(annotatedSinks);

        // annotatedType is a dummy type containing the @Source and
        // @Sink
        // from the IExtra, only to be used in the isSubType() call.
        AnnotatedTypeMirror annotatedType = AnnotatedTypeMirror.createType(
                javaType, atypeFactory);
        // Handling array types.
        if (annotatedType instanceof AnnotatedArrayType) {
            ((AnnotatedArrayType) annotatedType).getComponentType()
                    .addAnnotation(sourceAnnotation);
            ((AnnotatedArrayType) annotatedType).getComponentType()
                    .addAnnotation(sinkAnnotation);
            ((AnnotatedArrayType) annotatedType)
                    .getComponentType()
                    .addAnnotation(
                            ((IntentAnnotatedTypeFactory) atypeFactory).EMPTYINTENTEXTRAS);
        }
        annotatedType.addAnnotation(sourceAnnotation);
        annotatedType.addAnnotation(sinkAnnotation);
        annotatedType
                .addAnnotation(((IntentAnnotatedTypeFactory) atypeFactory).EMPTYINTENTEXTRAS);
        return annotatedType;
    }

    @Override
    public Void visitAnnotation(AnnotationTree node, Void p) {
        List<? extends ExpressionTree> args = node.getArguments();
        if (args.isEmpty()) {
            // Nothing to do if there are no annotation arguments.
            return null;
        }

        Element anno = TreeInfo.symbol((JCTree) node.getAnnotationType());
        if (anno.toString().equals(IntentMap.class.getName())
                || anno.toString().equals(Extra.class.getName())) {
            return null;
        }
        return super.visitAnnotation(node, p);
    }

    /**
     * This method receives annotations of 2 Intents and returns true if rhs can
     * be sent to lsh. For that to happen, every key in lhs need to exists in
     * rhs, and the @Source and @Sink with that key in rhs needs to be a subtype
     * of the
     * 
     * @Source and @Sink with that same key in lhs.
     * @param rhs
     *            Sender intent annotations
     * @param lhs
     *            Receiver intent annotations
     * @return true if the intent with annotations rhs can be sent to the intent
     *         with annotations lhs.
     */

    public boolean isCopyableTo(AnnotationMirror rhs, AnnotationMirror lhs) {
        if (rhs == null || lhs == null) {
            return false;
        }
        List<AnnotationMirror> rhsIExtrasList = AnnotationUtils
                .getElementValueArray(rhs, "value", AnnotationMirror.class,
                        true);
        List<AnnotationMirror> lhsIExtrasList = AnnotationUtils
                .getElementValueArray(lhs, "value", AnnotationMirror.class,
                        true);
        if (rhsIExtrasList.isEmpty()) {
            return true;
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
                        return false;
                    } else {
                        // if it is a subtype, exit the inner loop and
                        // check another @Extra in lhs
                        break;
                    }
                }
            }
            if (!found) {
                // If key is missing in the rhs, return false
                return false;
            }
        }
        return true;
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
        Set<ParameterizedFlowPermission> lhsAnnotatedSources = IntentUtils
                .getSourcesPFP(lhsIExtra);
        Set<ParameterizedFlowPermission> lhsAnnotatedSinks = IntentUtils
                .getSinksPFP(lhsIExtra);
        Set<ParameterizedFlowPermission> rhsAnnotatedSources = IntentUtils
                .getSourcesPFP(rhsIExtra);
        Set<ParameterizedFlowPermission> rhsAnnotatedSinks = IntentUtils
                .getSinksPFP(rhsIExtra);

        // Creating dummy type to add annotations to it and check if isSubtype
        TypeMirror dummy = atypeFactory.getProcessingEnv().getTypeUtils()
                .getPrimitiveType(TypeKind.BOOLEAN);
        AnnotatedTypeMirror lhsAnnotatedType = AnnotatedTypeMirror.createType(
                dummy, atypeFactory);
        AnnotatedTypeMirror rhsAnnotatedType = AnnotatedTypeMirror.createType(
                dummy, atypeFactory);

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

        // Why is this necessary?
        // How to set atypeFactory to IntentAnnotatedTypeFactory instead of
        // FlowAnnotatedTypeFactory?
        IntentAnnotatedTypeFactory intentAnnotatedTypeFactory = (IntentAnnotatedTypeFactory) atypeFactory;
        lhsAnnotatedType
                .addAnnotation(intentAnnotatedTypeFactory.EMPTYINTENTEXTRAS);
        rhsAnnotatedType
                .addAnnotation(intentAnnotatedTypeFactory.EMPTYINTENTEXTRAS);
        return atypeFactory.getTypeHierarchy().isSubtype(rhsAnnotatedType,
                lhsAnnotatedType);
    }

}
