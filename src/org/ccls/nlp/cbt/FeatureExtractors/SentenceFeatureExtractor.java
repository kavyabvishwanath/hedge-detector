package org.ccls.nlp.cbt.FeatureExtractors;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.ccls.nlp.cbt.HedgeClassifier;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.TreeFeature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.token.type.Token;
import org.cleartk.token.type.Sentence;
import org.uimafit.util.JCasUtil;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.trees.Tree;


public class SentenceFeatureExtractor implements SimpleFeatureExtractor {

	private LexicalizedParser lp;
	private boolean startOfSentence;
	private List<String> tokenText;
	private List<Token> tokens;
	private Token currentToken;
	private String pennTree;
	//Rupayan edit
	private String sentence;
	private Set<String> hedgePhrases;
	private int sentenceNumber;
	private Map<Token, HedgeClassifier.HedgeInfo> hedgeFeatures;


	public SentenceFeatureExtractor() {
		super();
		startOfSentence = true;
		setupHedgeSets();
		//lp = LexicalizedParser.getParserFromFile("englishPCFG.ser.gz", new Options());
	}
	
	private void setupHedgeSets() {
		hedgePhrases = new HashSet<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("hedge/multiword.txt"));
			while (br.ready()) {
				hedgePhrases.add(br.readLine());
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void common(JCas jCas, Annotation annotation) {
		tokens = JCasUtil.selectCovered(jCas, Token.class, annotation);
		tokenText = new ArrayList<String>();
		
		//Rupayan edit
		StringBuilder sb = new StringBuilder();
		for(Token token: tokens){
			sb.append(token.getCoveredText().toLowerCase());
			sb.append(" ");
//			sb.append(token.getLemma().toLowerCase());
//			sb.append(" ");
		}
		sentence = sb.toString();
		for (Token token : tokens) {
			// right now, svm-light-tk cannot handle right or left paren as token
			// what I suggest, is to simply eliminate those chars from sentence
			String covText = token.getCoveredText();
			if (covText.equals("(")) {
				tokenText.add("*LP*");
			} else if (covText.equals(")")) {
				tokenText.add("*RP*");
			} else {
				tokenText.add(covText);
			}
		}

		//Tree parse = lp.parseStrings(tokenText);
		//pennTree = parse.pennString();
	}

	@Override
	public List<Feature> extract(JCas jCas, Annotation annotation)
			throws CleartkExtractorException {

		List<Feature> hedges = new ArrayList<Feature>();

		if (isStartOfSentence()) {
			common(jCas, annotation);
		}
		//Risa's edit - Classifier -- extract hedge phrases on the sentence level, not on the token level
		for (HedgeClassifier.HedgeInfo s : hedgeFeatures.values())
			hedges.add(new Feature("hedgeFeatureSentHasHedgePhrase_", s.word.toLowerCase()));
			/*
			if (s.judgment.contains("M")) {
				//hedges.add(new Feature("hedgeFeatureSentHasHedgePhrase_" + s.word.toLowerCase(), s.confidence));
				hedges.add(new Feature("hedgeFeatureSentHasHedgePhrase_", s.word.toLowerCase()));
			}*/

		//Rupayan's edit - adding AnnaHedge feature
		for(String phrase: hedgePhrases){
			if(sentence.contains(phrase)){
				hedges.add(new Feature("dictHedgeFeatureSentHasHedgePhrase_", phrase));
				break;
			}
		}
		
		//end of Rupayan's edit



		return hedges;
	}

	public boolean isStartOfSentence() {
		return startOfSentence;
	}

	public void setStartOfSentence(boolean startOfSentence) {
		this.startOfSentence = startOfSentence;
	}

	public void setCurrentToken(Token currentToken) {
		this.currentToken = currentToken;
	}
	public void setSentenceNumber(int sentenceNumber) {
		this.sentenceNumber = sentenceNumber;
	}
	public void setHedgeFeatures(Map<Token, HedgeClassifier.HedgeInfo> hedgeFeatures) {
		this.hedgeFeatures = hedgeFeatures;
	}
		
}
