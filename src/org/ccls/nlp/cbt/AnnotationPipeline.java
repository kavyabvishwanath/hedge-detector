package org.ccls.nlp.cbt;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.examples.cpe.FileSystemCollectionReader;
import org.apache.uima.examples.xmi.XmiCollectionReader;
import org.ccls.nlp.cbt.CollectionReaders.ERECorpusCollectionReader;
import org.ccls.nlp.cbt.CollectionReaders.LUCorpusCollectionReader;
import org.ccls.nlp.cleartk.tweakers.StanfordCoreNLPAnnotator;
import org.ccls.nlp.cleartk.tweakers.StanfordCoreNLPSentenceSplitter;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.svmlight.SVMlightStringOutcomeDataWriter;
import org.cleartk.syntax.opennlp.ParserAnnotator;
import org.cleartk.syntax.opennlp.PosTaggerAnnotator;
import org.cleartk.token.TokenComponents;
import org.cleartk.token.lemma.choi.LemmaAnnotator;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * 
 * This class serves as a factory for various UIMA pipelines. 
 * It includes pipelines for training set writing, pipelines for feature extraction,
 * pipelines for various annotations, pipelines for classification and so on.
 *  
 */

public class AnnotationPipeline {

	static AnalysisEngineDescription annotationWriter,
	posTaggerAnnotator,
	powerTrainingSetWriter,
	powerMessageLevelTrainingSetWriter,
	lemmaAnnotator,
	stemmerAnnotator,
	maltParser,
	ptbParser,
	cbTrainingSetWriter,
	cbClassifier,
	clearParser;
	
	public static String DIR_NAME = "";

