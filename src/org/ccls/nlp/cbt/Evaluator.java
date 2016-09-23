package org.ccls.nlp.cbt;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ccls.nlp.cbt.utils.ConfusionMatrix;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;

public class Evaluator {
	
	  public static void printEvalInstance(Instance<Boolean> instance, Boolean gold, Boolean pred) {
		   String out = gold + "\t" + pred;
		   
		   for (Feature feat : instance.getFeatures()) {
			   out += " " + feat.getName() + ":" + feat.getValue();
		   }
		   System.out.println(out);
	   }	      
	  
	  public static String getMicroAveragedPRF( ArrayList<Map<Outcome,Map<String,Boolean>>> outcomeTables,String predTag) {
		  long tp = 0, fp = 0, fn = 0;

		  long totalInstCount = 0;
		  
		  for (Map<Outcome,Map<String,Boolean>> outcomeTable : outcomeTables) {
			  totalInstCount += outcomeTable.size();
			  for (Outcome outcome : outcomeTable.keySet()) {
				  boolean actual = outcomeTable.get(outcome).get("actual");
				  boolean pred = outcomeTable.get(outcome).get(predTag);
				  
				  if (actual && pred) {
					  tp++;
				  }
				  if (actual && !pred) {
					  fn++;
				  }
				  if (!actual && pred) {
					  fp++;
				  }
			  }
		  }
		  
		  double precision, recall, fmeasure, accuracy;
		  if (tp>0) {
				precision = 100.0*tp/(tp+fp);
				recall = 100.0*tp/(tp+fn);
				fmeasure = 2*precision*recall/(precision + recall);
				accuracy = (totalInstCount - fp - fn)*100.0/totalInstCount;
		  }
		  else {
				precision = 0.0;
				recall = 0.0;
				fmeasure = 0.0;		
				accuracy = (totalInstCount - fp - fn)*100.0/totalInstCount;
		  }
		  
		  double baselineAcc = (1.0*(tp+fn)/totalInstCount >= 0.5) ? 100.0*(tp+fn)/totalInstCount : 100 - 100.0*(tp+fn)/totalInstCount;
		  		  
		  return String.format("%.2f(%.2f)\t%.2f\t%.2f\t%.2f", accuracy, baselineAcc, precision , recall, fmeasure);  
	  }  
	  
