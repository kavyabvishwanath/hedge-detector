package org.ccls.nlp.cleartk.tweakers;
/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-stanford-corenlp project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Feature;
import org.cleartk.stanford.StanfordCoreNLPComponents;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class StanfordCoreNLPSentenceSplitter extends JCasAnnotator_ImplBase {

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(
				StanfordCoreNLPSentenceSplitter.class,
				StanfordCoreNLPComponents.TYPE_SYSTEM_DESCRIPTION);
	}

	private StanfordCoreNLP processor;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		Properties properties = new Properties();
		properties.put("annotators", 
						"tokenize, "
						+ "ssplit, "
						);
		properties.put("ssplit.eolonly", "true");

		this.processor = new StanfordCoreNLP(properties);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {		
		Annotation document = this.processor.process(jCas.getDocumentText());

		for (CoreMap tokenAnn : document.get(TokensAnnotation.class)) {

			// create the token annotation
			int begin = tokenAnn.get(CharacterOffsetBeginAnnotation.class);
			int end = tokenAnn.get(CharacterOffsetEndAnnotation.class);
			String pos = tokenAnn.get(PartOfSpeechAnnotation.class);
			String lemma = tokenAnn.get(LemmaAnnotation.class);
			Token token = new Token(jCas, begin, end);
			token.setPos(pos);
			token.setLemma(lemma);
			token.addToIndexes();
		}
		
		// add sentences and trees
		for (CoreMap sentenceAnn : document.get(SentencesAnnotation.class)) {

			// add the sentence annotation
			int sentBegin = sentenceAnn.get(CharacterOffsetBeginAnnotation.class);
			int sentEnd = sentenceAnn.get(CharacterOffsetEndAnnotation.class);
			Sentence sentence = new Sentence(jCas, sentBegin, sentEnd);
			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
			int sentenceSize = tokens.size();
			if (sentenceSize > 20) {
				int halfway = sentenceSize / 2;
				int splitPt = tokens.get(halfway - 1).getEnd();
				sentence = new Sentence(jCas, sentBegin, splitPt);
				Sentence sentence2 = new Sentence(jCas, splitPt, sentEnd);
				sentence.addToIndexes();
				sentence2.addToIndexes();
			} else {
				sentence.addToIndexes();
			}
		}
	}

	public static void main(String args[]) {
		//String sentence = "That could include firing across the bow to halt a ship.  But his sweeping statement contained no detials, and gave no indication of how the disputes could be resolved.";
		String sentence = "The dog may eat his dinner.";
		LexicalizedParser lp = LexicalizedParser.getParserFromFile("englishPCFG.ser.gz", new Options());
		Tree parse = lp.parse(sentence);
		parse.pennPrint();
		String pennTree = parse.pennString();
		Feature tree = new Feature("TK_tree", StringUtils.normalizeSpace(pennTree));
		System.out.println(tree.toString());
	}
}
