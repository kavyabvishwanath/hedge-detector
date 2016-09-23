package org.ccls.nlp.cbt;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.ccls.nlp.cbt.Converters.JcasToConll2009;
import org.ccls.nlp.cbt.FeatureExtractors.BagCountsExtractor;
import org.ccls.nlp.cbt.FeatureExtractors.DepFeatureExtractor;
import org.ccls.nlp.cbt.FeatureExtractors.PairFeatureExtractor;
import org.ccls.nlp.cbt.FeatureExtractors.SentenceFeatureExtractor;
import org.ccls.nlp.cbt.FeatureExtractors.TokenFeatureExtractor;
import org.ccls.nlp.cbt.ts.BeliefAnn;
import org.ccls.nlp.cbt.utils.ConfusionMatrix;
import org.ccls.nlp.cbt.utils.PredictionUtils;
import org.ccls.nlp.cbt.utils.VerbnetMapper;
import org.ccls.nlp.cbt.utils.WordnetUtils;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkAnnotatorDescriptionFactory;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.ScoredOutcome;
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

import se.lth.cs.srl.CompletePipeline;
import se.lth.cs.srl.Parse;
import sg.edu.nus.comp.nlp.ims.implement.CTester;

import com.clearnlp.nlp.engine.NLPDecode;

import edu.stanford.nlp.util.StringUtils;

public class CommittedBeliefAnnotatorNoFolds extends CleartkAnnotator<String> {

	private List<SimpleFeatureExtractor> tokenFeatureExtractors;
	private List<SimpleFeatureExtractor> sentenceFeatureExtractors;
	private List<SimpleFeatureExtractor> bagCountsExtractors;
	private List<AnnotationPairFeatureExtractor> pairFeatureExtractors;
	private List<CleartkExtractor> contextFeatureExtractors;
	private FileWriter testpredsfile;

	public static Map<Integer, Integer> quoteMap = new HashMap<Integer, Integer>();
	public static Map<Integer, Integer> sentenceLengthMap = new HashMap<Integer, Integer>();
	public static Map<Integer, ConfusionMatrix> sentencePerformanceMap = new HashMap<Integer, ConfusionMatrix>();
	
	public static int tokenCount;
	public static int instanceCount;

	private List<Feature> imsSentenceFeatures;
	private List<Feature> allWordsSentenceFeatures;	
	
	public static List<Map<Token, String>> weiweiDocumentFeatures;

	private static Set<String> failedFeatures = new HashSet<String>();
	private TokenFeatureExtractor tokenFeatureExtractor;

	public static PrintWriter senseDatabase;
	public static int documentNumber = 1;

	public static String featureSet = "SINGLE";
	public static Hashtable<Outcome,Hashtable<String,String>> outcomeTable = new Hashtable<Outcome,Hashtable<String,String>>();
	public static int outcomeIndex = 1;
	public static Pattern pattern = Pattern.compile("length=\"[0-9]+ ([a-z0-9\\-\\_\\.\\']+\\%[0-9]+\\:[0-9]+\\:[0-9]+\\:[a-z0-9\\-\\_\\.\\']*\\:[0-9]*)\\|[0-9]+\\.[0-9]+\"");

	// this is the predetermined order of rarity.  NCB is most rare, O is most common
	public static final List<String> minorities = Arrays.asList("NCB", "NA", "RB", "CB", "O");

