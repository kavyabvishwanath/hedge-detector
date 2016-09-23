package org.ccls.nlp.cbt.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;

public final class FileUtils {

	public static String getFilename(JCas jCas) {
		FSIterator  it = jCas.getAnnotationIndex(SourceDocumentInformation.type).iterator();
		String filename = null;
		if (it.hasNext()) {
			SourceDocumentInformation sdInfo = (SourceDocumentInformation) it.next();
			filename = sdInfo.getUri();
		}
		return filename;
	}
	
	public static String getDocumentPrefixFilename(String filename) {
		String[] pathParts = filename.split("\\\\");
		String[] filenameParts = pathParts[pathParts.length - 1].split("\\.");
		StringBuilder prefix = new StringBuilder("");
		for (int index = 0; index < filenameParts.length - 1; index++) {
			prefix.append((index > 0 ? "." : "") + filenameParts[index]); 
		}
		return prefix.toString();
	}
	
	public static void moveFile(String inputPath, String inputFile, String outputPath, String outputFile) {
		InputStream in = null;
		OutputStream out = null;
		File dir = new File(outputPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try {
			in = new FileInputStream(inputPath + inputFile);
			out = new FileOutputStream(outputPath + outputFile);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in=null;
			out.flush();
			out.close();out = null;
			new File(inputPath + inputFile).delete();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
}
