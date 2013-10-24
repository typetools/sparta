package sparta.checkers;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javacutils.AnnotationUtils;
import javacutils.Pair;
import javacutils.TreeUtils;

import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.IExtra;
import sparta.checkers.quals.IntentExtras;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import checkers.basetype.BaseTypeChecker;
import checkers.quals.PolyAll;
import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.source.Result;
import checkers.source.SupportedLintOptions;
import checkers.types.AbstractBasicAnnotatedTypeFactory;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.QualifierHierarchy;
import checkers.util.AnnotationBuilder;
import checkers.util.MultiGraphQualifierHierarchy;
import checkers.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import checkers.util.QualifierPolymorphism;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

@TypeQualifiers({ Source.class, Sink.class, PolySource.class, PolySink.class,
		PolyAll.class, IntentExtras.class, IExtra.class })
@StubFiles("flow.astub")
@SupportedOptions({ FlowPolicy.POLICY_FILE_OPTION,
		FlowChecker.MSG_FILTER_OPTION, FlowChecker.IGNORE_NOT_REVIEWED })
@SupportedLintOptions({ FlowPolicy.STRICT_CONDITIONALS_OPTION })
public class IntentChecker extends FlowChecker {

	protected AnnotationMirror INTENTEXTRAS, IEXTRA, EMPTYINTENTEXTRAS;
	protected IntentPolicy intentPolicy;
	
	public IntentChecker() {
        super();
    }

	private static List<String> GETEXTRA_SIGNATURES = (List<String>) Arrays
			.asList(new String[] { "getStringExtra", "getStringArrayListExtra",
					"getStringArrayExtra", "getShortExtra",
					"getShortArrayExtra", "getSerializableExtra",
					"getParcelableExtra", "getParcelableArrayListExtra",
					"getParcelableArrayExtra", "getLongExtra",
					"getLongArrayExtra", "getIntegerArrayListExtra",
					"getIntExtra", "getIntArrayExtra", "getFloatExtra",
					"getFloatArrayExtra", "getDoubleExtra",
					"getDoubleArrayExtra", "getCharSequenceExtra",
					"getCharSequenceArrayListExtra",
					"getCharSequenceArrayExtra", "getCharExtra",
					"getCharArrayExtra", "getByteExtra", "getByteArrayExtra",
					"getBundleExtra", "getBooleanExtra", "getBooleanArrayExtra" });

	private static List<String> PUTEXTRA_SIGNATURES = (List<String>) Arrays
			.asList(new String[] { "putExtra", "putCharSequenceArrayListExtra",
					"putIntegerArrayListExtra", "putParcelableArrayListExtra",
					"putStringArrayListExtra" });

	@Override
	public void initChecker() {
		intentPolicy = new IntentPolicy(new File("intent-policy")); //TODO: Remove this, load intent policy from command line
		super.initChecker();
	}
	
	/**
	 * Returns true if the MethodInvocationTree corresponds to one of the <code>Intent.getExtra()</code> calls
	 * @param tree
	 * @return
	 */

	public boolean isGetExtraMethod(MethodInvocationTree tree) {
		for (String getExtraSignature : GETEXTRA_SIGNATURES) {
			//The getExtra call can have 1 or 2 parameters,
			//2 when there is a use of default parameter, 1 otherwise.
			ExecutableElement getExtra = TreeUtils.getMethod(
					"android.content.Intent", getExtraSignature, 1, processingEnv);
			ExecutableElement getExtraWithDefault = TreeUtils.getMethod(
					"android.content.Intent", getExtraSignature, 2, processingEnv);
			if (getExtra != null
					&& TreeUtils.isMethodInvocation(tree, getExtra,
							processingEnv)) {
				return true;
			}
			if (getExtraWithDefault != null
					&& TreeUtils.isMethodInvocation(tree, getExtraWithDefault,
							processingEnv)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the MethodInvocationTree corresponds to one of the <code>Intent.putExtra()</code> calls
	 * TODO: The problem is that there are several putExtra signatures with the same amount of parameters and name
	 * and the TreeUtils.getMethod() cannot differentiate between them, it always returns the putExtra(String,boolean).
	 * If tree is putExtra(String,String) it won't pass this method. I also think the isMethodAccess() is not working.
	 * Isn't it supposed to return true if the tree is a method call?
	 * @param tree
	 * @return
	 */

	//TODO: Fix this method! isMethodAccess not working as intended?
	public boolean isPutExtraMethod(MethodInvocationTree tree) {
		//isMethodAccess bugged?
		if (TreeUtils.isMethodAccess(tree)) {
			String methodName = TreeUtils.getMethodName(tree);
			for (String s : PUTEXTRA_SIGNATURES) {
				if (s.equals(methodName)) {
					return true;
				}
				// correct way to do it. the problem is that there are several
				// putExtra methods with the same name and all
				// of them has the same number of paremeters. How to get each
				// one of them? TreeUtils.getMethod returns only
				// the first one.
				// ExecutableElement putExtra = TreeUtils.getMethod(
				// "android.content.Intent", s, 2, processingEnv);
				// if (putExtra != null
				// && TreeUtils.isMethodInvocation(tree, putExtra,
				// processingEnv)) {
				// return true;
				// }
			}
		}
		return false;
	}

	public IntentPolicy getIntentPolicy() {
		return intentPolicy;
	}

	public Set<String> getReceiversFromSender(String sender) {
		Set<String> receivers = getIntentPolicy().getIntentMap().get(sender);
		if (receivers == null || receivers.isEmpty()) {
			errorAbort("Could not find receivers for class: " + sender);
		}
		return receivers;
	}

	// Uncomment below to only report intent type checks.
	// @Override
	// public void report(Result r, Object src) {
	// List<String> messageKeys = r.getMessageKeys();
	// if (messageKeys.contains("intent.key.notfound")
	// || messageKeys.contains("intent.check.notcompatible")
	// || messageKeys.contains("send.intent")
	// || messageKeys.contains("getintent.not.found")) {
	// super.report(r, src);
	// }
	// }

	

}
