package org.ccls.nlp.cbt.FeatureExtractors;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.ccls.nlp.cbt.CommittedBeliefTrainAndTestAnnotator;
import org.ccls.nlp.cbt.HedgeClassifier;
import org.ccls.nlp.cbt.utils.LexUnitDecoder;
import org.ccls.nlp.cbt.utils.NounSenseMapper;
import org.ccls.nlp.cbt.utils.Syllables;
import org.ccls.nlp.cbt.utils.VerbnetMapper;
import org.ccls.nlp.cbt.utils.Word2VecClassMapper;
import org.ccls.nlp.cbt.utils.Word2VecSimilarity;
import org.ccls.nlp.cbt.utils.WordnetUtils;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.token.type.Token;

public class TokenFeatureExtractor implements SimpleFeatureExtractor{

	private VerbnetMapper verbnetMapper;
	private NounSenseMapper nounSenseMapper;
	private Word2VecClassMapper word2vecClassMapper;
	private Word2VecSimilarity word2vecSimilarity;
	private Set<String> dictionary;
	private Set<String> stopwords;
	private LexUnitDecoder framenet;
	//hedge word sets
	private Set<String> hedgeProp;
	private Set<String> hedgeRel;
	private int sentenceNumber;
	private Map<Token, String> sentenceHedges;

	private Boolean ngramMode;

	public TokenFeatureExtractor() {
		verbnetMapper = new VerbnetMapper();
		nounSenseMapper = new NounSenseMapper();
		word2vecClassMapper = new Word2VecClassMapper();
		//word2vecSimilarity = new Word2VecSimilarity();
		setupDictionary();
		setupStopwords();
		setupHedgeSets();
		//setupFramenet();
		//word2vecSimilarity.setupVectors();

		this.ngramMode = false;
	}

	public TokenFeatureExtractor(Boolean mode) {
		Calendar c1 = Calendar.getInstance();
		verbnetMapper = new VerbnetMapper();
		nounSenseMapper = new NounSenseMapper();
		word2vecClassMapper = new Word2VecClassMapper();
		//word2vecSimilarity = new Word2VecSimilarity();
		setupDictionary();
		setupStopwords();
		setupHedgeSets();
		//setupFramenet();
		//word2vecSimilarity.setupVectors();
		
		this.ngramMode = mode;
	}

