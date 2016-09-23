package org.ccls.nlp.cbt;

/**

 * 
 * This class is used log the evaluation results in a repository specified by loggerFile
 *  
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EvaluationLogger {

	String loggerFile = "results/evaluationLog.txt";

	String learnerDir;
	
	FileWriter logger = null;
	
	List<String> features = new ArrayList<String>();
	
	public EvaluationLogger(String learnerDir) {
		
		this.learnerDir = learnerDir;
		
		String strLine;		
		try {			
			logger = new FileWriter(new File(loggerFile),true);		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getLibSVMFeatureList(String featureListFile) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(featureListFile))));
		String strLine;
		ArrayList<String> features =  new ArrayList<String>();
		while ((strLine = br.readLine()) != null)   {				
			features.add(strLine.split(" ")[1].substring(0, strLine.split(" ")[1].lastIndexOf("_")));				
		}
		br.close();
		return features;
	}
	
	public List<String> getMaxentFeatureList(String featureFile) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(featureFile))));
		String strLine;
		ArrayList<String> features =  new ArrayList<String>();
		while ((strLine = br.readLine()) != null)   {
			for (String feat : strLine.split(" ")) {
				if (feat.contains("="))
					feat = feat.substring(0, feat.lastIndexOf("="));
				features.add(feat);								
			}
		}
		Set<String> featureSet = new HashSet<String>(features);
		featureSet.remove("true");
		featureSet.remove("false");
		List featureList = new ArrayList(featureSet); 
		Collections.sort(featureList);
		br.close();
		return featureList;
	}

	public void write(String text) {
		try {
			logger.write(text);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void close() {
		try {
			//logger.write("\t");
			//logger.write(StringUtils.join(features,","));
			//logger.write("\n");
			logger.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		EvaluationLogger el = new EvaluationLogger("");
	}
}
