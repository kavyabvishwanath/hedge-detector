package org.ccls.nlp.cbt.FeatureExtractors;

import java.util.Arrays;
import java.util.List;

/**
 * This contains common collections to be used across feature extraction classes
 * @author OFFLINE-GREG
 *
 */
public final class ExtractorCommons {

	public static final List<String> QUESTION_WORDS = Arrays.asList("who", "where", "when", "why", "what", "which", "how");
	public static final List<String> MODAL_WORDS = Arrays.asList("can", "could", "must", "might", "should", "would", "may",
																"shall","seem","believe","think","appear","impossibly","positively",
																"possibly","scarcely","certainly","definitely","surely","unquestionably","seriously",
																"apparently","obviously","rarely","occasionally","chance","opportunity","possibility",
																"necessity","capacity","certainty","potential","possible","certain","definite",
																"clear","probable","likely","unlikely","total","essential","absolute","complete");
    public static final List<String> AUXILIARY_VERBS = Arrays.asList("am","are","is","was","were","being","been","can","could",
    		                                                         "dare", "does", "did", "has", "had", "having", "may", "might", "must",
    		                                                         "need","ought","shall","should","will","would");
	
	public static final List<String> REPORTING_VERBS = Arrays.asList("tell", "accuse", "insist", "seem", "believe", "say", "find", "conclude", 
			                                                         "claim", "trust", "think", "suspect", "doubt", "suppose");

	public static final List<String> EXTENDED_REPORTING_VERBS = Arrays.asList("treat","prevent","induce","cause","contain","consist");

}