	private void setupDictionary() {
		dictionary = new HashSet<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("TWL06.txt"));
			while (br.ready()) {
				dictionary.add(br.readLine());
			}
			br.close();
		} catch (IOException ioe) {
			System.err.println("WARN: Dictionary could not be populated");
		}
	}

	private void setupStopwords() {
		stopwords = new HashSet<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("resources/stopwords.txt"));
			while (br.ready()) {
				stopwords.add(br.readLine());
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

	private void setupFramenet() {
		framenet = new LexUnitDecoder();
		try {
			framenet.execute();
		} catch (IOException e) {
			System.err.println("WARN: Framenet could not be initialized ....");
		}
	}
	
	//Rupayan - add method to read hedge files
	private void setupHedgeSets() {
		hedgeProp = new HashSet<String>();
		hedgeRel = new HashSet<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("hedge/hProp.txt"));
			while (br.ready()) {
				hedgeProp.add(br.readLine());
			}
			br.close();
			br = new BufferedReader(new FileReader("hedge/hRel.txt"));
			while (br.ready()) {
				hedgeRel.add(br.readLine());
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

	/*
	private Feature getHedgeFeature(String featureName, Token token) {
		//HEDGE FEATURES WITH DICTIONARY LOOKUP (Rupayan)
		String featureNameUpper = Character.toUpperCase(featureName.charAt(0)) + featureName.substring(1); //capital first char for camelCase
		if (hedgeProp.contains(token.getCoveredText().toLowerCase()))
			return new Feature("dict" + featureNameUpper + "Prop_", token.getCoveredText().toLowerCase());

		if (hedgeRel.contains(token.getCoveredText().toLowerCase()))
			return new Feature("dict" + featureNameUpper + "Rel_", token.getCoveredText().toLowerCase());

		//HEDGE FEATURES WITH CLASSIFIER (Risa)
		HedgeClassifier.HedgeInfo description = CommittedBeliefTrainAndTestAnnotator.sentenceHedges.get(token);
		if (description != null) {// && description.judgment.contains("S") ) {
			//System.out.println("found single hedge");
			if (description.type.equals("hRel"))
				return new Feature(featureName + "Rel_", token.getCoveredText().toLowerCase());
				//return new Feature(featureName + "Rel_" + token.getCoveredText().toLowerCase(), description.confidence);
			else if (description.type.equals("hProp"))
				return new Feature(featureName + "Prop_", token.getCoveredText().toLowerCase());
				//return new Feature(featureName + "Prop_" + token.getCoveredText().toLowerCase(), description.confidence);
			else
				return new Feature(featureName + "_", token.getCoveredText().toLowerCase());
				//return new Feature(featureName + "_" + description.word.toLowerCase(), description.confidence);
		}
		return null;//FEATURE IS NOT A HEDGE
	}
	*/

	private List<Feature> getHedgeFeature(String featureName, Token token) {
		List<Feature> featureList = new ArrayList<Feature>();
		HedgeClassifier.HedgeInfo description = CommittedBeliefTrainAndTestAnnotator.sentenceHedges.get(token);

		//HEDGE FEATURES WITH DICTIONARY LOOKUP (Rupayan)
		String featureNameUpper = Character.toUpperCase(featureName.charAt(0)) + featureName.substring(1); //capital first char for camelCase
		if (hedgeProp.contains(token.getCoveredText().toLowerCase()))
			featureList.add(new Feature("dict" + featureNameUpper + "Prop_", token.getCoveredText().toLowerCase()));
		else if (hedgeRel.contains(token.getCoveredText().toLowerCase()))
			featureList.add(new Feature("dict" + featureNameUpper + "Rel_", token.getCoveredText().toLowerCase()));

		//HEDGE FEATURES WITH CLASSIFIER (Seth)
		if (description != null) {
			featureList.add(new Feature(featureName + "Token_", token.getCoveredText()));
			featureList.add(new Feature(featureName + "Phrase_", description.word.toLowerCase()));
			featureList.add(new Feature(featureName + "Type_", description.type));
			featureList.add(new Feature(featureName + "Confidence_", description.confidence));
		}
		return featureList;
	}

	@Override
	public List<Feature> extract(JCas jCas, Annotation annotation) {
		List<Feature> features = new ArrayList<Feature>();

		Token token = (Token) annotation;
		String tokenText = token.getCoveredText();
		String lemma = token.getLemma();
		String pos = token.getPos();
		String wordnetPos = WordnetUtils.getWordnetPos(pos);
		int senseCount = WordnetUtils.getSenseNumber(lemma, pos);
		int totalSenseCount = WordnetUtils.getSenseNumber(lemma);

		if (!ngramMode) {
			if (stopwords.contains(tokenText.toLowerCase())) {
				features.add(new Feature("Lemma", "STOPWORD"));
			}
			else {
				features.add(new Feature("Lemma", lemma));
			}
//			if (totalSenseCount == -1) {
//				features.add(new Feature("PosTag", "UNCERTAIN"));
//			} else {
				features.add(new Feature("PosTag", pos));
//			}
		} else {
			features.add(new Feature("Lemma", lemma));
			features.add(new Feature("PosTag", pos));
		}
		
		//hedge features
		/*Feature feature = getHedgeFeature("hedgeFeature", token);
		if (feature != null)
			features.add(feature);*/
		features.addAll(getHedgeFeature("hedgeFeature", token));



		if (!ngramMode && pos.charAt(0) == 'V') {
			List<String> verbnetMatches = verbnetMapper.getVerbNetClasses(lemma);
			if (verbnetMatches.size() == 1) {
				features.add(new Feature("VerbClass", verbnetMatches.get(0)));
			}
			else {
				if (verbnetMatches.size() > 1) {
					int matchIndex = VerbnetMapper.getLemmaMatch(verbnetMatches, lemma);
					// this turns off the feature for now
					if (matchIndex == -2) {
						features.add(new Feature("VerbClass", verbnetMatches.get(matchIndex)));
					} else {
						features.add(new Feature("VerbClass", "NONE-VERB"));						
					}
				} else {
					features.add(new Feature("VerbClass", "NONE-VERB"));
				}
			}
		} else if (ngramMode) {
		} else {
			features.add(new Feature("VerbClass", "NONE-NONVERB"));
		}

		/*if (true) {
			features.add(new Feature("Word2VecClass", word2vecClassMapper.getLemmaClass(lemma)));
		}*/
		
		/*if (lemma != null && word2vecSimilarity.getSimilarVectors(lemma.toLowerCase())) {
			features.add(new Feature("Word2VecSimilarity", word2vecSimilarity.getBest(1)));
		}*/
		
		if (!ngramMode) {
			if (pos.equals("MD")) {
				features.add(new Feature("WhichModalAmI", lemma));
			} else {
				features.add(new Feature("WhichModalAmI", "NOT-MODAL"));
			}
		} else {			
		}

		if (ExtractorCommons.MODAL_WORDS.contains(tokenText.toLowerCase())) {
			features.add(new Feature("2WhichModalAmI", lemma));
		} else {
			features.add(new Feature("2WhichModalAmI", "NOT-MODAL"));
		}

		if (ExtractorCommons.QUESTION_WORDS.contains(tokenText.toLowerCase())) {
			features.add(new Feature("QuestionWord", lemma));
		} else {
			features.add(new Feature("QuestionWord", "NOT-QUESTION-WORD"));
		}

		if (pos.charAt(0) == 'N') {
			if (nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).size() == 1) {
				features.add(new Feature("OntoSenseNoun", nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).get(0)));
			} else {
				features.add(new Feature("OntoSenseNoun", "NONE"));
			}
		} else {
			features.add(new Feature("OntoSenseNoun", "NONE"));
		}	

		if (pos.charAt(0) == 'V') {
			if (nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).size() == 1) {
				features.add(new Feature("OntoSenseVerb", nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).get(0)));
			} else {
				features.add(new Feature("OntoSenseVerb", "NONE"));
			}
		} else {
			features.add(new Feature("OntoSenseVerb", "NONE"));
		}

		if (pos.startsWith("NN") || pos.startsWith("VB") || pos.startsWith("RB") || pos.startsWith("JJ"))
			features.add(new Feature("LemmaOrPOS", pos));
		else
			features.add(new Feature("LemmaOrPOS", lemma));

		if (senseCount > 0) {
			features.add(new Feature("SenseCount", senseCount));
		}
		if (senseCount > 1) {
			features.add(new Feature("Polysemous", true));
		}

		/*if (!wordnetPos.equals("uk")) {
			String framenetLookup = lemma.toLowerCase() + "." + wordnetPos;
			Set<String> frames = framenet.findExactFramesForLexUnit(framenetLookup);
			if (frames != null) {
				for (String frame : frames) {
					features.add(new Feature("Frame", frame));
				}
			}
		}*/		

		// #2 in ideas list
		if (!tokenText.equals(lemma)) {
			features.add(new Feature("LemmaInequality", true));
		}

		// #3 in ideas list
		features.add(new Feature("Syllables", Syllables.countSyllables(tokenText)));

		// #4 in ideas list
		if (dictionary.contains(tokenText.toUpperCase())) {
			features.add(new Feature("OutOfDictionary", true));
		}

		// #5 in ideas list
		if (tokenText.endsWith("ing")) {
			features.add(new Feature("EndsWithIng", true));
		}

		// #6 in ideas list
		if (tokenText.endsWith("ed")) {
			features.add(new Feature("EndsWithEd", true));
		}

		// #7 in ideas list
		Set<Character> charactersSeen = new HashSet<Character>();
		for (int i = 0; i < tokenText.length(); i++) {
			char c = tokenText.charAt(i);
			if (charactersSeen.contains(c)) {
				if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c =='u' ||
						c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U') {
				} else {
					features.add(new Feature("RepeatedConsonant", true));
					break;
				}
			} else {
				charactersSeen.add(c);
			}
		}

		// #8 in ideas list
		if (tokenText.toUpperCase().equals(tokenText)) {
			features.add(new Feature("AllCaps", true));
		}

		// #9 in ideas list
		if (tokenText.charAt(0) != lemma.charAt(0)) {
			features.add(new Feature("LemmaDifferentCase", true));
		}

		// #10 in ideas list
		if (tokenText.contains("-")) {
			features.add(new Feature("HyphenatedWord", true));
		}

		// #11 in ideas list
		if (pos.startsWith("NN") || pos.startsWith("VB") || pos.startsWith("RB") || pos.startsWith("JJ")) {
			features.add(new Feature("WordnetPos", true));
		}

		return features;
	}

	public NounSenseMapper getNounSenseMapper() {
		return nounSenseMapper;
	}		

}
