package org.ccls.nlp.cbt.FeatureExtractors;


import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.annotationpair.AnnotationPairFeatureExtractor;
import org.cleartk.token.type.Token;

public class PairFeatureExtractor implements AnnotationPairFeatureExtractor {

	@Override
	public List<Feature> extract(JCas arg0, Annotation arg1, Annotation arg2)
			throws CleartkExtractorException {
		List<Feature> features = new ArrayList<Feature>();

		Token token_m1 = (Token) arg1;
		Token token = (Token) arg2;

		if (token_m1 != null && token_m1.getCoveredText().toLowerCase().equals("to") && token.getPos().equals("VB")) {
			features.add(new Feature("FullInfinitive", "TRUE"));
		}
		if ((token_m1 == null || !token_m1.getCoveredText().toLowerCase().equals("to")) && token.getPos().equals("VB")) {
			features.add(new Feature("BareInfinitive", "TRUE"));
		}
		if (token_m1 != null && ExtractorCommons.MODAL_WORDS.contains(token_m1.getCoveredText().toLowerCase()) && token.getPos().equals("VB")) {
			features.add(new Feature("ModalInfinitive", "TRUE"));
		}
		if (token_m1 != null && ExtractorCommons.AUXILIARY_VERBS.contains(token_m1.getCoveredText().toLowerCase()) && token.getPos().equals("VBG")) {			
			features.add(new Feature("AuxVBG", "TRUE"));
		}
		if (token_m1 != null && token_m1.getLemma().toLowerCase().equals("be")) {			
			features.add(new Feature("BeLemmaToken", "TRUE"));
		}

		return features;
	}

}
