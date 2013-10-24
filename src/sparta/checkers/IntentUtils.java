package sparta.checkers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javacutils.AnnotationUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.util.AnnotationBuilder;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.IExtra;
import sparta.checkers.quals.IntentExtras;

public class IntentUtils {

	/**
	 * Method that receives an @IntentExtras and a <code> key </code>
	 * and return the @IExtra with that key and <code>null</code> if it 
	 * does not contain the key.
	 */
	public static AnnotationMirror getIExtraWithKey(AnnotationMirror intentExtras, String key) {
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
	
	public static Set<FlowPermission> unionSourcesIExtras(AnnotationMirror iExtra1, AnnotationMirror iExtra2) {
		Set<FlowPermission> a1AnnotatedSources = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra1, "source",
								FlowPermission.class,
								true));
		Set<FlowPermission> a2AnnotatedSources = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra2, "source",
								FlowPermission.class,
								true));
		a1AnnotatedSources.addAll(a2AnnotatedSources);
		return Flow.convertToAnySource(a1AnnotatedSources,true);

	}
	
	/**
	 * Return the union of sinks from 2 @IExtra annotations
	 */
	
	public static Set<FlowPermission> unionSinksIExtras(AnnotationMirror iExtra1, AnnotationMirror iExtra2) {
		Set<FlowPermission> a1AnnotatedSinks = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra1, "sink",
								FlowPermission.class,
								true));
		Set<FlowPermission> a2AnnotatedSinks = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra2, "sink",
								FlowPermission.class,
								true));
		a1AnnotatedSinks.addAll(a2AnnotatedSinks);
		return Flow.convertToAnySink(a1AnnotatedSinks,true);

	}
	
	/**
	 * Return the intersection of sources from 2 @IExtra annotations
	 */
	
	public static Set<FlowPermission> intersectionSourcesIExtras(AnnotationMirror iExtra1, AnnotationMirror iExtra2) {
		Set<FlowPermission> a1AnnotatedSources = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra1, "source",
								FlowPermission.class,
								true));
		Set<FlowPermission> a2AnnotatedSources = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra2, "source",
								FlowPermission.class,
								true));
		return Flow.intersectSinks(a1AnnotatedSources, a2AnnotatedSources);

	}
	
	/**
	 * Return the intersection of sinks from 2 @IExtra annotations
	 */
	
	public static Set<FlowPermission> intersectionSinksIExtras(AnnotationMirror iExtra1, AnnotationMirror iExtra2) {
		Set<FlowPermission> a1AnnotatedSinks = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra1, "sink",
								FlowPermission.class,
								true));
		Set<FlowPermission> a2AnnotatedSinks = new HashSet<FlowPermission>(
				AnnotationUtils
						.getElementValueEnumArray(
								iExtra2, "sink",
								FlowPermission.class,
								true));
		return Flow.intersectSinks(a1AnnotatedSinks, a2AnnotatedSinks);

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
			AnnotationMirror sources, AnnotationMirror sinks, ProcessingEnvironment processingEnv) {
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
			AnnotationMirror intentExtras, AnnotationMirror iExtra, ProcessingEnvironment processingEnv) {
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
	
	public static AnnotationMirror addIExtrasToIntentExtras(AnnotationMirror intentExtras, List<AnnotationMirror> iExtras, ProcessingEnvironment processingEnv) {
		AnnotationMirror result = intentExtras;
		for (AnnotationMirror iExtra : iExtras) {
			result = addIExtraToIntentExtras(result, iExtra, processingEnv);
		}
		return result;
	}
	
	
}
