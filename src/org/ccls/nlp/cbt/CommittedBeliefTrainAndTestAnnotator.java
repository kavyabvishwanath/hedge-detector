package org.ccls.nlp.cbt;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.ccls.nlp.cbt.FeatureExtractors.*;
import org.ccls.nlp.cbt.ts.BeliefAnn;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkAnnotatorDescriptionFactory;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.Counts;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Focus;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Ngram;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.annotationpair.AnnotationPairFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.util.JCasUtil;

public class CommittedBeliefTrainAndTestAnnotator extends CleartkAnnotator<String> {

	private List<SimpleFeatureExtractor> tokenFeatureExtractors;
	private List<SimpleFeatureExtractor> sentenceFeatureExtractors;
	private List<SimpleFeatureExtractor> bagCountsExtractors;
	private List<AnnotationPairFeatureExtractor> pairFeatureExtractors;
	private List<CleartkExtractor> contextFeatureExtractors;

	private HedgeClassifier hedgeClassifier;

	public static int tokenCount;
	public static int instanceCount;

	public static List<Map<Token, String>> weiweiDocumentFeatures;

	//for hedge classifier
	public static List<Map<Token, HedgeClassifier.HedgeInfo>> hedgeFeatures;
	public static Map<Token, HedgeClassifier.HedgeInfo> sentenceHedges;
	public static int sentenceNumber;

	private static Set<String> failedFeatures = new HashSet<String>();

	public static PrintWriter senseDatabase;
	public static int documentNumber = 1;

	public static String featureSet = "SINGLE";
	public static int currentFold = 0;
	public static Hashtable<Outcome, Hashtable<String,String>> outcomeTable = new Hashtable<Outcome, Hashtable<String,String>>();
	public static int outcomeIndex = 1;

	public static FileWriter testpredsfile, annotatedresultsfile;

	// this is the predetermined order of rarity.  NCB is most rare, O is most common
	public static final List<String> minorities = Arrays.asList("NCB", "NA", "RB", "CB", "O");
	
	public static boolean tagRawText = false;

	public CommittedBeliefTrainAndTestAnnotator()// if I wanna add any feature here after making the class
	{


		hedgeClassifier = new HedgeClassifier();

		tokenFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
		tokenFeatureExtractors.add(new TokenFeatureExtractor());
		tokenFeatureExtractors.add(new DepFeatureExtractor());
		sentenceFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();		
		sentenceFeatureExtractors.add(new SentenceFeatureExtractor());
		bagCountsExtractors = new ArrayList<SimpleFeatureExtractor>();
		bagCountsExtractors.add(new BagCountsExtractor(Sentence.class, new TokenFeatureExtractor(true)));
		pairFeatureExtractors = new ArrayList<AnnotationPairFeatureExtractor>();
		pairFeatureExtractors.add(new PairFeatureExtractor());
		contextFeatureExtractors = new ArrayList<CleartkExtractor>();
		contextFeatureExtractors.add(new CleartkExtractor(Token.class, new TokenFeatureExtractor(true), new Preceding(2),new Following(2)));
		contextFeatureExtractors.add(new CleartkExtractor(Token.class, new TypePathExtractor(Token.class, "lemma"), new Ngram(new Preceding(1), new Focus())));

		tokenCount = 0;
		instanceCount = 0;

	}

	public void initialize(UimaContext context) throws ResourceInitializationException {
		outcomeTable.clear();
		try {
			testpredsfile = new FileWriter(new File(AnnotationPipeline.DIR_NAME  + "/TestResults.txt" ));
			annotatedresultsfile = new FileWriter(new File(AnnotationPipeline.DIR_NAME + "/AnnotatedResults.txt"));
			senseDatabase = new PrintWriter("Sense_Database.csv");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}		
		super.initialize(context);
	}

