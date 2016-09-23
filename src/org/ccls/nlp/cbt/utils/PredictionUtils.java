package org.ccls.nlp.cbt.utils;

import java.util.List;

public final class PredictionUtils {

	public static String getCascadingMinorityTag(String label, List<String> acceptableTags) {
		String prediction = label;

		if (label.equals("O") || acceptableTags.contains("O")) {
			prediction = "O";
		}
		if (label.equals("CB") || acceptableTags.contains("CB")) {
			prediction = "CB";
		}
		if (label.equals("RB") || acceptableTags.contains("RB")) {
			prediction = "RB";
		}
		if (label.equals("NA") || acceptableTags.contains("NA")) {
			prediction = "NA";
		} 
		if (label.equals("NCB") || acceptableTags.contains("NCB")) {
			prediction = "NCB";
		}
		
		return prediction;
	}
	
	public static String getMinorityTag(String label, List<String> acceptableTags) {
		String prediction = label;
		
		if (acceptableTags.contains("NCB")) {
			prediction = "NCB";
		}
		
		return prediction;		
	}

	public static String getCascadingMajorityTag(String label, List<String> acceptableTags) {
		if (acceptableTags.size() > 0) {
			System.out.println(acceptableTags);
		}
		if (acceptableTags.contains("O")) {
			return "O";
		}		
		if (acceptableTags.contains("CB")) {
			return "CB";
		}		
		if (acceptableTags.contains("RB")) {
			return "RB";
		}
		if (acceptableTags.contains("NA")) {
			return "NA";
		}
		if (acceptableTags.contains("NCB")) {
			return "NCB";
		}
		
		return label;
	}
	
	public static String getLabel(String label, List<String> acceptableTags) {
		return label;
	}

}
