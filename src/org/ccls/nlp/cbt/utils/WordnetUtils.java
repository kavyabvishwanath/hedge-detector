package org.ccls.nlp.cbt.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;

public final class WordnetUtils {

	public static IDictionary dict;
	
	static {
		String wnhome = System.getenv("WNHOME");
		String path = wnhome + File.separator + "dict";
		URL url;
		try {
			url = new URL("file", null, path);
			dict = new Dictionary(url);
			dict.open();
		} catch (Exception e) {
		}
	}
		
	public static void disposeWordNet() {
		try {
			dict.close();
		} catch (Exception e) {			
		}
	}
	
	public static String getWordnetPos(String pos) {
		if (pos.startsWith("N")) {
			return "n";
		} else if (pos.startsWith("V")) {
			return "v";
		} else if (pos.startsWith("J")) {
			return "a";
		} else if (pos.length() >= 2 && pos.startsWith("RB")) {
			return "r";
		}
		return "uk";
	}
	
	public static int getSenseNumber(String lemma, String pos) {
		if (dict == null || !dict.isOpen()) return 0;
		POS posObject = null;
		if (pos.charAt(0) == 'N') {
			posObject = POS.NOUN;
		} else if (pos.charAt(0) == 'V') {
			posObject = POS.VERB;
		} else if (pos.charAt(0) == 'J') {
			posObject = POS.ADJECTIVE;
		} else if (pos.length() >= 2 && pos.substring(0,2).equals("RB")) {
			posObject = POS.ADVERB;
		}
		if (posObject == null) {
			return 0;
		}
		IIndexWord idxWord = dict.getIndexWord(lemma, posObject);
		if (idxWord == null) {
			return 0;
		}
		return idxWord.getTagSenseCount();		
	}
	
	public static int getSenseNumber(String lemma) {
		if (dict == null || !dict.isOpen()) return -1;		
		POS[] availablePos = {POS.ADJECTIVE, POS.ADVERB, POS.NOUN, POS.VERB};
		int count = 0;
		for (POS pos : availablePos) {
		   IIndexWord idxWord = dict.getIndexWord(lemma, pos);
		   if (idxWord != null) {
		      count+= idxWord.getTagSenseCount();		
		   }
		}
		return count;
	}
	
}
