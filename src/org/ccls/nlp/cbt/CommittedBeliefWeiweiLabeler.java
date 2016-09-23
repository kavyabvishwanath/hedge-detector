package org.ccls.nlp.cbt;


import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.ccls.nlp.cbt.FeatureExtractors.WeiweiWSD;
import org.ccls.nlp.cbt.utils.FileUtils;
import org.cleartk.token.type.Sentence;
import org.uimafit.util.JCasUtil;

public class CommittedBeliefWeiweiLabeler extends org.apache.uima.analysis_component.JCasAnnotator_ImplBase {

	public static Boolean isTraining = null;
	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		String filename = FileUtils.getFilename(jCas);
		isTraining = false;
		if (!filename.contains("ABC19980114.1830.0611")) {
			return;
		}
				
		List<Sentence> sentences = new ArrayList<Sentence>(JCasUtil.select(jCas, Sentence.class));
		
		WeiweiWSD.weiweiBit(jCas, sentences, isTraining, false, true);
		
	}

}