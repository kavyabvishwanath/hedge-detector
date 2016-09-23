package org.ccls.nlp.cbt;

/**
 * 
 * This class is the main driver class for cross validation. For each fold, it runs the 
 * training phase by calling FeatureExtractorPipeline (This is a misnomer - it does feature 
 * extraction and writing training data) and 
 * testing phase by calling ClassifierPipeline
 * It then calls the EvaluationLogger to log the Precision Recall and F measure.
 *  
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class TrainAndTest {

	public FileWriter featuresFile;

	// Checks the task type and call appropriate function
	public void trainAndTest(String type,String featureSet, String refId, String trainPath, String testPath) throws Exception{
		if (type.equals("CB")) {
			doTrainAndTest(featureSet,refId, trainPath, testPath);
		}
		else {
			System.out.println("Task not recognized : " + type);
		}
	}

	public void doTrainAndTest(String featureSet, String refId, String trainPath, String testPath) throws Exception{
		System.out.println("Inside doTrainAndTest");
//		for(Integer degree=2; degree<=3; degree++){
			AnnotationPipeline annotator = new AnnotationPipeline("CB_" + refId, false);
			EvaluationLogger el;
			List<Map<Outcome,Map<String,String>>> outcomeTables = new ArrayList<Map<Outcome,Map<String,String>>>();
			CommittedBeliefTrainAndTestAnnotator.featureSet = featureSet;
			featuresFile = new FileWriter(new File(AnnotationPipeline.DIR_NAME  + "/feature_set.txt" ), true);
			featuresFile.write(featureSet + "\n");
			featuresFile.close();
			
			annotator.runFeatureExtractorPipelineForCBTraining(trainPath);//envoke the train process
			System.out.println("CrossValidator.trainAndTest(): start org.cleartk.classifier.jar.Train.main");
			//System.out.println("DIR_NAME: " + AnnotationPipeline.DIR_NAME);
			//linear
//			org.cleartk.classifier.jar.Train.main(AnnotationPipeline.DIR_NAME,"-t", "0");
			//linear with large cache
			org.cleartk.classifier.jar.Train.main(AnnotationPipeline.DIR_NAME,"-t", "0","-m","200");
			//quadratic, d is degree of polynomial
//			org.cleartk.classifier.jar.Train.main(AnnotationPipeline.DIR_NAME,"-t", "1","-d","2");
			
			System.out.println("CrossValidator.trainAndTest(): run Classifier");
			annotator.runCBClassifierPipelineForTesting(testPath);// test process
			outcomeTables.add((Map<Outcome, Map<String, String>>) CommittedBeliefTrainAndTestAnnotator.outcomeTable.clone());

			System.out.println(outcomeTables.get(0).size());
			
			el = new EvaluationLogger(AnnotationPipeline.DIR_NAME);
			el.write(featureSet + "\n");			
			el.write("Polynomial kernel, degree 2 " + ":\n");
			el.write(Evaluator.getMicroAveragedPRFMultiClass(outcomeTables,"pred"));			  
			el.close();
			
			//write out feature weights if you want to analyse most useful features
//			for(int i=0;i<=5; i++){
//				rankFeatures(AnnotationPipeline.DIR_NAME,i);
//			}
			
//		}
	}
	
	public void onlyTest(String featureSet, String modelfilepath) throws Exception{
		System.out.println("Inside onlyTest");
		
		AnnotationPipeline.DIR_NAME = modelfilepath;
		String modelfile = modelfilepath + "/model.jar";
		
		EvaluationLogger el;
		List<Map<Outcome,Map<String,String>>> outcomeTables = new ArrayList<Map<Outcome,Map<String,String>>>();
		CommittedBeliefTrainAndTestAnnotator.featureSet = featureSet;
		
		AnnotationPipeline.runCBClassifierPipelineWithSavedModel(modelfile);
		outcomeTables.add((Map<Outcome, Map<String, String>>) CommittedBeliefTrainAndTestAnnotator.outcomeTable.clone());

		System.out.println(outcomeTables.get(0).size());
		
		el = new EvaluationLogger(AnnotationPipeline.DIR_NAME);
		el.write(featureSet + "\n");			  
		el.write(Evaluator.getMicroAveragedPRFMultiClass(outcomeTables,"pred"));			  
		el.close();
		
		//write out feature weights if you want to analyse most useful features
//		for(int i=1;i<=5; i++){
//			rankFeatures(AnnotationPipeline.DIR_NAME,i);
//		}

	}
	
	public static void tagTextFiles(String indir, String modelfilepath, String featureSet) throws Exception{
		System.out.println("Inside CrossValidator.tagTextFiles()");
		CommittedBeliefTrainAndTestAnnotator.featureSet = featureSet;
		AnnotationPipeline.DIR_NAME = modelfilepath;
		String modelfile = modelfilepath + "/model.jar";
		String outdirPath = indir + "/output";
		File outdir = new File(outdirPath);
		outdir.delete();
		String DIR_NAME = "";
		if (outdir.mkdirs()) {
			DIR_NAME = outdir.getAbsolutePath();
			(new File(DIR_NAME)).mkdir();
			System.out.println("Created new temp Directory : " + DIR_NAME);
		}
		else {
			System.out.println("Cannot create temp directory! " + outdirPath);
			System.exit(0);
		}
		AnnotationPipeline.runCBTaggerOnPlainSentences(indir, modelfile, DIR_NAME);
	}



	
	public static void rankFeatures(String dir, int index) {
		
		String inline;
		Hashtable<String,String> featMap = new Hashtable<String,String>();
		Hashtable<String,Double> featWeightMap = new Hashtable<String,Double>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dir+"/features-lookup.txt"));
			while ((inline = reader.readLine())!=null) {
				featMap.put(inline.split(" ")[0],inline.split(" ")[1]);
			}
			reader.close();
			reader = new BufferedReader(new FileReader(dir+"/training-data-"+ index +".svmlight.model"));
//			FileWriter writer = new FileWriter(new File(dir+"/"+CandidateRankerAnnotator.featureSet.replaceAll(",", "_")+"_featureWeights.txt"));
			FileWriter writer = new FileWriter(new File(dir+"/featureWeights_" + index + ".txt"));
			while ((inline = reader.readLine())!=null) {
				
				if (!inline.contains(":"))
					continue;
				
				String[] feats = inline.split(" ");
				double alpha,value;
				String feat;
				alpha = Double.parseDouble(feats[0].trim());
				
				for (int i=0; i< feats.length; i++) {
					if (i<1) {
						continue;
					}
					if (feats[i].trim().equals("#"))
						continue;
					
					feat = feats[i].split(":")[0];
					value = Double.parseDouble(feats[i].split(":")[1]);
					
					if(!featWeightMap.containsKey(feat))
						featWeightMap.put(feat,0.0);
					featWeightMap.put(feat, featWeightMap.get(feat) + value*alpha);
//					featWeightMap.put(feats[i].split(":")[0], feats[i].split(":")[1]);
//					writer.write(feats[i].replaceAll(":", "\t")+"\t"+featMap.get(feats[i].split(":")[0])+"\n");
					
				}
			}
			
			reader.close();
			ArrayList<Map.Entry<String, Double>> myArrayList=new ArrayList<Map.Entry<String, Double>>(featWeightMap.entrySet());
			
			Collections.sort(myArrayList, new Comparator<Map.Entry<String, Double>>(){
				public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2){

					int result=0;
					
					//Sort based on values.
					Double value1 = e1.getValue();
					Double value2 = e2.getValue();

					//Sort values in a descending order
					result=value2.compareTo( value1 );

					return result;
				}
			});
			
			for(Map.Entry<String, Double> e:myArrayList){
				writer.write(e.getKey()+"\t"+e.getValue()+"\t"+featMap.get(e.getKey())+"\n");
//				writer.write(feats[i].replaceAll(":", "\t")+"\t"+featMap.get(feats[i].split(":")[0])+"\n");
			}
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
}