	public CommittedBeliefAnnotatorNoFolds()
	{
		tokenFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
		tokenFeatureExtractor = new TokenFeatureExtractor();
		tokenFeatureExtractors.add(tokenFeatureExtractor);
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

	private void completePipelineBit(Collection<Sentence> sentences) {
		try {
			PrintWriter pw = new PrintWriter("temp.txt");
			for (Sentence sentence : sentences) {
				pw.println(sentence.getCoveredText());
			}
			pw.close();

			String[] srlArgs = new String[]{
					"eng",
					"-tagger", "srl_models/CoNLL2009-ST-English-ALL.anna-3.3.postagger.model",
					"-parser", "srl_models/CoNLL2009-ST-English-ALL.anna-3.3.parser.model",
					"-srl", "srl_models/CoNLL2009-ST-English-ALL.anna-3.3.srl-4.1.srl.model",
					"-lemma", "srl_models/CoNLL2009-ST-English-ALL.anna-3.3.lemmatizer.model",
					"-tokenize",
					"-test", "temp.txt",
					"-out", "temp_results.txt"
			};
			CompletePipeline.main(srlArgs);

			File f = new File("temp.txt");
			f.delete();

			FileReader fr = new FileReader("temp_results.txt");
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String line = br.readLine();
				System.out.println(line);
			}
			fr.close();

			f = new File("temp_results.txt");
			f.delete();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void cleanNlpBit(Collection<Sentence> sentences) {
		try {
			PrintWriter pw = new PrintWriter("temp.txt");
			for (Sentence sentence : sentences) {
				pw.println(sentence.getCoveredText());
			}
			pw.close();		

			String[] srlArgs = new String[]{
					"-z","srl",
					"-c","C:\\Users\\OFFLINE-GREG\\Desktop\\clearnlp-lib-2.0.2\\config.xml",
					"-i","temp.txt"
			};		

			NLPDecode.main(srlArgs);

			/*FileReader fr = new FileReader("temp.txt.cnlp");
			BufferedReader br = new BufferedReader(fr);
			System.out.println("********************");
			while (br.ready()) {
				String line = br.readLine();
				System.out.println(line);
			}
			fr.close();
			System.out.println("********************");*/

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);			
		}
	}

	private void mateBit(JCas jCas) {
		try {
			JcasToConll2009.convert(jCas);

			String[] srlArgs = new String[]{
					"eng",
					"temp.txt",
					"C:\\Users\\Greg\\Desktop\\NLP\\srl-4.31\\srl-20131216\\srl-eng.model",
					"temp_results.txt",
			};
			Parse.main(srlArgs);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}

	// this is necessary because you can't split on space for IMS output.  There is space embedded in the x tags
	// which messes up that whole scheme.  We need to be more clever.
	public List<String> tokenize(String line) {
		int marker = 0;
		int end = line.length();
		List<String> tokens = new ArrayList<String>();
		while (marker < end) {
			if (end - marker == 1 || !line.substring(marker, marker + 2).equals("<x")) {
				// no tag
				tokens.add(null);
				int nextSpace = line.indexOf(' ', marker);
				if (nextSpace == -1) {
					marker = end;
				} else {
					marker = nextSpace + 1;
				}
			} else {
				// tag				
				tokens.add(line.substring(marker, 4 + line.indexOf("</x>", marker)));
				marker = line.indexOf("</x>", marker) + 5;
			}
		}
		return tokens;
	}

	private String fixTextForAllWords(String raw) {
		return raw.replace("_", "uscorz");
	}

	public List<List<Feature>> allWordsBit(JCas jCas, List<Sentence> sentences) {
		List<List<Feature>> toReturn = new ArrayList<List<Feature>>();
		StringBuilder sb = new StringBuilder();
		for (int s = 0; s < sentences.size(); s++) {
			Sentence sentence = sentences.get(s);
			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
			for (int t = 0; t < tokens.size(); t++) {
				Token token = tokens.get(t);
				if (t > 0) {
					sb.append(" ");
				}
				sb.append(fixTextForAllWords(token.getLemma().replaceAll(" ", "_")) + "/" + token.getPos());
			}
			if (s != sentences.size() - 1) {
				sb.append(System.getProperty("line.separator"));
			}
		}
		PrintStream ps;
		try {
			ps = new PrintStream("temp.txt");
			String finalString = sb.toString();
			finalString = finalString.replaceAll("\\:", "-colon-");
			finalString = finalString.replaceAll("\\$", "");
			finalString = finalString.replaceAll("(?<=[\\d])(,)(?=[\\d])","");
			finalString = finalString.replaceAll("([0-9]+)\\u00a0([0-9]+/[0-9]+)", "$1$2");
			ps.println(finalString);
			ps.close();

			// here is where we need to call the perl script
			Process process = Runtime.getRuntime().exec("perl /home/gwerner/Desktop/WordNet-SenseRelate-AllWords-0.19/utils/wsd.pl --context temp.txt --format tagged --outfile temp_out.txt");
			InputStream stdout = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdout);
			BufferedReader br = new BufferedReader(isr);
			process.waitFor();

			for (int s = 0; s < sentences.size(); s++) {
				List<Feature> featureList = new ArrayList<Feature>();
				String line = br.readLine();
				List<String> parts = Arrays.asList(line.split(" "));
				List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentences.get(s));
				if (parts.size() != tokens.size()) {
					System.out.println(documentNumber);
					System.out.println(s);
					System.out.println(line);
					System.out.println(sentences.get(s).getCoveredText());
					System.err.println("Uh oh, you're in trouble " + parts.size() + " : " + tokens.size());
					System.exit(1);
					for (int t = 0; t < tokens.size(); t++) {
						//featureList.add(new Feature("AllWords", null));
						featureList.add(new Feature("AllWords-OntoSense", null));
					}
					toReturn.add(featureList);
					continue;
				}
				for (int t = 0; t < parts.size(); t++) {
					Token token = tokens.get(t);
					String pos = token.getPos();
					if (pos.charAt(0) != 'N' && pos.charAt(0) != 'V') {
						//featureList.add(new Feature("AllWords", null));
						featureList.add(new Feature("AllWords-OntoSense", null));
						continue;
					}
					String par = parts.get(t);
					String allWordsPos = par.split("#")[1];
					if (allWordsPos.equals("n") || allWordsPos.equals("v")) {
						//featureList.add(new Feature("AllWords", par));
						List<String> senses = tokenFeatureExtractor.getNounSenseMapper().getNounFullSenseClasses(par);
						String sense = null;
						if (senses.size() == 1) {
							sense = senses.get(0);
						} else if (senses.size() > 1) {
							sense = StringUtils.join(senses, "***");
						}
						featureList.add(new Feature("AllWords-OntoSense", sense));
					} else {
						//featureList.add(new Feature("AllWords", null));
						featureList.add(new Feature("AllWords-OntoSense", null));
					}
				}
				toReturn.add(featureList);
			}
			br.close();
			isr.close();
			stdout.close();

			return toReturn;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);			
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);	
		}	

		return null;

	}

	public List<List<Feature>> imsBit(JCas jCas, List<Sentence> sentences) {
		List<List<Feature>> toReturn = new ArrayList<List<Feature>>();
		StringBuilder sb = new StringBuilder();
		for (int s = 0; s < sentences.size(); s++) {
			Sentence sentence = sentences.get(s);
			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
			for (int t = 0; t < tokens.size(); t++) {
				Token token = tokens.get(t);
				if (t > 0) {
					sb.append(" ");
				}
				sb.append(token.getCoveredText());
			}
			if (s != sentences.size() - 1) {
				sb.append(System.getProperty("line.separator"));
			}
		}
		PrintStream ps;
		try {
			String outFilename = "temp_out.txt";
			ps = new PrintStream("temp.txt");
			ps.println(sb.toString());
			ps.close();

			String[] args = {"-ptm", "lib/tag.bin.gz", "-tagdict", "lib/tagdict.txt", 
					"-ssm", "lib/EnglishSD.bin.gz", "-prop", "lib/prop.xml",
					"-f", "sg.edu.nus.comp.nlp.ims.feature.CAllWordsFeatureExtractorCombination",
					"-c", "sg.edu.nus.comp.nlp.ims.corpus.CAllWordsPlainCorpus",
					"-r", "sg.edu.nus.comp.nlp.ims.io.CPlainCorpusResultWriter",
					"-is", "lib/wn21.index.sense",
					"temp.txt",
					"models",
					"models",
					outFilename,
					"-delimiter", "/",
					"-split", "1",
					"-token", "1",
					"-pos", "0",
					"-lemma", "0"
			};

			CTester.main(args);

			FileReader fr = new FileReader(outFilename);
			BufferedReader br = new BufferedReader(fr);
			for (int s = 0; s < sentences.size(); s++) {
				List<Feature> featureList = new ArrayList<Feature>();
				String line = br.readLine();
				List<String> parts = tokenize(line);
				List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentences.get(s));
				if (parts.size() != tokens.size()) {
					System.err.println("Uh oh, you're in trouble " + parts.size() + " : " + tokens.size());
					for (int t = 0; t < tokens.size(); t++) {
						featureList.add(new Feature("IMS", null));
					}
					toReturn.add(featureList);
					continue;
				}
				for (int t = 0; t < parts.size(); t++) {
					String par = parts.get(t);
					if (par == null) {
						featureList.add(new Feature("IMS", par));
					} else {
						Matcher matcher = pattern.matcher(par);
						matcher.find();
						String senseKey = matcher.group(1);
						String coarseSense = senseKey.split(":")[0];
						featureList.add(new Feature("IMS", coarseSense));
					}
				}
				toReturn.add(featureList);
			}
			br.close();
			fr.close();

			return toReturn;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);			
		}	

		return null;

	}

	public List<Boolean> getQuoteParts(JCas jCas, Sentence sentence) {
		List<Boolean> toReturn;
		List<Integer> spots = new ArrayList<Integer>();
		List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
		for (int t = 0; t < tokens.size(); t++) {
			if (tokens.get(t).getPos().equals("``") || (tokens.get(t).getPos().equals("''"))) {
				spots.add(t);
			}
		}
		toReturn = new ArrayList<Boolean>(Collections.nCopies(tokens.size(), false));
		if (spots.size() == 0) {
			return toReturn;
		}
		int mode = 0;
		for (int t = 0; t < tokens.size(); t++) {
			if (mode == 0) {
				if (tokens.get(t).getPos().equals("``")) {
					mode = 2;
				} else if (tokens.get(t).getPos().equals("''")) {
					for (int index = 0; index < toReturn.size(); index++) {
						toReturn.set(index, true);
					}
				}
				toReturn.add(false);
			} else if (mode == 2) {
				if (tokens.get(t).getPos().equals("''")) {
					mode = 0;
					toReturn.add(false);
				} else {
					toReturn.add(true);
				}				
			} else {
				System.err.println("State machine error: mode " + mode);
				System.exit(1);
			}
		}
		
		if (!quoteMap.containsKey(spots.size())) {
			quoteMap.put(spots.size(), 0);
		}
		quoteMap.put(spots.size(), 1 + quoteMap.get(spots.size()));
		
		return toReturn;
	}

	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		outcomeTable.clear();
		try {
			testpredsfile = new FileWriter(new File(AnnotationPipeline.DIR_NAME  + "/TestResults.txt" ));
			senseDatabase = new PrintWriter("Sense_Database.csv");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}		
		super.initialize(context);
	}

	public void tokenAnnotation(JCas jCas, Sentence sentence, int s, List<Token> tokens) throws CleartkProcessingException {		
		List<Instance<String>> instances = new ArrayList<Instance<String>>();

		// ok, which tokens are within a quote and which not
		List<Boolean> underQuote = getQuoteParts(jCas, sentence);
		
		Token lastToken = null;		
		
		// for each token, extract all feature values and the label
		for (int t = 0; t < tokens.size(); t++) {
			Token token = tokens.get(t);
			
			Instance<String> instance = new Instance<String>();

			if (underQuote.get(t)) {
				instance.add(new Feature("UnderQuote", true));
			}
			
			// Check already computed Weiwei WSD output feature (based on document)

			/*Feature weiweiFeature = new Feature("Weiwei", weiweiDocumentFeatures.get(s).get(token));
			if (wantedFeature(weiweiFeature) && weiweiFeature.getValue() != null) {
				instance.add(weiweiFeature);
			}*/

			// at this point, we must do the preceding and following features
			/*Feature weiweiPreceding20Feature = new Feature("Preceding_0_2_0_Weiwei", WeiweiWSD.getDirectionalSense(weiweiDocumentFeatures, false, 1, s, t, tokens));
			if (wantedFeature(weiweiPreceding20Feature) && weiweiPreceding20Feature.getValue() != null) {
				instance.add(weiweiPreceding20Feature);
			}
			Feature weiweiPreceding21Feature = new Feature("Preceding_0_2_1_Weiwei", WeiweiWSD.getDirectionalSense(weiweiDocumentFeatures, false, 2, s, t, tokens));
			if (wantedFeature(weiweiPreceding21Feature) && weiweiPreceding21Feature.getValue() != null) {
				instance.add(weiweiPreceding21Feature);
			}
			Feature weiweiFollowing20Feature = new Feature("Following_0_2_0_Weiwei", WeiweiWSD.getDirectionalSense(weiweiDocumentFeatures, true, 1, s, t, tokens));
			if (wantedFeature(weiweiFollowing20Feature) && weiweiFollowing20Feature.getValue() != null) {
				instance.add(weiweiFollowing20Feature);
			}
			Feature weiweiFollowing21Feature = new Feature("Following_0_2_1_Weiwei", WeiweiWSD.getDirectionalSense(weiweiDocumentFeatures, true, 2, s, t, tokens));
			if (wantedFeature(weiweiFollowing21Feature) && weiweiFollowing21Feature.getValue() != null) {
				instance.add(weiweiFollowing21Feature);
			}*/

			// extract all features that require only the token annotation
			for (SimpleFeatureExtractor extractor : sentenceFeatureExtractors) {
				((SentenceFeatureExtractor) extractor).setCurrentToken(token);
				((SentenceFeatureExtractor) extractor).setStartOfSentence(lastToken == null);

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
				if (extractor instanceof DepFeatureExtractor) {
					//((DepFeatureExtractor) extractor).setSentenceNumber(s);
					//((DepFeatureExtractor) extractor).setWeiweiDocumentFeatures(weiweiDocumentFeatures);
				}
				for (Feature feature : extractor.extract(jCas, token)) {
					if (wantedFeature(feature)) {
						instance.add(feature);
					}
				}
			}

			// extract all features that require the token and sentence annotations
			for (CleartkExtractor extractor : this.contextFeatureExtractors) {
				for (Feature feature : extractor.extractWithin(jCas, token, sentence)) {
					if (wantedFeature(feature)) {
						instance.add(feature);
					}
				}					  
			}

			// set the instance label from annotation
			if (this.isTraining()) {
				if (JCasUtil.selectCovered(jCas, BeliefAnn.class,token).size() > 0) {
					instance.setOutcome(JCasUtil.selectCovered(jCas, BeliefAnn.class,token).get(0).getTag());
				}
				else{
					instance.setOutcome("O");
				}
			}
			else {
				// Place holder

			}

			// add the instance to the list
			instances.add(instance);

			lastToken = token;
		}

		// for training, write instances to the data write
		if (this.isTraining()) {
			for (Instance<String> instance : instances) {
				dataWriter.write(instance);
			}
		}

		// for classification/testing
		else {			
			Iterator<Token> tokensIter = tokens.iterator();
			List<String> beliefSequence = new ArrayList<String>();

			Integer sentenceLength = tokens.size();
			if (!sentenceLengthMap.containsKey(sentenceLength)) {
				sentenceLengthMap.put(sentenceLength, 0);
			}
			sentenceLengthMap.put(sentenceLength, 1 + sentenceLengthMap.get(sentenceLength));
			
			if (!sentencePerformanceMap.containsKey(sentenceLength)) {
				sentencePerformanceMap.put(sentenceLength, new ConfusionMatrix());
			}
			ConfusionMatrix cm = sentencePerformanceMap.get(sentenceLength);
			
			for (Instance<String> instance : instances) {
				List<Feature> features = instance.getFeatures();
				String label = this.classifier.classify(features);

				Token token = tokensIter.next();

				String actual, prediction;

				List<ScoredOutcome<String>> scores = this.classifier.score(features,  5);
				List<String> acceptableTags = new ArrayList<String>();
				for (int index = 1; index < scores.size(); index++) {
					ScoredOutcome<String> so = scores.get(index);
					if (scores.get(index).getScore() >= 0.50) {
						acceptableTags.add(so.getOutcome());
					}
				}
				Outcome outcome = new Outcome(instance, outcomeIndex);
				outcomeTable.put(outcome, new Hashtable<String,String>());

				if (JCasUtil.selectCovered(jCas, BeliefAnn.class,token).size() > 0) {
					actual = JCasUtil.selectCovered(jCas, BeliefAnn.class,token).get(0).getTag();
				}
				else {
					actual = "O";
				}
				
				prediction = PredictionUtils.getLabel(label, acceptableTags);				
				
				cm.addEntry(actual, prediction);
				
				if (!prediction.equals("O")) {
					beliefSequence.add(prediction);
				}

				
				writeEvalInstance(testpredsfile, jCas, instance, actual, prediction, sentence);
				outcomeTable.get(outcome).put("pred", prediction);
				outcomeTable.get(outcome).put("actual", actual);

				senseDatabase.println("\"" + token.getLemma() + "\"" + "," + "\"" + token.getPos() + "\"" + "," + WordnetUtils.getSenseNumber(token.getLemma(), token.getPos()) + "," + prediction + "," + actual);

				outcomeIndex++;
			}

			// if we were to do post-processing here is where it would go
			// we might need to bring some stuff out of the loop though, if were to do 

		}		

	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		// Check this next line out when you get back
		//DocumentAnnotation da = JCasUtil.select(jCas, DocumentAnnotation.class);

		List<Sentence> sentences = new ArrayList<Sentence>(JCasUtil.select(jCas, Sentence.class));

		//List<List<Feature>> imsDocumentFeatures = imsBit(jCas, sentences);
		//List<List<Feature>> allWordsDocumentFeatures = allWordsBit(jCas, sentences);
		//weiweiDocumentFeatures = WeiweiWSD.weiweiBit(jCas, sentences, this.isTraining(), true, false);

		VerbnetMapper verbnetMapper = new VerbnetMapper();

		for (int s = 0; s < sentences.size(); s++) {

			Sentence sentence = sentences.get(s);
			//imsSentenceFeatures = imsDocumentFeatures.get(s);
			//allWordsSentenceFeatures = allWordsDocumentFeatures.get(s);

			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
			/*List<Token> verbClassTokens = new ArrayList<Token>();
			for (Token token : tokens) {
				verbClassTokens.add((Token) token.clone());
			}
			List<Token> imsTokens = new ArrayList<Token>();
			for (Token token : tokens) {
				imsTokens.add((Token) token.clone());
			}*/
			/*			for (int t = 0; t < verbClassTokens.size(); t++) {
				String pos = tokens.get(t).getPos();
				if (pos.charAt(0) == 'V' || pos.charAt(0) == 'N') {
					if (pos.charAt(0) == 'V') {
						Token token = verbClassTokens.get(t);
						if (verbnetMapper.getVerbNetClasses(token.getLemma()).size() == 1) {
							List<String> vnClasses = verbnetMapper.getVerbNetClasses(token.getLemma());
							String sense = vnClasses.get(0);
							String lemma = sense.split("-")[0];
							token.setLemma(sense);
						}
					}
					if (pos.charAt(0) =='V' | pos.charAt(0) =='N') {
						Token token = imsTokens.get(t);
						Feature imsFeature = imsSentenceFeatures.get(t);
						Object value = imsFeature.getValue();
						if (value != null) {
							String senseKey = value.toString();
							String coarseSense = senseKey.split(":")[0];
							token.setLemma(coarseSense);
						}
					}
				}
			}*/

			tokenAnnotation(jCas, sentence, s, tokens);
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
				//System.out.println("FAIL: " + thisFeature.getName());
				failedFeatures.add(thisFeature.getName());
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
		return AnalysisEngineFactory.createPrimitiveDescription(
				CommittedBeliefAnnotatorNoFolds.class, 
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