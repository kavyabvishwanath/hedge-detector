package org.ccls.nlp.cbt;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.examples.xmi.XmiCollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasCreationUtils;
import org.ccls.nlp.cleartk.tweakers.StanfordCoreNLPAnnotator;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.svmlight.SVMlightStringOutcomeDataWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.cleartk.classifier.jar.Train;

import edu.stanford.nlp.util.StringUtils;

public class Experimenter_fb_4_svmlite {

	private CrossValidator crossValidator;	  
	private TrainAndTest trainAndTest;
	private List<String> featureSets = new ArrayList<String>();
	private String modelfile, rawInputFilesDir;

	public Experimenter_fb_4_svmlite() throws IOException{
		crossValidator = new CrossValidator(1);
		featureSets = getFeatureSets("conf/fs_current.txt");
		System.out.println(featureSets);
	}
	
	public Experimenter_fb_4_svmlite(String filepath) throws IOException{
		crossValidator = new CrossValidator(1);
		featureSets = getFeatureSets(filepath);
		System.out.println(featureSets);
	}


	private List<String> getFeatureSets(String featureSetFile) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(featureSetFile))));
		String strLine;
		List<String> featureSets = new ArrayList<String>();

		while ((strLine = br.readLine()) != null)   {
			if (strLine.equals("EOF") || strLine.trim().equals(""))
				break;
			if (strLine.startsWith("#"))
				continue;
			featureSets.add(strLine);						  
		}
		br.close();			
		return featureSets;
	}

	public static List<String> getCombinations(String featureList) {

		List<String> newFeatureSets = new ArrayList<String>();		  
		String[] features = featureList.split(",");
		int[] indices;

		for (int r = 1; r<=features.length; r++) {
			CombinationGenerator x = new CombinationGenerator (features.length, r);
			StringBuffer combination;
			while (x.hasMore ()) {
				combination = new StringBuffer ();
				indices = x.getNext ();
				for (int i = 0; i < indices.length; i++) {
					if (!combination.toString().equals(""))
						combination.append(",");
					combination.append (features[indices[i]]);
				}
				newFeatureSets.add(combination.toString ());
			}
		}
		return newFeatureSets;		  
	}

	public void doExperiment() {

		String refID, taskID, featureList, daID;

		try {
			for (String featureSet : featureSets) {
				System.out.println("Experimenter.doExperiment(): Working with feature set " + featureSet);
				Matcher featureSetMatcher = Pattern.compile("^(.*):([^:]+):([^:]+):(.*$)").matcher(featureSet);
				if (featureSetMatcher.find()) {
					taskID = featureSetMatcher.group(1);
					daID = featureSetMatcher.group(2);
					refID = featureSetMatcher.group(3);
					featureList = featureSetMatcher.group(4);
				} else {
					continue;
				}
				String newRefId;
				if (refID.startsWith("MUL*")) {
					Integer firstToKeep = null;
					try {
						firstToKeep = Integer.valueOf(refID.substring(4, refID.length()));
					} catch (NumberFormatException nfe) {
						System.err.println("Improper configuration option " + refID);
						System.exit(1);
					}
					if (firstToKeep < 1) {
						System.err.println("This configuration mode only works with integers greater than or equal to 1");
						System.exit(1);
					}
					List<String> interestingFeatures = StringUtils.split(featureList,",");
					String staticFeatureList = StringUtils.join(interestingFeatures.subList(0, firstToKeep), ",");
					featureList = StringUtils.join(interestingFeatures.subList(firstToKeep, interestingFeatures.size()), ",");

					int i=1;
					for (String newfeatureList : getCombinations(featureList)) {
						newfeatureList = staticFeatureList + "," + newfeatureList; 
						if (i<=0) {	// If you want to restart from (n+1)'th feature set, set i <= n here.
							i++;
							continue;
						}
						newRefId = refID + ";" + i;
						crossValidator.crossValidate(taskID,newfeatureList,newRefId);					
						i++;
					}					
				}
				else if (refID.startsWith("MUL")) {					  
					int i=1;
					for (String newfeatureList : getCombinations(featureList)) {
						if (i<=0) {	// If you want to restart from (n+1)'th feature set, set i <= n here.
							i++;
							continue;
						}
						newRefId = refID + ";" + i;
						crossValidator.crossValidate(taskID,newfeatureList,newRefId);					  
						i++;
					}

				} else {
					crossValidator.crossValidate(taskID,featureList,refID);	
				}
			}			  
		}
		catch (Exception e) {
			e.printStackTrace();
		}		  
	}

	private void createSmallerSentenceFiles() throws Exception {
		AnnotationPipeline.runFeatureExtractorPipelineSplitAndSave("factbank_data\\goldAnnotated", "factbank_data\\splitAnnotated");		
	}

	private void createXMIFiles(String indir, String outdir) throws Exception {
//		//AnnotationPipeline.runFeatureExtractorPipelineParseAndSave("factbank_data\\authorAnnotated", "factbank_data\\authorTagged");
//		System.out.println("Experimenter.rceateXMIFiles(): training set");
//		AnnotationPipeline.runFeatureExtractorPipelineParseAndSave("/Users/rupayanbasu/Documents/Belief/Data/Dataset2/CBTagger_Data/train2", "/Users/rupayanbasu/Documents/Belief/Data/Dataset2/CBTagger_Data/uima/train2");
//		System.out.println("Experimenter.createXMLFiles(): test set");
		AnnotationPipeline.runFeatureExtractorPipelineParseAndSave(indir, outdir);
	}

	// if you run this method, you should not do cross validation!
	// it is only meant for train->model->dev or train->model->test analysis
	private void createFeatureVectors(String mode, String outputDir) throws Exception {
		AnnotationWriter.mode = mode;
		AnnotationWriter.parts = 6;
		//AnnotationPipeline.runFeatureExtractorPipelineParseAndSave("factbank_data/goldAnnotated", outputDir);
		AnnotationPipeline.runFeatureExtractorPipelineParseAndSave("data_2014/test", outputDir);
	}

	private List<String> readFeatures() {
		List<String> toReturn = new ArrayList<String>();

		System.out.println("Working with feature set " + featureSets.get(0));
		Matcher featureSetMatcher = Pattern.compile("^(.*):([^:]+):([^:]+):(.*$)").matcher(featureSets.get(0));

		String refID = "", taskID, featureList = "", daID;

		if (featureSetMatcher.find()) {
			taskID = featureSetMatcher.group(1);
			daID = featureSetMatcher.group(2);
			refID = featureSetMatcher.group(3);
			featureList = featureSetMatcher.group(4);
		}		

		if (refID.startsWith("MUL*")) {
			Integer firstToKeep = null;
			try {
				firstToKeep = Integer.valueOf(refID.substring(4, refID.length()));
			} catch (NumberFormatException nfe) {
				System.err.println("Improper configuartion option " + refID);
				System.exit(1);
			}
			if (firstToKeep < 1) {
				System.err.println("This configuration mode only works with integers greater than or equal to 1");
				System.exit(1);
			}
			List<String> interestingFeatures = StringUtils.split(featureList,",");
			String staticFeatureList = StringUtils.join(interestingFeatures.subList(0, firstToKeep), ",");
			featureList = StringUtils.join(interestingFeatures.subList(firstToKeep, interestingFeatures.size()), ",");

			for (String newfeatureList : getCombinations(featureList)) {
				newfeatureList = staticFeatureList + "," + newfeatureList; 
				toReturn.add(newfeatureList);
			}
		} else if (refID.equals("MUL")) {					  
			int i=1;
			for (String newfeatureList : getCombinations(featureList)) {
				toReturn.add(newfeatureList);
			}

		} else {
			toReturn.add(featureList);
		}

		return toReturn;

	}

	// this trains and generates a model
	public void goldEcoreGeneration(String directory, String expId, String featureList) throws Exception
	{
		AnnotationPipeline pipeline = new AnnotationPipeline(expId,true);

		CommittedBeliefAnnotatorNoFolds.featureSet = featureList;

		AnnotationWriter.parts = 6;
		AnnotationWriter.mode = "train";

		CollectionReader xmiReader =  CollectionReaderFactory.createCollectionReader(
				XmiCollectionReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
				XmiCollectionReader.PARAM_INPUTDIR,
				directory 
				);
		SimplePipeline.runPipeline(    		
				xmiReader,
				AnalysisEngineFactory.createPrimitiveDescription(
						CommittedBeliefAnnotatorNoFolds.class,
						TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
						DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
						AnnotationPipeline.DIR_NAME,
						DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
						SVMlightStringOutcomeDataWriter.class.getName()
						)
						//AuthorAnnotatorNoFolds.getDescription(),
						//AnnotationWriter.getWriterDescription(AnnotationPipeline.DIR_NAME)
				);

		org.cleartk.classifier.jar.Train.main(AnnotationPipeline.DIR_NAME, "-t", "0");
	}

	private void tagTestFile(String directory) throws Exception {
		EvaluationLogger el;
		List<Map<Outcome,Map<String,String>>> outcomeTables = new ArrayList<Map<Outcome,Map<String,String>>>();

		CollectionReader xmiReader =  CollectionReaderFactory.createCollectionReader(
				XmiCollectionReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
				XmiCollectionReader.PARAM_INPUTDIR,
				directory
				);
		SimplePipeline.runPipeline(
				xmiReader,
				CommittedBeliefAnnotatorNoFolds.getClassifierDescription(AnnotationPipeline.DIR_NAME + "/model.jar"),
				AuthorAnnotatorNoFolds.getDescription()
				);

		outcomeTables.add((Map<Outcome,Map<String,String>>) CommittedBeliefAnnotatorNoFolds.outcomeTable.clone());

		System.out.println(outcomeTables.get(0).size() + " is the hashtable size at this point");

		System.out.println(CommittedBeliefAnnotatorNoFolds.sentenceLengthMap);
		System.out.println(CommittedBeliefAnnotatorNoFolds.sentencePerformanceMap);

		//ssCommittedBeliefAnnotatorNoFolds.senseDatabase.close();
		//CommittedBeliefAnnotatorNoFolds.dict.close();

		//PrintWriter paw = new PrintWriter("weiweiCountMap.txt");
		//for (String weiweiSense : WeiweiWSD.weiweiCountMap.keySet()) {
		//	paw.println(weiweiSense + "\t" + WeiweiWSD.weiweiCountMap.get(weiweiSense));
		//}
		//paw.close();

		el = new EvaluationLogger(AnnotationPipeline.DIR_NAME);
		el.write(CommittedBeliefAnnotatorNoFolds.featureSet + "\n");			  
		el.write(Evaluator.getMicroAveragedPRFMultiClass(outcomeTables, "pred"));			  
		el.close();

	}

	private void tagRawFile(String documentText, String model) throws Exception {
		AnnotationPipeline ap = new AnnotationPipeline("TRF",true);
		JCas jCas = CasCreationUtils.createCas(CommittedBeliefAnnotatorNoFolds.getClassifierDescription(model)).getJCas();
		jCas.setDocumentText(documentText);
		SimplePipeline.runPipeline(jCas, 
				StanfordCoreNLPAnnotator.getDescription(),
				CommittedBeliefAnnotatorNoFolds.getClassifierDescription(model));
	}

	// The purpose of this method is to:
	// 1) Read through each test data file
	// 2) Collect for each sentence the sequence of gold belief tags
	// 3) Present this in fashion such that we can:
	//     a) Easily access any size belief n-grams
	//     b) Present aggregate information on whole chains
	private void collectBeliefSequences() throws UIMAException, IOException {
		String trainDir = "factbank_data/train/tagged";

		CollectionReader xmiReader =  CollectionReaderFactory.createCollectionReader(
				XmiCollectionReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
				XmiCollectionReader.PARAM_INPUTDIR,
				trainDir 
				);	

		SimplePipeline.runPipeline(    		
				xmiReader,
				AnalysisEngineFactory.createPrimitiveDescription(
						CommittedBeliefSequencer.class,
						TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem")
						)
				);

		System.out.println(CommittedBeliefSequencer.sequences);

	}
	
	//Rupayan - new methods
	
	public void doTrainAndTest() {

		String refID, taskID, featureList, daID;
		String trainPath = "ldc_data_new/uima/train1";
		String testPath = "ldc_data_new/uima/dev1";
		
		trainAndTest = new TrainAndTest();

		try {
			for (String featureSet : featureSets) {
				System.out.println("Experimenter.doTrainAndTest(): Working with feature set " + featureSet);
				Matcher featureSetMatcher = Pattern.compile("^(.*):([^:]+):([^:]+):(.*$)").matcher(featureSet);

				if (featureSetMatcher.find()) {
					taskID = featureSetMatcher.group(1);
					daID = featureSetMatcher.group(2);
					refID = featureSetMatcher.group(3);
					featureList = featureSetMatcher.group(4);
				} else {
					continue;
				}
				String newRefId;
				if (refID.startsWith("MUL*")) {
					Integer firstToKeep = null;
					try {
						firstToKeep = Integer.valueOf(refID.substring(4, refID.length()));
					} catch (NumberFormatException nfe) {
						System.err.println("Improper configuartion option " + refID);
						System.exit(1);
					}
					if (firstToKeep < 1) {
						System.err.println("This configuration mode only works with integers greater than or equal to 1");
						System.exit(1);
					}
					List<String> interestingFeatures = StringUtils.split(featureList,",");
					String staticFeatureList = StringUtils.join(interestingFeatures.subList(0, firstToKeep), ",");
					featureList = StringUtils.join(interestingFeatures.subList(firstToKeep, interestingFeatures.size()), ",");

					int i=1;
					for (String newfeatureList : getCombinations(featureList)) {
						newfeatureList = staticFeatureList + "," + newfeatureList; 
						if (i<=0) {	// If you want to restart from (n+1)'th feature set, set i <= n here.
							i++;
							continue;
						}
						newRefId = refID + ";" + i;
						trainAndTest.trainAndTest(taskID,newfeatureList,newRefId,trainPath,testPath);					
						i++;
					}					
				}
				else if (refID.startsWith("MUL")) {					  
					int i=1;
					for (String newfeatureList : getCombinations(featureList)) {
						if (i<=0) {	// If you want to restart from (n+1)'th feature set, set i <= n here.
							i++;
							continue;
						}
						newRefId = refID + ";" + i;
						trainAndTest.trainAndTest(taskID,newfeatureList,newRefId,trainPath,testPath);					  
						i++;
					}

				} else {
					System.out.println(trainAndTest);
					trainAndTest.trainAndTest(taskID,featureList,refID,trainPath,testPath);	
				}
			}			  
		}
		catch (Exception e) {
			e.printStackTrace();
		}		  
	}
	
	private void tagPlainTextFiles(String indir, String model){
		rawInputFilesDir = indir;
		modelfile = model;
		
		CommittedBeliefTrainAndTestAnnotator.tagRawText = true;
		try{
			for (String featureSet : featureSets) {
				System.out.println("Experimenter.tagPlainTextFiles(): Working with feature set " + featureSet);
				Matcher featureSetMatcher = Pattern.compile("^(.*):([^:]+):([^:]+):(.*$)").matcher(featureSet);
				//Matcher featureSetMatcher = Pattern.compile("^(.*):([^:]+):(.*$)").matcher(featureSet);
				if (featureSetMatcher.find()) {
					TrainAndTest.tagTextFiles(rawInputFilesDir, modelfile, featureSet);
				} else {
					continue;
				}	
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void runTestsOnly(String model){
		modelfile = model;
		try{
			for (String featureSet : featureSets) {
				System.out.println("Experimenter.runTestsOnly(): Working with feature set " + featureSet);
				Matcher featureSetMatcher = Pattern.compile("^(.*):([^:]+):([^:]+):(.*$)").matcher(featureSet);
				if (featureSetMatcher.find()) {
					trainAndTest.onlyTest(featureSet, modelfile);
				} else {
					continue;
				}	
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void tomekEval() throws Exception {// this is not being used--Sardar 
		String inlineBeliefPath = "/Users/rupayanbasu/Documents/Belief/belief_eval/tomek_eval/ERE_InlineAnnotator/belief_inline";
		String ereAnnotationsPath = "/Users/rupayanbasu/Documents/Belief/belief_eval/tomek_eval/ERE_InlineAnnotator/annotation/";
		
		//model - not used, but required to call annotator 
		String model = "/Users/rupayanbasu/Documents/Belief/belief_eval/trained_models/linear_train/model.jar";
		AnnotationPipeline.runBeliefAndEntityExtractionPipeline(inlineBeliefPath, ereAnnotationsPath, model);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(Runtime.getRuntime().maxMemory());
		Experimenter_fb_4_svmlite experimenter = new Experimenter_fb_4_svmlite();
//		Experimenter_fb_4_svmlite experimenter = new Experimenter_fb_4_svmlite(args[0]);
		// this creates xmi representation of annotated corpus with features in place
		//experimenter.createFeatureVectors("train", "data_2014");
		//experimenter.createFeatureVectors("train", "factbank_data/train/tagged");
		//experimenter.createFeatureVectors("test", "factbank_data/test/tagged");
		//experimenter.createFeatureVectors("dev", "factbank_data/dev/tagged");
		//experimenter.createFeatureVectors("all", "factbank_data/tagged");
		// this trains and generates a model, then classifies against it

		/*List<String> allFeatures = experimenter.readFeatures();
		for (String featureList : allFeatures) {
			experimenter.goldEcoreGeneration("factbank_data/train/tagged", "CB_", featureList);
			experimenter.tagTestFile("factbank_data/test/tagged");
		}*/

		/*PrintWriter p1 = new PrintWriter("sense.txt");
		PrintWriter p2 = new PrintWriter("lemma.txt");
		for (String sense: WeiweiWSD.lemmataForSense.keySet()) {
			if (WeiweiWSD.lemmataForSense.get(sense).size() > 1) {
				p1.println(sense + "\t" + WeiweiWSD.lemmataForSense.get(sense));
			}
		}
		for (String lemma: WeiweiWSD.sensesForLemma.keySet()) {
			if (WeiweiWSD.sensesForLemma.get(lemma).size() > 1) {
				p2.println(lemma + "\t" + WeiweiWSD.sensesForLemma.get(lemma));
			}			
		}
		p2.close();
		p1.close();*/
		//experimenter.createSmallerSentenceFiles();
		
		//Rupayan - new methods
		experimenter.doTrainAndTest();
		//experimenter.tomekEval();
//		experimenter.tagPlainTextFiles("/Users/rupayanbasu/Desktop/belief_eval/data/eval_data","/Users/rupayanbasu/Desktop/belief_eval/trained_models/quad_train1");
		//experimenter.tagPlainTextFiles("/Users/monadiab/Beleif/new/LatestCBTaggerPackage_VPtoSH/CBTagger_fb_4_svmlite/data","/Users/monadiab/Beleif/new/LatestCBTaggerPackage_VPtoSH/CBTagger_fb_4_svmlite/CB_FIRST-090215-174004");		

		//experimenter.tagPlainTextFiles("/Users/Risa/Documents/Hedging/Archive/CBTagger_fb_4_svmlite/data_2014/test",
				//"/Users/Risa/Documents/Hedging/Archive/CBTagger_fb_4_svmlite/CB_FIRST-090215-174004");
		//experimenter.tagPlainTextFiles(args[0],args[1]);

		//experimenter.runTestsOnly("quad_train1");
		//experimenter.createXMIFiles("data_2014/inline_annotation", "data_2014/uima");
		// this performs cross validation if we already have a model
		//experimenter.doExperiment();
		// this tags a raw file with the generated factbank, 4-tag model
		// experimenter.tagRawFile(sb.toString(), "C:\\Users\\Greg\\workspace\\CBShell\\Temp\\CB_SVMLIGHTFACTBANKCB_FIRST-260114-035420\\model5.jar");
		// this list all belief sequences for training set sentences
		//experimenter.collectBeliefSequences();
	}
}
