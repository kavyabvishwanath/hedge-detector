package org.ccls.nlp.cbt.utils;
/**

 * 
 * An outline class to use verbnet information. Currently it just gives back verbnet class 
 * when there is only one possibility. With more than one classes for a given lemma, 
 * finding the correct class is a complicated problem. It might be valuable, but 
 * kept for future research
 *  
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerbnetMapper {

	public static Hashtable <String,ArrayList<String>> verbnetHash;
	public static List<String> selectedFeatures;
	
	private static Pattern matchPattern = Pattern.compile("^([A-Za-z]+)-");
	
	public VerbnetMapper() {
		try {		
		BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("verbnet.txt"))));
		String strLine;
		verbnetHash = new Hashtable <String,ArrayList<String>>();
		
		while ((strLine = br.readLine()) != null)   {
			
			String verb = strLine.split(":")[0];
			String verbClass = strLine.split(":")[1];
			
			if (!verbnetHash.containsKey(verb))
				verbnetHash.put(verb, new ArrayList<String>());
			verbnetHash.get(verb).add(verbClass);
//			System.out.println(verb);
			
		}
		
		br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public List<String> getVerbNetClasses(String verb) {
		if (verbnetHash.containsKey(verb))
			return verbnetHash.get(verb);
		else
			return Collections.EMPTY_LIST;
	}
	
	public static int getLemmaMatch(List<String> verbnetMatches, String lemma) {
		for (int index = 0; index < verbnetMatches.size(); index++) {
			Matcher matcher = matchPattern.matcher(verbnetMatches.get(index));
			boolean result = matcher.find();
			if (result && matcher.group(1) != null) {
				return index;
			}
		}
		return -1;
	}
}
