package org.ccls.nlp.cbt.FeatureExtractors;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.examples.xmi.XmiCollectionReader;
import org.apache.uima.jcas.JCas;
import org.ccls.nlp.cbt.CommittedBeliefWeiweiLabeler;
import org.ccls.nlp.cbt.utils.FileUtils;
import org.ccls.nlp.cbt.utils.StreamGobbler;
import org.ccls.nlp.cbt.utils.WordnetUtils;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

public class WeiweiWSD {

	public static final Map<String, Integer> weiweiCountMap = new HashMap<String, Integer>();
	public static final Map<String, Set<String>> sensesForLemma = new HashMap<String, Set<String>>();
	public static final Map<String, Set<String>> lemmataForSense = new HashMap<String, Set<String>>();
	
	/**
	 * This method captures the output for Weiwei Guo's WSD tool
	 * It is meant to be run on a single document.
	 * A single call to the tool takes a bit of time
	 * and so running on many documents could eat up 4 hours on a fast machine.
	 * We provide copy to disc and lookup from disc
	 * so that each execution of this does not eat up all of our time
	 * 
	 * @param jCas - The jCas
	 * @param sentences - A list of sentences for (presumably) a single document
	 * @param isTraining - If not training then testing/classification
	 * @param fromDisc - Has tool output already been captured on disc?
	 * @param copyToDisc - Should tool output be stored on disc (for future use)?  Meaningless if fromDisc is true.
	 * @return List of features values which can be loaded in Feature objects
	 */
	public static List<Map<Token, String>> weiweiBit(JCas jCas, List<Sentence> sentences, boolean isTraining, boolean fromDisc, boolean copyToDisc) {
		String filename = FileUtils.getFilename(jCas);
		String prefix = FileUtils.getDocumentPrefixFilename(filename);		
		prefix = (isTraining ? "train_" : "test_") + prefix;
		List<Map<Token, String>> toReturn = new ArrayList<Map<Token, String>>();

		if (fromDisc) {
			try {
				BufferedReader br = new BufferedReader(new FileReader("weiwei/" + prefix + "_temp_out.txt"));
				for (int s = 0; s < sentences.size(); s++) {
					Map<Token, String> featureMap = new HashMap<Token, String>();
					Sentence sentence = sentences.get(s);
					for (Token token : JCasUtil.selectCovered(jCas, Token.class, sentence)) {
						String lemma = token.getLemma().toLowerCase();
						String wordnetPos = WordnetUtils.getWordnetPos(token.getPos());
						if (wordnetPos.equals("uk")) {
							featureMap.put(token, null);
							continue;
						}
						String result = br.readLine();
						if (result.length() == 0) {
							featureMap.put(token, null);
						} else {
							featureMap.put(token, result);
							if (!weiweiCountMap.containsKey(result)) {
								weiweiCountMap.put(result, 0);
							}
							weiweiCountMap.put(result, 1 + weiweiCountMap.get(result));
							if (!sensesForLemma.containsKey(lemma)) {
								sensesForLemma.put(lemma, new HashSet<String>());
							}
							sensesForLemma.get(lemma).add(result);
							if (!lemmataForSense.containsKey(result)) {
								lemmataForSense.put(result, new HashSet<String>());
							}
							lemmataForSense.get(result).add(lemma);
						}
					}
					toReturn.add(featureMap);
				}
				br.close();
			} catch (IOException ioe) {
				System.err.println("FATAL: Cannot read " + prefix);
				System.exit(1);
			}
			return toReturn;
		} else {
			StringBuilder sb = new StringBuilder();
			for (int s = 0; s < sentences.size(); s++) {
				Sentence sentence = sentences.get(s);
				boolean seenNothing = true;
				for (Token token : JCasUtil.selectCovered(jCas, Token.class, sentence)) {
					String wordnetPos = WordnetUtils.getWordnetPos(token.getPos());
					if (wordnetPos.equals("uk")) {
						continue;
					}
					if (seenNothing) {
						seenNothing = false;
					}
					else {
						sb.append(" ");
					}
					sb.append(token.getLemma().toLowerCase().replaceAll(" ","_") + "#" + wordnetPos + "#1");
				}
				if (!seenNothing && s != sentences.size() - 1) {
					sb.append(System.getProperty("line.separator"));
				}
			}

			PrintStream ps;
			try {
				String outFilename = "temp_out.txt";
				ps = new PrintStream("temp.txt");
				String finalString = sb.toString();
				finalString = finalString.replaceAll("([0-9]+)\\u00a0([0-9]+/[0-9]+)", "$1\\_$2");
				ps.println(finalString);
				ps.close();

				// here is where we need to call the perl script
				System.out.println("Process Begin");
				Process process = Runtime.getRuntime().exec("perl /home/gwerner/Desktop/weiwei_wsd/bin/run.pl");

				StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");
				errorGobbler.start();

				int exitVal = process.waitFor();
				System.out.println("Process End with " + exitVal);

				FileReader fr = new FileReader(outFilename);
				BufferedReader br = new BufferedReader(fr);

				for (int s = 0; s < sentences.size(); s++) {
					Map<Token, String> featureMap = new HashMap<Token, String>();
					for (Token token : JCasUtil.selectCovered(jCas, Token.class, sentences.get(s))) {
						String wordnetPos = WordnetUtils.getWordnetPos(token.getPos());
						if (wordnetPos.equals("uk")) {
							featureMap.put(token, null);
							continue;
						}
						String par = br.readLine();
						if (par.length() > 0) {
							featureMap.put(token, par);
						} else {
							featureMap.put(token, null);
						}
					}
					toReturn.add(featureMap);
				}
				br.close();
				fr.close();

				if (copyToDisc) {
					System.out.println("Copying files for posterity");
					FileUtils.moveFile("./", "temp.txt", "weiwei/", prefix + "_temp.txt");
					FileUtils.moveFile("./", "temp_out.txt", "weiwei/", prefix + "_temp_out.txt");
					System.out.println("Done copying files for posterity");
				}

				//System.out.println("For this document we have " + wwNN + " non-null and " + wwN + " null");

				return toReturn;

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);			
			} catch (InterruptedException ie) {
				ie.printStackTrace();
				System.exit(1);						
			}

			return null;

		}
	}	
	
	public static String getDirectionalSense(List<Map<Token, String>> features, boolean forward, int steps, int s, int t, List<Token> tokens) {
		if (forward) {
			if (t + steps >= tokens.size()) {
				return null;
			}
			return features.get(s).get(tokens.get(t + steps));
		} else {
			if (t - steps < 0) {
				return null;
			}
			return features.get(s).get(tokens.get(t - steps));
		}
	}
	
	public static void main(String[] args) throws UIMAException, IOException {
		String trainDir = "factbank_data/test/tagged";
		
		CollectionReader xmiReader =  CollectionReaderFactory.createCollectionReader(
				XmiCollectionReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
				XmiCollectionReader.PARAM_INPUTDIR,
				trainDir 
				);

		SimplePipeline.runPipeline(    		
				xmiReader,
				AnalysisEngineFactory.createPrimitiveDescription(
						CommittedBeliefWeiweiLabeler.class,
						TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem")
						)
				);		
	}
}