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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import com.google.common.io.Files;

public class CrossValidator {

	int numberOfFolds;	  
	public static int currentFold;	  
	public static double currentTrainingPosRatio;
	public FileWriter featuresFile;

	public CrossValidator(int folds) {
		numberOfFolds = folds;
	}

	// Checks the task type and call appropriate function
	//
	public void crossValidate(String type,String featureSet, String refId) throws Exception{

		if (type.equals("CB")) {
			crossValidateCB(featureSet,refId);
		}
		else {
			System.out.println("Task not recognized : " + type);
		}

	}

	// Actual cross validation. 
	public void crossValidateCB(String featureSet, String refId) throws Exception{
		//useCrossVal = true
		AnnotationPipeline annotator = new AnnotationPipeline("CB_" + refId, true);
		EvaluationLogger el;
		List<Map<Outcome,Map<String,String>>> outcomeTables = new ArrayList<Map<Outcome,Map<String,String>>>();
		CommittedBeliefAnnotator.featureSet = featureSet;
		for (int fold = 1; fold <= numberOfFolds; fold++) {

			System.out.println("Fold " + fold);		  

			annotator.runFeatureExtractorPipelineForCBCrossVal(fold);
			org.cleartk.classifier.jar.Train.main(AnnotationPipeline.DIR_NAME, "-t", "0");
			annotator.runCBClassifierPipelineForCrossVal(fold);  
			outcomeTables.add((Map<Outcome,Map<String,String>>) CommittedBeliefAnnotator.outcomeTable.clone());

			System.out.println(outcomeTables.get(fold - 1).size());
			
		}

		el = new EvaluationLogger(AnnotationPipeline.DIR_NAME);
		el.write(featureSet + "\n");			  
		el.write(Evaluator.getMicroAveragedPRFMultiClass(outcomeTables,"pred"));			  
		el.close();

	}
	
	public void renameJar(int fold) {
		File old = new File(AnnotationPipeline.DIR_NAME + "/model.jar");
		File ren = new File(AnnotationPipeline.DIR_NAME + "/model" + fold + ".jar");			
		try {
			Files.move(old, ren);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
	
	public void copyJar() throws IOException {
		JarFile jarFile = new JarFile(new File(AnnotationPipeline.DIR_NAME + "/model.jar"));
		File destDir = new File(AnnotationPipeline.DIR_NAME + "/CB");
		String fileName = jarFile.getName();
		String fileNameLastPart = fileName.substring(fileName.lastIndexOf(File.separator));
		File destFile = new File(destDir, fileNameLastPart);
		JarOutputStream jos = new JarOutputStream(new FileOutputStream(destFile));
		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			InputStream is = jarFile.getInputStream(entry);
			jos.putNextEntry(new JarEntry(entry.getName()));
			byte[] buffer = new byte[4096];
			int bytesRead = 0;
			while ((bytesRead = is.read(buffer)) != -1) {
				jos.write(buffer, 0, bytesRead);
			}
			is.close();
			jos.flush();
			jos.closeEntry();
		}
		jos.close();
		jarFile.close();
	}
	
}