	public AnnotationPipeline(String refId, boolean useCrossVal) throws Exception{

		if (DIR_NAME.length() > 0) {
			DIR_NAME = "";
		}
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyy-HHmmss");

		File BASE_DIR = new File(DIR_NAME + refId + "-" + simpleDateFormat.format(new Date()));
//		File BASE_DIR = new File(DIR_NAME + refId + "_linear");
		//first delete all files in the directory if it exists
		if(BASE_DIR.exists()){
			String[]entries = BASE_DIR.list();
			for(String s: entries){
			    File currentFile = new File(BASE_DIR.getPath(),s);
			    currentFile.delete();
			}
		}
		//now delete the directory itself
		BASE_DIR.delete();

		if (BASE_DIR.mkdirs()) {
			DIR_NAME = BASE_DIR.getAbsolutePath();
			(new File(DIR_NAME)).mkdir();
			System.out.println("Created new temp Directory : " + DIR_NAME);
		}
		else {
			System.out.println("Cannot create temp directory!");
			System.exit(0);
		}

		posTaggerAnnotator = PosTaggerAnnotator.getDescription();
		ptbParser = ParserAnnotator.getDescription();
		lemmaAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
				LemmaAnnotator.class,
				TokenComponents.TYPE_SYSTEM_DESCRIPTION,
				LemmaAnnotator.PARAM_LEMMATIZER_DATA_FILE_NAME,
				"lib/wordnet-3.0-lemma-data.jar"
				);
		stemmerAnnotator = DefaultSnowballStemmer.getDescription("English");
		if(useCrossVal){
			cbClassifier = CommittedBeliefAnnotator.getClassifierDescription(DIR_NAME + "/model.jar");
			cbTrainingSetWriter = AnalysisEngineFactory.createPrimitiveDescription(
					CommittedBeliefAnnotator.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
					DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
					DIR_NAME,
					DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
					SVMlightStringOutcomeDataWriter.class.getName()
					);
		}else{
			cbClassifier = CommittedBeliefTrainAndTestAnnotator.getClassifierDescription(DIR_NAME + "/model.jar");
			cbTrainingSetWriter = AnalysisEngineFactory.createPrimitiveDescription(
					CommittedBeliefTrainAndTestAnnotator.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
					DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
					DIR_NAME,
					DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
					SVMlightStringOutcomeDataWriter.class.getName()
					);
		}
		
	}

	public void runFeatureExtractorPipelineForCBCrossVal(int fold) throws Exception {
		System.out.println("Inside AnnotationPipeline.runFeatureExtractorPipelineForCBfromSaved()");
		CommittedBeliefAnnotator.currentFold = fold;
		CollectionReader xmiReader =  CollectionReaderFactory.createCollectionReader(
				XmiCollectionReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
				XmiCollectionReader.PARAM_INPUTDIR,
				"ldc_data/uima/train" 
				);		
		System.out.println("AnnotationPipeline.runFeatureExtractorPipelineForCBfromSaved(): runPipeline");
		SimplePipeline.runPipeline(    		
				xmiReader,
				cbTrainingSetWriter
				);
	}

	public void runCBClassifierPipelineForCrossVal(int fold) throws Exception {
		System.out.println("Inside AnnotationPipeline.runCBClassifierPipelineFromSaved()");
		CommittedBeliefAnnotator.currentFold = fold;
		CollectionReader xmiReader =  CollectionReaderFactory.createCollectionReader(
				XmiCollectionReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
				XmiCollectionReader.PARAM_INPUTDIR,
				"ldc_data/uima/train" 
				);
		
		SimplePipeline.runPipeline(    		
				xmiReader,
				cbClassifier
				);
	}
	
	public void runFeatureExtractorPipelineForCBTraining(String trainPath) throws Exception {
		CollectionReader xmiReader =  CollectionReaderFactory.createCollectionReader(
				XmiCollectionReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
				XmiCollectionReader.PARAM_INPUTDIR,
				trainPath
				);		
		System.out.println("AnnotationPipeline.runFeatureExtractorPipelineForCBfromSaved(): runPipeline");
		SimplePipeline.runPipeline(    		
				xmiReader,
				cbTrainingSetWriter
				);
	}

	public void runCBClassifierPipelineForTesting(String testPath) throws Exception {
		CollectionReader xmiReader =  CollectionReaderFactory.createCollectionReader(
				XmiCollectionReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
				XmiCollectionReader.PARAM_INPUTDIR,
				testPath
				);
		
		SimplePipeline.runPipeline(    		
				xmiReader,
				cbClassifier
				);
	}
	
	public static void runFeatureExtractorPipelineSplitAndSave(String indir, String outdir) throws Exception {
		SimplePipeline.runPipeline(
				LUCorpusCollectionReader.getCollectionReader(indir),
				StanfordCoreNLPSentenceSplitter.getDescription(),
				//MaltParser.getDescription("models/engmalt.linear-1.7.mco"),
				//ClearParser.getDescription(),
				//FeatureAnnotator.getDescription(),
				AnnotationWriterTxt.getWriterDescription(outdir)
				);
	}
	
	public static void runFeatureExtractorPipelineParseAndSave(String indir, String outdir) throws Exception {
		SimplePipeline.runPipeline(
				LUCorpusCollectionReader.getCollectionReader(indir),
				StanfordCoreNLPAnnotator.getDescription(),
				//MaltParser.getDescription("models/engmalt.linear-1.7.mco"),
				//ClearParser.getDescription(),
				//FeatureAnnotator.getDescription(),
				AnnotationWriterAll.getWriterDescription(outdir)
				);
	}	
	
	public static void runBeliefAndEntityExtractionPipeline(String beliefPath, String erePath, String model) throws Exception {
		System.out.println("AnnotationPipeline.runBeliefAndEntityExtractionPipeline()");
		String combinedPath = beliefPath + ":" + erePath;
		SimplePipeline.runPipeline(
				ERECorpusCollectionReader.getCollectionReader(combinedPath), 
				StanfordCoreNLPAnnotator.getDescription(),
				EntityAnnotator.getClassifierDescription(model)
				);
	}	

//	public void runCBTaggerOnPlainSentences(String infile, String modelfile) throws Exception {
//		System.err.println("Not implemented yet");
//	}	 
	
	public static void runCBTaggerOnPlainSentences(String indir, String modelfile, String outdir) throws Exception {
			System.out.println("Inside AnnotationPipeline.runCBTaggerOnPlainSentences()");
		  CollectionReader fileSystemReader =  CollectionReaderFactory.createCollectionReader(
					  	FileSystemCollectionReader.class,
					  	TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
					  	FileSystemCollectionReader.PARAM_INPUTDIR,
			    		indir 
			    		);

			  SimplePipeline.runPipeline(    		
				  	fileSystemReader,
					StanfordCoreNLPAnnotator.getDescription(),	
					CommittedBeliefTrainAndTestAnnotator.getClassifierDescription(modelfile),
				    CBTagsWriter.getWriterDescription(outdir)
		    		);

	  }
	
	public static void runCBClassifierPipelineWithSavedModel(String modelfile) throws Exception {
		System.out.println("Inside AnnotationPipeline.runCBClassifierPipelineFromSaved()");
		CollectionReader xmiReader =  CollectionReaderFactory.createCollectionReader(
				XmiCollectionReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem"),
				XmiCollectionReader.PARAM_INPUTDIR,
				"ldc_data/uima/eval_data_noxml"
				);
		
		SimplePipeline.runPipeline(    		
				xmiReader,
				CommittedBeliefTrainAndTestAnnotator.getClassifierDescription(modelfile)
				);
	}

}
