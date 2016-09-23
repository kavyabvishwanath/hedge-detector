package org.ccls.nlp.cbt;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class WeiweiAnalysis {

	public WeiweiAnalysis() {
		
	}
	
	public static void main(String[] args) throws IOException {
		String dir = "weiwei";
		String[] filenames = new File(dir).list();
		int miss = 0;
		int total = 0;
		int files_seen = 0;
		for (String filename : filenames) {
			if (!filename.contains("temp_out")) {
				continue;
			}
			files_seen++;
			BufferedReader br = new BufferedReader(new FileReader(dir + "/" + filename));
			while (br.ready()) {
				String line = br.readLine();
				if (line.length() == 0) {
					miss++;
				}
				total++;
			}
			br.close();
		}
		System.out.println("Files seen: " + files_seen);
		System.out.println("Raw total: " + total);
		System.out.println("Raw miss: " + miss);
	}
	
}
