package sparta.checkers.intents;

import java.util.Arrays;
import java.util.List;

import javacutils.InternalUtils;
import javacutils.TreeUtils;

import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;

import sparta.checkers.ComponentMap;
import sparta.checkers.FlowChecker;
import sparta.checkers.FlowPolicy;
import sparta.checkers.quals.IExtra;
import sparta.checkers.quals.IntentExtras;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import checkers.quals.PolyAll;
import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.source.SupportedLintOptions;

import com.sun.source.tree.MethodInvocationTree;

@TypeQualifiers({ Source.class, Sink.class, PolySource.class, PolySink.class,
		PolyAll.class, IntentExtras.class, IExtra.class })
@StubFiles("flow.astub")
@SupportedOptions({ FlowPolicy.POLICY_FILE_OPTION, ComponentMap.COMPONENT_MAP_FILE_OPTION,
		FlowChecker.MSG_FILTER_OPTION, FlowChecker.IGNORE_NOT_REVIEWED })
@SupportedLintOptions({ FlowPolicy.STRICT_CONDITIONALS_OPTION })
public class IntentChecker extends FlowChecker {

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