	  public static String getMicroAveragedPRFMultiClass( List<Map<Outcome,Map<String,String>>> outcomeTables,String predTag) {
		
		  Map<String,Long> tp = new HashMap<String,Long>();
		  Map<String,Long> fp = new HashMap<String,Long>();
		  Map<String,Long> fn = new HashMap<String,Long>();
		
		  long totalInstCount = 0;
		  long correctInstCount = 0;

		  ConfusionMatrix cm = new ConfusionMatrix();
		  
		  for (Map<Outcome,Map<String,String>> outcomeTable : outcomeTables) {
			  totalInstCount += outcomeTable.size();
			  for (Outcome outcome : outcomeTable.keySet()) {
				  String actual = outcomeTable.get(outcome).get("actual");
				  String pred = outcomeTable.get(outcome).get(predTag);
				  cm.addEntry(actual, pred);

				  if (!tp.containsKey(actual)) {
						tp.put(actual, new Long(0));
						fp.put(actual, new Long(0));
						fn.put(actual, new Long(0));				
					}
					if (!tp.containsKey(pred)) {
						tp.put(pred, new Long(0));
						fp.put(pred, new Long(0));
						fn.put(pred, new Long(0));				
					}
					if(actual.equals(pred)) {
						correctInstCount ++;
						tp.put(pred, tp.get(pred)+1) ;
					}
					else {
						fn.put(actual, fn.get(actual)+1) ;
						fp.put(pred, fp.get(pred)+1) ;
					}
			  }
		  }
		  
		  cm.toConsole();
		  
		  List<Map.Entry<String,Long>> list = new ArrayList<Map.Entry<String,Long>>(tp.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<String,Long>>() {
			            public int compare(Map.Entry<String,Long> e1, Map.Entry<String,Long> e2) {
			                String s1 = e1.getKey();
			                String s2 = e2.getKey();		                
			            	return s2.compareTo(s1);
			            }
			});
			
			String targetC;
			double precision, recall, fmeasure;
			long max = 0;
			String maxClass = "";
			long instCount = 0, totInstCount = 0;
			
			long totalTp=0, totalFp=0, totalFn=0;
			
			String out = "";
			
			for (Map.Entry<String,Long> entry : list) {
				
				targetC = entry.getKey();
				
				instCount = tp.get(targetC) + fn.get(targetC);
//				System.out.println(targetC + " tp: " + tp.get(targetC));
//				System.out.println(targetC + " fn: " + fn.get(targetC));
//				System.out.println(targetC + " fp: " + fp.get(targetC));
//				System.out.println();
				
				totInstCount += instCount; 
				
				if (targetC.equals("O"))
					continue;
				
				if (max < instCount) {
					max = instCount;
					maxClass = targetC;
				}
				
				if (true) {
					totalTp += tp.get(targetC);
					totalFp += fp.get(targetC);
					totalFn += fn.get(targetC);
				}
				
				if (tp.get(targetC)>0) {
					precision = 100.0*tp.get(targetC)/(tp.get(targetC)+fp.get(targetC));
					recall = 100.0*tp.get(targetC)/(tp.get(targetC)+fn.get(targetC));
					fmeasure = 2*precision*recall/(precision + recall);
				}
				else {
					precision = 0.0;
					recall = 0.0;
					fmeasure = 0.0;				
				}
				
				System.out.format("%s (%d) : \t\t%.2f\t%.2f\t%.2f%n", targetC , instCount, precision , recall , fmeasure);
				
				out += String.format("%s (%d) : \t\t%.2f\t%.2f\t%.2f%n", targetC , instCount, precision , recall , fmeasure);
				
				//logger.write(String.format("%d\t%.2f\t%.2f\t%.2f\t", instCount, precision , recall , fmeasure));
			}
			if (totalTp>0) {
				precision = 100.0*totalTp/(totalTp+totalFp);
				recall = 100.0*totalTp/(totalTp+totalFn);
				fmeasure = 2*precision*recall/(precision + recall);
			}
			else {
				precision = 0.0;
				recall = 0.0;
				fmeasure = 0.0;				
			}
			
			System.out.format("%s (%d) : %.2f\t%.2f\t%.2f%n", "OverallMicroAvg" , totInstCount, precision , recall , fmeasure);
			//logger.write(String.format("%.2f\t%.2f\t%.2f\t", precision , recall , fmeasure));
			
			out = String.format("%s (%d) : %.2f\t%.2f\t%.2f%n", "OverallMicroAvg" , totInstCount, precision , recall , fmeasure) + out;
			
			System.out.println("Accuracy : " + (correctInstCount*100.0/totalInstCount));
		
			return out;
	  }  

	  public static String getSignificanceFromART(String dirName) {
		  
	        String[] command = new String[6];
	        command[0] = "python";
	        command[1] = "/Users/vinodkpg/Dropbox/Projects/Utils/ART/art.py";
	        command[2] = "-c";
	        command[3] = dirName+"/actual.txt";
	        command[4] = dirName+"/alwaystrue.txt";
	        command[5] = dirName+"/pred.txt";
	        
			return StringUtils.join(command," ");
	  }

