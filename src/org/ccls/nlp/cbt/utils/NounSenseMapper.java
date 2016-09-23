package org.ccls.nlp.cbt.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NounSenseMapper {

	public static Map <String, ArrayList<String>> nounSenseMap;
	public static Map <String, ArrayList<String>> nounFullSenseMap;

	public NounSenseMapper() {
		try {		
			BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("sense_ontology.txt"))));
			String strLine;
			nounSenseMap = new HashMap <String,ArrayList<String>>();
			nounFullSenseMap = new HashMap <String,ArrayList<String>>();

			while ((strLine = br.readLine()) != null)   {

				String noun = strLine.split("\t")[0];
				String nounSenseList = strLine.split("\t")[1];
				List<String> nounSenses = Arrays.asList(nounSenseList.split(","));

				if (!nounSenseMap.containsKey(noun.split("\\@")[0])) {
					nounSenseMap.put(noun.split("\\@")[0], new ArrayList<String>());
				}				
				nounSenseMap.get(noun.split("\\@")[0]).addAll(nounSenses);

				String[] parts = noun.split("\\@");
				String word = parts[0];
				String sense = parts[1];
				String pos = parts[2];
				String wordnetString = word + "#" + pos + "#" + sense;
				if (!nounFullSenseMap.containsKey(wordnetString)) {
					nounFullSenseMap.put(wordnetString, new ArrayList<String>());
				}
				nounFullSenseMap.get(wordnetString).addAll(nounSenses);					
			}			
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> getNounSenseClasses(String noun) {
		if (nounSenseMap.containsKey(noun))
			return nounSenseMap.get(noun);
		else
			return Collections.emptyList();
	}

	public List<String> getNounFullSenseClasses(String noun) {
		if (nounFullSenseMap.containsKey(noun))
			return nounFullSenseMap.get(noun);
		else
			return Collections.emptyList();		
	}
}