	private void tokenAnnotation(JCas jCas, Sentence sentence, int s) throws CleartkProcessingException {
		List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
//		List<BeliefAnn> beliefs = JCasUtil.selectCovered(jCas, BeliefAnn.class, sentence);
//		for (BeliefAnn belief : beliefs) { if (JCasUtil.selectCovered(jCas, Token.class, belief).size() > 1 { } else {} }
//		sentenceNumber = s;
		List<Instance<String>> instances = new ArrayList<Instance<String>>();
		sentenceHedges = hedgeFeatures.remove(0);
		Token lastToken = null;
		// for each token, extract all feature values and the label
		for (Token token : tokens) {
			Instance<String> instance = new Instance<String>();

			// extract all features that require only the token annotation
			for (SimpleFeatureExtractor extractor : sentenceFeatureExtractors) {
				((SentenceFeatureExtractor) extractor).setCurrentToken(token);
				((SentenceFeatureExtractor) extractor).setStartOfSentence(lastToken == null);
				((SentenceFeatureExtractor) extractor).setHedgeFeatures(sentenceHedges);
				for (Feature feature : extractor.extract(jCas, sentence)) {
					if (wantedFeature(feature)) {
						instance.add(feature);
					}
				}
			}

			// all pair extractors
			for (AnnotationPairFeatureExtractor extractor : pairFeatureExtractors) {
				for (Feature feature : extractor.extract(jCas, lastToken, token)) {
					if (wantedFeature(feature)) {
						instance.add(feature);
					}
				}					
			}

			// extract all features that require only the token annotation
			for (SimpleFeatureExtractor extractor : tokenFeatureExtractors) {
				for (Feature feature : extractor.extract(jCas, token)) {
					if (wantedFeature(feature)) {
						instance.add(feature);
					}
				}
			}

			// extract all features that require the token and sentence annotations
			for (CleartkExtractor extractor : this.contextFeatureExtractors) {
				for (Feature feature : extractor.extractWithin(jCas, token,sentence)) {
					if (wantedFeature(feature)) {
						instance.add(feature);
					}
				}
			}

			// set the instance label from annotation
			if (this.isTraining()) {
				StackTraceElement[] stes = Thread.currentThread().getStackTrace();
				if (true) {
					if (JCasUtil.selectCovered(jCas, BeliefAnn.class,token).size() > 0) {
						instance.setOutcome(JCasUtil.selectCovered(jCas, BeliefAnn.class,token).get(0).getTag());
					}
					else{
						instance.setOutcome("O");
					}
				}
			}
			
			// add the instance to the list
			instances.add(instance);

			lastToken = token;
		}

		// for training, write instances to the data write
		if (this.isTraining()) {
			if (true) {		  						
				for (Instance<String> instance : instances) {
					dataWriter.write(instance);
				}
			}
		}

		// for classification/testing
		else {
			int i = 0;
			Iterator<Token> tokensIter = tokens.iterator();
			HashMap<Token,String> tokenTag = new HashMap<Token, String>();

			for (Instance<String> instance : instances) {
				List<Feature> features = instance.getFeatures();
				String label = this.classifier.classify(features);

				Token token = tokensIter.next();

				String actual, prediction;

				Outcome outcome = new Outcome(instances.get(i), outcomeIndex);
				outcomeTable.put(outcome, new Hashtable<String,String>());

				if (JCasUtil.selectCovered(jCas, BeliefAnn.class,token).size() > 0) {
					actual = JCasUtil.selectCovered(jCas, BeliefAnn.class,token).get(0).getTag();
				}
				else {
					actual = "O";
				}
				prediction = label;	
				tokenTag.put(token, prediction);
				
				//add belief predictions to the jCas only we're about to tag plain text files
				if(tagRawText && !prediction.equals("O")){
					BeliefAnn beliefAnn = new BeliefAnn(jCas);					
					beliefAnn.setBegin(token.getBegin());
					beliefAnn.setEnd(token.getEnd());
					String beliefTag = prediction;
					beliefAnn.setTag(beliefTag);					
					beliefAnn.addToIndexes();
				}

				writeEvalInstance(testpredsfile, jCas, instances.get(i), actual, prediction, sentence);		  		        		
				outcomeTable.get(outcome).put("pred", prediction);
				outcomeTable.get(outcome).put("actual", actual);

				i++;

				outcomeIndex++;
			}
			
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {// reading sequence classifier tutorial first
		List<Sentence> sentences = new ArrayList<Sentence>(JCasUtil.select(jCas, Sentence.class));
		hedgeFeatures = hedgeClassifier.runClassifier(jCas);
		System.out.println("CommitedBeliefTrainAndTestAnnotator.process: document " + documentNumber);
		for (int s = 0; s < sentences.size(); s++) {
			Sentence sentence = sentences.get(s);
			tokenAnnotation(jCas, sentence, s);
		}
		documentNumber++;
	}

	public boolean wantedFeature(Feature thisFeature) {
		if (featureSet.equals("ALL")) {
			return true;
		} else {
			for (String wantedFeature : featureSet.split(",")) {
				if (thisFeature.getName().equals(wantedFeature)) {
					return true;
				}
			}
			if (!failedFeatures.contains(thisFeature.getName())) {
				failedFeatures.add(thisFeature.getName());
				//System.out.println("FAILED: " + thisFeature.getName());
			}
		}
		return false;
	}

	public boolean wantedFeatureOriginal(Feature thisFeature) {
		boolean featurepass = false;

		if (featureSet.equals("ALL"))
			return true;

		if (thisFeature.getName().startsWith("Bag")) {
			if (featureSet.contains(","+"Bag" + ",") || featureSet.startsWith("Bag") || featureSet.endsWith("Bag")) {
				featurepass = true;
			}
		}

		if (thisFeature.getValue() instanceof Counts) {
			for (String wantedFeature : featureSet.split(",")) {
				if (((Counts)thisFeature.getValue()).getIdentifier().equals(wantedFeature)) {
					featurepass = true;
					break;
				}

			}
		} else {
			for (String wantedFeature : featureSet.split(",")) {
				if (thisFeature.getName().startsWith("Following") || thisFeature.getName().startsWith("Preceding")) {
					int prefixlength = thisFeature.getName().lastIndexOf("_"); 

					if (((String)thisFeature.getValue()).startsWith("OOB") || 
							thisFeature.getName().substring(prefixlength+1).startsWith(wantedFeature)) {
						featurepass = true;
						break;
					}
				}
				if (thisFeature.getName().startsWith(wantedFeature)) {
					featurepass = true;
					break;
				}
			}
		}

		return featurepass;
	}

	public static AnalysisEngineDescription getClassifierDescription(String modelFileName)
			throws ResourceInitializationException {
		System.out.println("Inside CommitedBeliefTrainAndTestAnnotator.getClassifierDescription()");
		return AnalysisEngineFactory.createPrimitiveDescription(
				CommittedBeliefTrainAndTestAnnotator.class, 
				TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				modelFileName);
	}	

	public void writeEvalInstance(FileWriter writer, JCas jcas, Instance<String> instance, String gold, String pred, Sentence sentence) throws CleartkProcessingException {

		String out = gold + "\t" + pred + "\t";

		for (Token token : JCasUtil.selectCovered(jcas, Token.class, sentence)) {
			out += token.getCoveredText() + "(" + token.getPos() + ") ";
		}

		out += "\t";

		for (Feature feat : instance.getFeatures()) {
			out += " " + feat.getName() + ":" + feat.getValue();
		}
		try {
			writer.write(out + "\n");
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}	

}