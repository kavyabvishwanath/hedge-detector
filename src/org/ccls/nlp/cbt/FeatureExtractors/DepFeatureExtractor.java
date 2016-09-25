package org.ccls.nlp.cbt.FeatureExtractors;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.ccls.nlp.cbt.CommittedBeliefTrainAndTestAnnotator;
import org.ccls.nlp.cbt.HedgeClassifier;
import org.ccls.nlp.cbt.utils.FileUtils;
import org.ccls.nlp.cbt.utils.NounSenseMapper;
import org.ccls.nlp.cbt.utils.Tree;
import org.ccls.nlp.cbt.utils.VerbnetMapper;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.TreeFeature;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.token.type.Token;
import org.uimafit.util.JCasUtil;

public class DepFeatureExtractor implements SimpleFeatureExtractor{
	VerbnetMapper verbnetMapper;
	NounSenseMapper nounSenseMapper;
	//hedge word sets
	private Set<String> hedgeProp;
	private Set<String> hedgeRel;

	// important for preprocessed features like Weiwei
	//and hedge
	//private int sentenceNumber;
	private List<Map<Token, String>> weiweiDocumentFeatures;
	public DepFeatureExtractor() {
		verbnetMapper = new VerbnetMapper();
		nounSenseMapper = new NounSenseMapper();
		
		//sentenceNumber = -1;
		setupHedgeSets();
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

	/**
	 * To avoid having to change the code in 4 different places when modifying the hedge features used:
	 * Given the name of the feature (eg. hedgeFeature_parentIsHedge) and the token, returns a feature
	 * if the token is a hedge, null otherwise
	 * @param featureName The name of the feature (eg. hedgeFeature_parentIsHedge)
	 * @param token
     * @return A Feature if the token is a hedge, null otherwise
     */
	/*
	private Feature getHedgeFeature(JCas jCas, DependencyNode node, String featureName, Token token) {
		//HEDGE FEATURES WITH DICTIONARY LOOKUP (Rupayan)
		String featureNameUpper = Character.toUpperCase(featureName.charAt(0)) + featureName.substring(1); //capital first char for camelCase
		if(hedgeProp.contains(token.getCoveredText().toLowerCase())) {
			return new Feature("dict" + featureNameUpper + "Prop_", token.getCoveredText().toLowerCase());
			//return new Feature(featureName + "Prop_" + token.getLemma().toLowerCase(), true);
		}
		if(hedgeRel.contains(token.getCoveredText().toLowerCase())) {
			return new Feature("dict" + featureNameUpper + "Rel_", token.getCoveredText().toLowerCase());
			//return new Feature(featureName + "Rel_" + token.getLemma().toLowerCase(), true);
		}

		//HEDGE FEATURES WITH CLASSIFIER (Risa)
		Map<Token, HedgeClassifier.HedgeInfo> sentenceHedges = CommittedBeliefTrainAndTestAnnotator.sentenceHedges;
		if (sentenceHedges.containsKey(token)) {
			HedgeClassifier.HedgeInfo description = sentenceHedges.get(token);
			if (description != null) { //description.judgment.contains("S")) {
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
		}
		return null;//FEATURE IS NOT A HEDGE
	}
	*/

	private List<Feature> getHedgeFeature(JCas jCas, DependencyNode node, String featureName, Token token) {
		List<Feature> featureList = new ArrayList<Feature>();

		//HEDGE FEATURES WITH DICTIONARY LOOKUP (Rupayan)
		String featureNameUpper = Character.toUpperCase(featureName.charAt(0)) + featureName.substring(1); //capital first char for camelCase
		if (hedgeProp.contains(token.getCoveredText().toLowerCase()))
			featureList.add(new Feature("dict" + featureNameUpper + "Prop_", token.getCoveredText().toLowerCase()));
		else if (hedgeRel.contains(token.getCoveredText().toLowerCase()))
			featureList.add(new Feature("dict" + featureNameUpper + "Rel_", token.getCoveredText().toLowerCase()));

		//HEDGE FEATURES WITH CLASSIFIER (Seth)
		Map<Token, HedgeClassifier.HedgeInfo> sentenceHedges = CommittedBeliefTrainAndTestAnnotator.sentenceHedges;
		if (sentenceHedges.containsKey(token)) {
			HedgeClassifier.HedgeInfo description = sentenceHedges.get(token);
			if (description != null) {// && description.judgment.contains("S") ) {
				featureList.add(new Feature(featureName + "Token_", token.getCoveredText()));
				featureList.add(new Feature(featureName + "Phrase_", description.word.toLowerCase()));
				featureList.add(new Feature(featureName + "Type_", description.type));
				featureList.add(new Feature(featureName + "Confidence_", description.confidence));
			}
		}
		return featureList;
	}

	@Override
	public List<Feature> extract(JCas jCas, Annotation annotation) {

		List<Feature> features = new ArrayList<Feature>();

		if (JCasUtil.selectCovered(jCas, DependencyNode.class, ((Token) annotation)).size() > 0) {
			DependencyNode node = JCasUtil.selectCovered(jCas, DependencyNode.class, ((Token) annotation)).get(0);
			features.addAll(getDepTree(jCas, node, null, false, false));
			// remember, this is POS which has a prefix of VB, not just VB
			features.addAll(getChildren(jCas, node));
			features.addAll(getParent(jCas, node));
			features.addAll(getSiblings(jCas, node));
			features.addAll(getDepAncestors(jCas, node));
			features.addAll(getReportingAncestor(jCas, node));
			features.addAll(getFirstDepAncestorOfPos(jCas, node, "VB"));
			features.addAll(getFirstDepAncestorOfPos(jCas, node, "NN"));
			features.addAll(getAmUnderConditionalScope(jCas, node));
			features.addAll(getDepRel(jCas, node));
		}

		return features;

	}

	private List<Feature> getDepTree(JCas jCas, DependencyNode node, Boolean isTraining, boolean fromDisc, boolean toDisc) {
		String filename = FileUtils.getFilename(jCas);
		String prefix = FileUtils.getDocumentPrefixFilename(filename);		
		//prefix = (isTraining ? "train_" : "test_") + prefix;
		
		List<Feature> features = new ArrayList<Feature>();

		Tree t = new Tree(node);
		if (fromDisc) {
			//t.readFullTreeFromDisc();
		}
		else {
			t.formFullTree();
		}

		Map<String, String> featureRepresentations = new HashMap<String, String>();
		Tree siblingsTree = t.getSiblingTree();
		featureRepresentations.putAll(siblingsTree.getFeatureRepresentations(jCas, "SiblingsTree"));
		Tree childrenTree = t.getChildrenTree();
	    featureRepresentations.putAll(childrenTree.getFeatureRepresentations(jCas, "ChildrenTree"));
		Tree parentTree = t.getParentTree();
		featureRepresentations.putAll(parentTree.getFeatureRepresentations(jCas, "ParentTree"));
		Tree depAncestorTree = t.getDepAncestorTree();
		featureRepresentations.putAll(depAncestorTree.getFeatureRepresentations(jCas, "DepAncestorTree"));

		if (toDisc) {
			//t.printTree(jCas, ps);
		}

		for (String featureName : featureRepresentations.keySet()) {
			features.add(new TreeFeature(featureName, featureRepresentations.get(featureName)));
		}

		return features;
	}
	
	private List<Feature> getChildren(JCas jCas, DependencyNode node) {

		List<Feature> features = new ArrayList<Feature>();
		Token token;
		String lemma;

		FSArray fsarray = node.getChildRelations();
		for (int i=0; i<fsarray.size(); i++) {
			DependencyNode child = ((DependencyRelation) fsarray.get(i)).getChild();
			token = JCasUtil.selectCovered(jCas, Token.class, child).get(0);
			lemma = token.getLemma();

			features.add(new Feature("Child-lemma", lemma));
			features.add(new Feature("Child-pos", token.getPos()));
			//features.add(new Feature("Child-weiwei", weiweiDocumentFeatures.get(sentenceNumber).get(token)));

			if (token.getPos().charAt(0) == 'V') {
				if (verbnetMapper.getVerbNetClasses(lemma).size() == 1) {
					features.add(new Feature("Child-VerbClass", verbnetMapper.getVerbNetClasses(lemma).get(0)));
				}
			}

			if (token.getPos().equals("MD")) {
				features.add(new Feature("Child-WhichModalAmI", lemma));
			}

			if (ExtractorCommons.MODAL_WORDS.contains(token.getCoveredText().toLowerCase())) {
				features.add(new Feature("Child-2WhichModalAmI", lemma));
			}

			if (ExtractorCommons.QUESTION_WORDS.contains(token.getCoveredText().toLowerCase())) {
				features.add(new Feature("Child-QuestionWord", lemma));
			}

			if (token.getPos().charAt(0) == 'N') {
				if (nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).size() == 1) {
					features.add(new Feature("Child-OntoSenseNoun", nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).get(0)));
				} else {
					features.add(new Feature("Child-OntoSenseNoun", "NONE-NOUN"));
				}
			} else {
				features.add(new Feature("Child-OntoSenseNoun", "NONE-NONNOUN"));
			}

			if (token.getPos().charAt(0) == 'V') {
				if (nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).size() == 1) {
					features.add(new Feature("Child-OntoSenseVerb", nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).get(0)));
				} else {
					features.add(new Feature("Child-OntoSenseVerb", "NONE-VERB"));
				}
			} else {
				features.add(new Feature("Child-OntoSenseVerb", "NONE-NONVERB"));
			}

			//hedge feature

			/*Feature feature = getHedgeFeature(jCas, node, "hedgeFeatureChildIsHedge", token);
			if (feature != null)
				features.add(feature);*/
			features.addAll(getHedgeFeature(jCas, node, "hedgeFeatureChildIsHedge", token));
		}
		return features;
	}

	private List<Feature> getAmUnderConditionalScope(JCas jCas, DependencyNode node) {

		Token token;
		List<Feature> features = new ArrayList<Feature>();

		FSArray fsarray = node.getChildRelations();
		for (int i=0; i<fsarray.size(); i++) {
			DependencyNode child = ((DependencyRelation) fsarray.get(i)).getChild();
			Token childToken = JCasUtil.selectCovered(jCas, Token.class, child).get(0);

			if (childToken.getLemma().equals("if") || childToken.getLemma().equals("when")) {
				features.add(new Feature("UnderConditional", childToken.getLemma()));
			}
		}
		if (node.getHeadRelations().size() > 0) {
			node = node.getHeadRelations(0).getHead();
			if (node != null) {
				while(true) {
					fsarray = node.getChildRelations();
					token = JCasUtil.selectCovered(jCas, Token.class, node).get(0);
					for (int i=0; i<fsarray.size(); i++) {
						DependencyNode child = ((DependencyRelation) fsarray.get(i)).getChild();
						Token childToken = JCasUtil.selectCovered(jCas, Token.class, child).get(0);

						if (childToken.getLemma().equals("if") || childToken.getLemma().equals("when")) {
							features.add(new Feature("AncestorUnderConditional",childToken.getLemma()));
							features.add(new Feature("ConditionalAncestorPos", token.getPos()));
						}
					}
					if (node.getHeadRelations().size() == 0) {
						break;
					}
					String lastNode = node.getCoveredText();
					node = node.getHeadRelations(0).getHead();
					if (lastNode.equals(node.getCoveredText())) {
						break;
					}

				}
			}
		}
		return features;
	}

	private List<Feature> getReportingAncestor(JCas jCas, DependencyNode node) {

		Token token;
		String lemma;
		List<Feature> features = new ArrayList<Feature>();
		ArrayList<String> lemmaList = new ArrayList<String>();

		lemmaList.addAll(ExtractorCommons.EXTENDED_REPORTING_VERBS);
		lemmaList.addAll(ExtractorCommons.REPORTING_VERBS);

		if (node != null) {
			while(true) {
				token = JCasUtil.selectCovered(jCas, Token.class, node).get(0);
				lemma = token.getLemma();
				if (lemmaList.contains(lemma)) {
					features.add(new Feature("ReportingAncestor-pos", token.getPos()));
					features.add(new Feature("ReportingAncestor-lemma", lemma));
					//features.add(new Feature("ReportingAncestor-weiwei", weiweiDocumentFeatures.get(sentenceNumber).get(token)));
					features.add(new Feature("ReportingAncestor", "TRUE"));
				}
				if (node.getHeadRelations().size() == 0) {
					break;
				}
				String lastNode = node.getCoveredText();
				node = node.getHeadRelations(0).getHead();
				if (lastNode.equals(node.getCoveredText())) {
					break;
				}

			}
		}
		else {
			return Collections.EMPTY_LIST;
		}
		return features;
	}


	private List<Feature> getParent(JCas jCas, DependencyNode node) {
		Token token;
		String lemma;
		DependencyNode headNode;
		List<Feature> features = new ArrayList<Feature>();

		if (node.getHeadRelations().size() == 0) {
			return Collections.EMPTY_LIST;
		}
		headNode = node.getHeadRelations(0).getHead();

		if (headNode != null) {
			token = JCasUtil.selectCovered(jCas, Token.class, headNode).get(0);
			lemma = token.getLemma();

			features.add(new Feature("Parent-pos", token.getPos()));
			features.add(new Feature("Parent-lemma", lemma));
			//features.add(new Feature("Parent-weiwei", weiweiDocumentFeatures.get(sentenceNumber).get(token)));

			//hedge feature
			/*Feature feature = getHedgeFeature(jCas, node, "hedgeFeatureParentIsHedge", token);
			if (feature != null)
				features.add(feature);*/
			features.addAll(getHedgeFeature(jCas, node, "hedgeFeatureParentIsHedge", token));

			if (token.getPos().charAt(0) == 'V') {
				if (verbnetMapper.getVerbNetClasses(lemma).size() == 1) {
					features.add(new Feature("Parent-VerbClass", verbnetMapper.getVerbNetClasses(lemma).get(0)));
				}
			}

			if (token.getPos().equals("MD")) {
				features.add(new Feature("Parent-WhichModalAmI", lemma));
			}

			if (ExtractorCommons.MODAL_WORDS.contains(token.getCoveredText().toLowerCase())) {
				features.add(new Feature("Parent-2WhichModalAmI", lemma));
			}

			if (ExtractorCommons.QUESTION_WORDS.contains(token.getCoveredText().toLowerCase())) {
				features.add(new Feature("Parent-QuestionWord", lemma));
			}
			if (token.getPos().charAt(0) == 'N') {
				if (nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).size() == 1) {
					features.add(new Feature("Parent-OntoSenseNoun", nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).get(0)));
				} else {
					features.add(new Feature("Parent-OntoSenseNoun", "NONE-NOUN"));
				}
			} else {
				features.add(new Feature("Parent-OntoSenseNoun", "NONE-NONNOUN"));
			}
			if (token.getPos().charAt(0) == 'V') {
				if (nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).size() == 1) {
					features.add(new Feature("Parent-OntoSenseVerb", nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).get(0)));
				} else {
					features.add(new Feature("Parent-OntoSenseVerb", "NONE-VERB"));
				}
			} else {
				features.add(new Feature("Parent-OntoSenseVerb", "NONE-NONVERB"));
			}
			return features;
		}
		else {
			features.add(new Feature("Parent-pos", "ROOT"));
			features.add(new Feature("Parent-lemma", "ROOT"));
			//features.add(new Feature("Parent-weiwei", "ROOT"));
			return features;
		}
	}

	private List<Feature> getDepRel(JCas jCas, DependencyNode node) {

		List<Feature> features = new ArrayList<Feature>();

		if (node != null && node.getHeadRelations().size() > 0) {
			features.add(new Feature("DepRel", node.getHeadRelations(0).getRelation()));
		}
		else {
			features.add(new Feature("DepRel", "NIL"));
		}
		
		return features;

	}

	// POS should be the prefix of a PENN POS tag.  Don't expect whole word matching only ...
	private List<Feature> getFirstDepAncestorOfPos(JCas jCas, DependencyNode node, String POS) {
		Token token;
		List<Feature> features = new ArrayList<Feature>();
		String lastDepRel = "NIL";

		if (node != null) { 
			while(true) {
				token = JCasUtil.selectCovered(jCas, Token.class, node).get(0);
				if (token.getPos().startsWith(POS)) {
					features.add(new Feature("FirstDepAncestorOfPos"+POS+"-lemma", token.getLemma()));
					//features.add(new Feature("FirstDepAncestorOfPos"+POS+"-weiwei", weiweiDocumentFeatures.get(sentenceNumber).get(token)));
					features.add(new Feature("FirstDepAncestorOfPos"+POS+"-lastDeprel", lastDepRel));
					if (verbnetMapper.getVerbNetClasses(token.getLemma()).size() == 1)
						features.add(new Feature("FirstDepAncestorOfPos"+POS+"-VerbClass",verbnetMapper.getVerbNetClasses(token.getLemma()).get(0)));

				}
				if (node.getHeadRelations().size() == 0) {
					break;
				}
				lastDepRel = node.getHeadRelations(0).getRelation();
				String lastNode = node.getCoveredText();
				node = node.getHeadRelations(0).getHead();
				if (node.getCoveredText().equals(lastNode)) {
					break;
				}
			}
		}
		else {
			features.add(new Feature("FirstDepAncestorOfPos"+POS+"-lemma", "ROOT"));
			//features.add(new Feature("FirstDepAncestorOfPos"+POS+"-weiwei", "ROOT"));
			features.add(new Feature("FirstDepAncestorOfPos"+POS+"-lastDeprel", "ROOT"));	
			return features;
		}
		return features;
	}

	private List<Feature> getDepAncestors(JCas jCas, DependencyNode node) {
		Token token;
		List<Feature> features = new ArrayList<Feature>();

		if (node != null) { 
			while(true) {
				if (node.getHeadRelations().size() == 0) {
					break;
				}
				String lastNode = node.getCoveredText();
				node = node.getHeadRelations(0).getHead();
				if (lastNode.equals(node.getCoveredText())) {
					break;
				}
				token = JCasUtil.selectCovered(jCas, Token.class, node).get(0);
				features.add(new Feature("DepAncestors-lemma", token.getLemma()));
				features.add(new Feature("DepAncestors-pos", token.getPos()));
				//features.add(new Feature("DepAncestors-weiwei", weiweiDocumentFeatures.get(sentenceNumber).get(token)));
				
				//hedge feature
				/*Feature feature = getHedgeFeature(jCas, node, "hedgeFeatureDepAncestorIsHedge", token);
				if (feature != null)
					features.add(feature);*/
				features.addAll(getHedgeFeature(jCas, node, "hedgeFeatureDepAncestorIsHedge", token));
				
				if (verbnetMapper.getVerbNetClasses(token.getLemma()).size() == 1)
					features.add(new Feature("DepAncestors-VerbClass",verbnetMapper.getVerbNetClasses(token.getLemma()).get(0)));
				if (ExtractorCommons.QUESTION_WORDS.contains(token.getCoveredText().toLowerCase())) {
					features.add(new Feature("DepAncestors-QuestionWord", token.getLemma()));
				}
				if (token.getPos().equals("MD")) {
					features.add(new Feature("DepAncestors-WhichModalAmI",token.getLemma()));
				}
				if (ExtractorCommons.MODAL_WORDS.contains(token.getCoveredText().toLowerCase())) {
					features.add(new Feature("DepAncestors-2WhichModalAmI", token.getLemma()));
				}
				if (ExtractorCommons.AUXILIARY_VERBS.contains(token.getCoveredText().toLowerCase())) {
					features.add(new Feature("DepAncestors-Aux", token.getLemma()));
				}
			}
		}
		else {
			features.add(new Feature("DepAncestors-lemma", "ROOT"));
			features.add(new Feature("DepAncestors-pos", "ROOT"));
			//features.add(new Feature("DepAncestors-weiwei", "ROOT"));
		}
		return features;
	}

	private List<Feature> getSiblings(JCas jCas, DependencyNode node) {
		Token token;
		String lemma;
		List<Feature> features = new ArrayList<Feature>();

		if (node != null && node.getHeadRelations().size() != 0) { 
			FSArray fsarray = node.getHeadRelations(0).getHead().getChildRelations();
			for (int i=0; i<fsarray.size(); i++) {
				DependencyNode sibling = ((DependencyRelation) fsarray.get(i)).getChild();
				if (!sibling.equals(node)) {
					token = JCasUtil.selectCovered(jCas, Token.class, sibling).get(0);
					lemma = token.getLemma();
					
					features.add(new Feature("Siblings-lemma", lemma));
					features.add(new Feature("Siblings-pos", token.getPos()));
					//features.add(new Feature("Siblings-weiwei", weiweiDocumentFeatures.get(sentenceNumber).get(token)));
					
					if (verbnetMapper.getVerbNetClasses(lemma).size() == 1)
						features.add(new Feature("Siblings-VerbClass",verbnetMapper.getVerbNetClasses(lemma).get(0)));
					if (token.getPos().equals("MD")) {
						features.add(new Feature("Siblings-WhichModalAmI",lemma));
					}
					if (ExtractorCommons.MODAL_WORDS.contains(token.getCoveredText().toLowerCase())) {
						features.add(new Feature("Siblings-2WhichModalAmI", lemma));
					}
					if (ExtractorCommons.QUESTION_WORDS.contains(token.getCoveredText().toLowerCase())) {
						features.add(new Feature("Siblings-QuestionWord", lemma));
					}
					if (token.getPos().charAt(0) == 'N') {
						if (nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).size() == 1) {
							features.add(new Feature("Siblings-OntoSenseNoun", nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).get(0)));
						} else {
							features.add(new Feature("Siblings-OntoSenseNoun", "NONE-NOUN"));
						}
					} else {
						features.add(new Feature("Siblings-OntoSenseNoun", "NONE-NONNOUN"));
					}
					if (token.getPos().charAt(0) == 'V') {
						if (nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).size() == 1) {
							features.add(new Feature("Siblings-OntoSenseVerb", nounSenseMapper.getNounSenseClasses(lemma.toLowerCase()).get(0)));
						} else {
							features.add(new Feature("Siblings-OntoSenseVerb", "NONE-VERB"));
						}
					} else {
						features.add(new Feature("Siblings-OntoSenseVerb", "NONE-NONVERB"));
					}	
					
					//hedge feature
					/*Feature feature = getHedgeFeature(jCas, node, "hedgeFeatureSiblingIsHedge", token);
					if (feature != null)
						features.add(feature);*/
					features.addAll(getHedgeFeature(jCas, node, "hedgeFeatureSiblingIsHedge", token));
					
					return features;
				}
			}
		}
		return features;
	}

	//public void setSentenceNumber(int sentenceNumber) {
		//this.sentenceNumber = sentenceNumber;
	//}

	public void setWeiweiDocumentFeatures(List<Map<Token, String>> weiweiDocumentFeatures) {
		this.weiweiDocumentFeatures = weiweiDocumentFeatures;
	}
}