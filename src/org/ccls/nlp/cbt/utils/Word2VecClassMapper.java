package org.ccls.nlp.cbt.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Word2VecClassMapper {

	private static final String classFile = "word2vec/classes.sorted.txt";

	private Map<String, String> mapper;

	public Word2VecClassMapper() {
		mapper = new HashMap<String, String>();
		readInFile();
	}	

	private void readInFile() {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(classFile));
		while (br.ready()) {
			String[] parts = br.readLine().split(" ");
			mapper.put(parts[0], parts[1]);
		}
		br.close();
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
			System.exit(1);
		}
	}

    public String getLemmaClass(String lemma) {
       if (mapper.containsKey(lemma)) {
    	   return mapper.get(lemma);
       } else {
    	   return "-1";
       }
    }
}
