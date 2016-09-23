package org.ccls.nlp.cbt;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.ccls.nlp.cbt.Converters.JcasToConll2009;
import org.ccls.nlp.cbt.FeatureExtractors.DepFeatureExtractor;
import org.ccls.nlp.cbt.FeatureExtractors.PairFeatureExtractor;
import org.ccls.nlp.cbt.FeatureExtractors.SentenceFeatureExtractor;
import org.ccls.nlp.cbt.FeatureExtractors.TokenFeatureExtractor;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.Counts;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.annotationpair.AnnotationPairFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.util.JCasUtil;

import com.clearnlp.nlp.engine.NLPDecode;

import se.lth.cs.srl.CompletePipeline;
import se.lth.cs.srl.Parse;

public class FeatureAnnotator extends JCasAnnotator_ImplBase {

	private List<SimpleFeatureExtractor> tokenFeatureExtractors;
	private List<SimpleFeatureExtractor> sentenceFeatureExtractors;
	private List<AnnotationPairFeatureExtractor> pairFeatureExtractors;
	private List<CleartkExtractor> contextFeatureExtractors;

	// Things which need to be set from outside
	public static String featureSet = "SINGLE";
	public static int currentFold = -1;
	public static int cvf = -1;

	// this is the predetermined order of rarity.  NCB is most rare, O is most common
	public static final List<String> minorities = Arrays.asList("NCB", "NA", "RB", "CB", "O");

	public FeatureAnnotator()
	{
		tokenFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
		tokenFeatureExtractors.add(new TokenFeatureExtractor());
		tokenFeatureExtractors.add(new DepFeatureExtractor());
		sentenceFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();		
		sentenceFeatureExtractors.add(new SentenceFeatureExtractor());
		pairFeatureExtractors = new ArrayList<AnnotationPairFeatureExtractor>();
		pairFeatureExtractors.add(new PairFeatureExtractor());
		contextFeatureExtractors = new ArrayList<CleartkExtractor>();
		contextFeatureExtractors.add(new CleartkExtractor(Token.class, new TokenFeatureExtractor(), new Preceding(2),new Following(2)));
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

	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		
		Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);

		//completePipelineBit(sentences);
		//mateBit(jCas);
		cleanNlpBit(sentences);

		for (Sentence sentence : sentences) {

			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);

			Token lastToken = null;

			// for each token, extract all feature values and the label
			for (Token token : tokens) {

				// extract all features that require only the token annotation
				for (SimpleFeatureExtractor extractor : sentenceFeatureExtractors) {
					for (Feature feature : extractor.extract(jCas, token)) {
						if (wantedFeature(feature)) {
							token.removeFromIndexes();
							token.addToIndexes();
						}
					}
				}				

				// all pair extractors
				for (AnnotationPairFeatureExtractor extractor : pairFeatureExtractors) {
					for (Feature feature : extractor.extract(jCas, lastToken, token)) {
						if (wantedFeature(feature)) {
							token.removeFromIndexes();
							token.addToIndexes();
						}
					}					
				}				

				// extract all features that require only the token annotation
				for (SimpleFeatureExtractor extractor : tokenFeatureExtractors) {
					for (Feature feature : extractor.extract(jCas, token)) {
						if (wantedFeature(feature)) {
							token.removeFromIndexes();
							token.addToIndexes();
						}
					}
				}

				// extract all features that require the token and sentence annotations
				for (CleartkExtractor extractor : this.contextFeatureExtractors) {
					for (Feature feature : extractor.extractWithin(jCas, token,sentence)) {
						if (wantedFeature(feature)) {
							token.removeFromIndexes();
							token.addToIndexes();
						}
					}					  
				}

				lastToken = token;
			}

		}		
	}

	public boolean wantedFeature(Feature thisFeature) {
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
				//if (((Counts)thisFeature.getValue()).getFeatureName().equals(wantedFeature)) {
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

	public static AnalysisEngineDescription getDescription()
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(
				FeatureAnnotator.class, 
				TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem")
				);
	}	

}