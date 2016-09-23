package org.ccls.nlp.cbt.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexUnitDecoder {

	private static final String LU_DIRECTORY = "fndata-1.5/lu";
	
	private String luDirectory;
	private Pattern p;
	
	private Map<String, Set<String>> frameLookupMap;
	
	public LexUnitDecoder() {
		luDirectory = LexUnitDecoder.LU_DIRECTORY;
		commonInit();
	}
	
	public LexUnitDecoder(String luDirectory) {
		this.luDirectory = luDirectory;
		commonInit();
	}
	
	private void commonInit() {
		p = Pattern.compile("<lexUnit.+name=\"([ 0-9A-Za-z\\/\\.\\_\\(\\)\\-\\']+)\".+frame=\"([A-Za-z\\_\\-]+)\".+>");
		
		frameLookupMap = new HashMap<String, Set<String>>();		
	}
	
	public void execute() throws IOException {
		String[] filenames = new File(luDirectory).list();
		boolean hasResult;
		for (String filename : filenames) {
			if (filename.endsWith(".xsl")) {
				continue;
			}
			hasResult = false;
			BufferedReader br = new BufferedReader(new FileReader(luDirectory + "/" + filename));
			while (br.ready()) {
				String line = br.readLine();
				Matcher matcher = p.matcher(line);
				boolean result = matcher.find();
				if (result) {
					hasResult = true;
					String lexicalUnit = matcher.group(1);
					String frame = matcher.group(2);
					if (!frameLookupMap.containsKey(lexicalUnit)) {
						frameLookupMap.put(lexicalUnit, new HashSet<String>());
					}
					frameLookupMap.get(lexicalUnit).add(frame);
				}
			}
			br.close();
			if (!hasResult) {
				System.err.println(filename + " did not seem to be parseable as you expected");
			}
		}
	}
	
	public Set<String> findStartsWithFramesForLexUnit(String lexUnit) {
		Set<String> toReturn = new HashSet<String>();
		for (String key : frameLookupMap.keySet()) {
			if (key.startsWith(lexUnit)) {
				toReturn.addAll(frameLookupMap.get(key));
			}
		}
		return toReturn;
	}
	
	public Set<String> findExactFramesForLexUnit(String lexUnit) {
		return frameLookupMap.get(lexUnit);
	}
	
	public Map<String, Set<String>> getFrameLookupMap() {
		return frameLookupMap;
	}
	
	public static void main(String[] args) throws IOException {
		String luDirectory = "/home/gwerner/Desktop/fndata-1.5/lu";
		LexUnitDecoder lud = new LexUnitDecoder(luDirectory);
		lud.execute();
		System.out.println(lud.findExactFramesForLexUnit("help.v"));
		System.out.println(lud.findStartsWithFramesForLexUnit("help"));
	}
	
}
