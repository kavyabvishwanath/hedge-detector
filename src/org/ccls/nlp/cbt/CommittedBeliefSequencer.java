package org.ccls.nlp.cbt;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.ccls.nlp.cbt.ts.BeliefAnn;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.util.JCasUtil;

public class CommittedBeliefSequencer extends org.apache.uima.analysis_component.JCasAnnotator_ImplBase {

	public static Map<List<String>, Integer> sequences = new HashMap<List<String>, Integer>();	
	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		List<Sentence> sentences = new ArrayList<Sentence>(JCasUtil.select(jCas, Sentence.class));
		for (Sentence sentence : sentences) {
			List<String> beliefSequence = new ArrayList<String>();
			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
			for (Token token : tokens) {
				List<BeliefAnn> beliefTags = JCasUtil.selectCovered(jCas, BeliefAnn.class, token);
				if (beliefTags != null && beliefTags.size() == 1) {
					beliefSequence.add(beliefTags.get(0).getTag());
				}
			}
			if (!sequences.containsKey(beliefSequence)) {
				sequences.put(beliefSequence, 0);
			}
			sequences.put(beliefSequence, 1 + sequences.get(beliefSequence));
		}
	}

}