	  public static void printMicroAveragedPRF(List<Map<Outcome,Map<String,Boolean>>> outcomeTables) {
		  long tp = 0, fp = 0, fn = 0;

		  long totalInstCount = 0;
		  
		  for (Map<Outcome,Map<String,Boolean>> outcomeTable : outcomeTables) {
			  totalInstCount += outcomeTable.size();
			  for (Outcome outcome : outcomeTable.keySet()) {
				  boolean actual = outcomeTable.get(outcome).get("actual");
				  boolean pred = outcomeTable.get(outcome).get("pred");
				  
				  if (actual && pred) {
					  tp++;
				  }
				  if (actual && !pred) {
					  fn++;
				  }
				  if (!actual && pred) {
					  fp++;
				  }
			  }
		  }
		  
		  double precision, recall, fmeasure, accuracy;
		  if (tp>0) {
				precision = 100.0*tp/(tp+fp);
				recall = 100.0*tp/(tp+fn);
				fmeasure = 2*precision*recall/(precision + recall);
				accuracy = (totalInstCount - fp - fn)*100.0/totalInstCount;
		  }
		  else {
				precision = 0.0;
				recall = 0.0;
				fmeasure = 0.0;		
				accuracy = (totalInstCount - fp - fn)*100.0/totalInstCount;
		  }
		  
		  double baselineAcc = (1.0*(tp+fn)/totalInstCount >= 0.5) ? 100.0*(tp+fn)/totalInstCount : 100 - 100.0*(tp+fn)/totalInstCount;
		  
		  System.out.format("%.2f(%.2f)\t%.2f\t%.2f\t%.2f%n", accuracy, baselineAcc, precision , recall, fmeasure);  		  
		  
	  }
	  
	  public static void printPRF (Map<Outcome,Map<String,Boolean>> outcomeTable, String test) {
		  
		  long tp = 0, fp = 0, fn = 0;

		  for (Outcome outcome : outcomeTable.keySet()) {
			  boolean actual = outcomeTable.get(outcome).get("actual");
			  boolean pred = outcomeTable.get(outcome).get(test);
			  
			  if (actual && pred) {
				  tp++;
			  }
			  if (actual && !pred) {
				  fn++;
			  }
			  if (!actual && pred) {
				  fp++;
			  }
		  }
		  
		  double precision, recall, fmeasure, accuracy;
		  if (tp>0) {
				precision = 100.0*tp/(tp+fp);
				recall = 100.0*tp/(tp+fn);
				fmeasure = 2*precision*recall/(precision + recall);
				accuracy = (outcomeTable.size() - fp - fn)*100.0/outcomeTable.size();
		  }
		  else {
				precision = 0.0;
				recall = 0.0;
				fmeasure = 0.0;		
				accuracy = (outcomeTable.size() - fp - fn)*100.0/outcomeTable.size();
		  }
		  
		  double baselineAcc = (1.0*(tp+fn)/outcomeTable.size() >= 0.5) ? 100.0*(tp+fn)/outcomeTable.size() : 100 - 100.0*(tp+fn)/outcomeTable.size();
		  
		  System.out.format("%.2f(%.2f)\t%.2f\t%.2f\t%.2f%n", accuracy, baselineAcc, precision , recall, fmeasure);  		  
	  }
	
	  public static void printPRF(ArrayList<Boolean> actuals, ArrayList<Boolean> preds) {
		 		  
		  long tp = 0, fp = 0, fn = 0;
		  
		  for (int i = 0; i<actuals.size(); i++) {
			  if (actuals.get(i) && preds.get(i)) {
				  tp++;
			  }
			  if (actuals.get(i) && !preds.get(i)) {
				  fn++;
			  }
			  if (!actuals.get(i) && preds.get(i)) {
				  fp++;
			  }
		  }
		  
		  double precision, recall, fmeasure, accuracy;
		  if (tp>0) {
				precision = 100.0*tp/(tp+fp);
				recall = 100.0*tp/(tp+fn);
				fmeasure = 2*precision*recall/(precision + recall);
				accuracy = (actuals.size() - fp - fn)*100.0/actuals.size();
		  }
		  else {
				precision = 0.0;
				recall = 0.0;
				fmeasure = 0.0;		
				accuracy = (actuals.size() - fp - fn)*100.0/actuals.size();
		  }
		  
		  double baselineAcc = (1.0*(tp+fn)/actuals.size() >= 0.5) ? 100.0*(tp+fn)/actuals.size() : 100 - 100.0*(tp+fn)/actuals.size();
		  
		  System.out.format("%.2f(%.2f)\t%.2f\t%.2f\t%.2f%n", accuracy, baselineAcc, precision , recall, fmeasure);  
		  
	  }
	  
}