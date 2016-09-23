/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ccls.nlp.cbt.CollectionReaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.ccls.nlp.cbt.ts.AuthorAnn;
import org.ccls.nlp.cbt.ts.BeliefAnn;
import org.cleartk.token.type.Sentence;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;

import com.google.common.base.CharMatcher;

import edu.stanford.nlp.util.StringUtils;

public class LUCorpusCollectionReader_old extends CollectionReader_ImplBase {

	public static final String PARAM_INPUTDIR = "InputDirectory";
	public static final String PARAM_FILE_EXT = "FileExtension";
	public static final String PARAM_FILE = "File";


	private List<File> mFiles;

	private int mCurrentIndex;


	public static CollectionReader getCollectionReader(String Dir) throws ResourceInitializationException {
		System.out.println("Inside LUCorpusCollectionReader.getCollectionReader()");
		return CollectionReaderFactory.createCollectionReader(
				LUCorpusCollectionReader_old.class,
				TYPE_SYSTEM_DESCRIPTION,
				PARAM_INPUTDIR,
				Dir);
	}

	/**
	 * @see org.apache.uima.collection.CollectionReader_ImplBase#initialize()
	 */
	public void initialize() throws ResourceInitializationException {
		File directory = new File(((String) getConfigParameterValue(PARAM_INPUTDIR)).trim());

		mCurrentIndex = 0;

		// if input directory does not exist or is not a directory, throw exception
		if (!directory.exists() || !directory.isDirectory()) {
			throw new ResourceInitializationException(ResourceConfigurationException.DIRECTORY_NOT_FOUND,
					new Object[] { PARAM_INPUTDIR, this.getMetaData().getName(), directory.getPath() });
		}

		// get list of files in the specified directory, and subdirectories if the
		// parameter PARAM_SUBDIR is set to True
		mFiles = new ArrayList<File>();
		addFilesFromDir(directory);
	}

	/**
	 * This method adds files in the directory passed in as a parameter to mFiles.
	 * If mRecursive is true, it will include all files in all
	 * subdirectories (recursively), as well. 
	 * 
	 * @param dir
	 */
	private void addFilesFromDir(File dir) {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			//      if (!files[i].isDirectory() && files[i].getName().equals(file+".txt")) {
			//        if (!files[i].isDirectory() && files[i].getName().startsWith(file) && files[i].getName().endsWith(".txt")) {
			mFiles.add(files[i]);
			//      } 
		}
	}
	//public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION = TypeSystemDescriptionFactory
	//.createTypeSystemDescription("NDFRelationTypeSystem");

	public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION = TypeSystemDescriptionFactory
			.createTypeSystemDescription("LUCorpusTypeSystem");


	/**
	 * @see org.apache.uima.collection.CollectionReader#hasNext()
	 */
	public boolean hasNext() {
		return mCurrentIndex < mFiles.size();
	}

	/**
	 * @see org.apache.uima.collection.CollectionReader#getNext(org.apache.uima.cas.CAS)
	 */
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			throw new CollectionException(e);
		}

		// open input stream to file
		File file = (File) mFiles.get(mCurrentIndex++);
		String text = FileUtils.file2String(file);

		// Also store location of source document in CAS. This information is critical
		// if CAS Consumers will need to know where the original document contents are located.
		// For example, the Semantic Search CAS Indexer writes this information into the
		// search index that it creates, which allows applications that use the search index to
		// locate the documents that satisfy their semantic queries.
		SourceDocumentInformation srcDocInfo = new SourceDocumentInformation(jcas);
		srcDocInfo.setUri(file.getPath());
		srcDocInfo.setOffsetInSource(0);
		srcDocInfo.setDocumentSize((int) file.length());
		srcDocInfo.setLastSegment(mCurrentIndex == mFiles.size());
		srcDocInfo.addToIndexes();

		// Import SemHeart relations to CAS
		//System.out.println(text);
		importCBAnnotationsToCas(text,jcas);

	}

	/**
	 * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#close()
	 */
	public void close() throws IOException {

	}

	/**
	 * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#getProgress()
	 */
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(mCurrentIndex, mFiles.size(), Progress.ENTITIES) };
	}

	/**
	 * Gets the total number of documents that will be returned by this collection reader. This is not
	 * part of the general collection reader interface.
	 * 
	 * @return the number of documents in the collection
	 */
	public int getNumberOfDocuments() {
		return mFiles.size();
	}

	private void importCBAnnotationsToCas(String text,JCas jcas) {

		String textLines[] = Pattern.compile("\\r?\\n").split(text);
		//	  Pattern annotationPattern = Pattern.compile("(\\S+)\\(([^,\\/]+),([^,\\/]+)\\) \\| (.*)\\\\"); //.split(text);
		//	  Pattern annotationPattern = Pattern.compile("<sentence certainty=\"(uncertain|certain)\" id=\"(S\\d+\\.\\d+)\">(.*?)<\\/sentence>"); //.split(text);
		//	  Matcher annotationMatcher;

		//Pattern annPattern = Pattern.compile("<(CB|NCB|RB|NA)>(.*?)<\\/\\1>"); //.split(text);
		Pattern boostedAnnPattern = Pattern.compile("<(CB|NCB|RB_[A-Za-z_']+|NA)>(.*?)<\\/\\1>");
		Matcher annMatcher;

		String sentence = "";
		int subjStart, objStart, subjEnd, objEnd;
		StringBuffer outputBuffer = new StringBuffer();
		int outputBufferLength = 0;
		int count = 0;

		String prevLine = "";

		int i=0;
		for (String line : textLines) {
			//		  if (i++ % 1000 == 0)
			//			  System.out.println("Added " + i + " lines to JCAS");
			if (line.equals(prevLine))
				continue;
			count++;
			if (!CharMatcher.ASCII.matchesAllOf(line)) {
				continue;
			}
			//		  annotationMatcher = annotationPattern.matcher(line);
			if (true) {
				Sentence sentenceAnn = new Sentence(jcas);
				//			  LUCSentence sentenceAnn = new LUCSentence(jcas);
				sentenceAnn.setBegin(outputBufferLength);

				String sent = line;

				annMatcher = boostedAnnPattern.matcher(sent);
				while (annMatcher.find()) {

					annMatcher.appendReplacement(outputBuffer, annMatcher.group(2));
					outputBufferLength = outputBuffer.toString().length();
					
					BeliefAnn beliefAnn = new BeliefAnn(jcas);					
					beliefAnn.setBegin(outputBufferLength - annMatcher.group(2).length());
					beliefAnn.setEnd(outputBufferLength);
					String beliefTag = annMatcher.group(1);
					if (beliefTag.startsWith("RB_")) {
						importAuthorAnnotationsToCas(beliefTag, outputBufferLength - annMatcher.group(2).length(), outputBufferLength, jcas);
						beliefTag = "RB";
					}
					beliefAnn.setTag(beliefTag);					
					beliefAnn.addToIndexes();

				}
				annMatcher.appendTail(outputBuffer);
				//			  sent = sent.replaceAll("<ccue>", "");
				//			  sent = sent.replaceAll("<\\/ccue>", "");  
				outputBuffer.append( "\n");

				outputBufferLength = outputBuffer.toString().length();
				sentenceAnn.setEnd(outputBufferLength-1);
				//			  if (annotationMatcher.group(1).equals("certain"))
				//				  sentenceAnn.setCertainty(false);
				//			  else
				//				  sentenceAnn.setCertainty(true);
				//			  sentenceAnn.setUid(annotationMatcher.group(2));
				//			  sentenceAnn.addToIndexes();

				//			  Sentence sentenceForTok = new Sentence(jcas,sentenceAnn.getBegin(),
				//					  sentenceAnn.getEnd());
				//			  sentenceForTok.addToIndexes();


			}
			prevLine = line;

		}
		jcas.setDocumentText(outputBuffer.toString());
		//  	for (Cue cue : JCasUtil.select(jcas, Cue.class)) {
		//  		System.out.println(cue.getCoveredText());
		//  	}

		//	  for (Sentence sentenceAnn : JCasUtil.select(jcas, Sentence.class)) {
		//		  System.out.println(sentenceAnn.getCoveredText());
		//	  for (BeliefAnn b : JCasUtil.selectCovered(jcas, BeliefAnn.class, sentenceAnn)) {
		//for (BeliefAnn b : JCasUtil.select(jcas, BeliefAnn.class)) {
		//  System.out.println(b.getCoveredText() + "\t" + b.getTag());
		// }
		//	  }

		//	  System.out.println(JCasUtil.selectSingle(jcas,SourceDocumentInformation.class).getUri()  +"==>"+ JCasUtil.select(jcas, LUCSentence.class).size());

	} 

	private void importAuthorAnnotationsToCas(String text, Integer begin, Integer end, JCas jcas) {
		String[] parts = text.split("_");
		String authorTag = StringUtils.join(Arrays.copyOfRange(parts, 1, parts.length), " ");

		AuthorAnn authorAnn = new AuthorAnn(jcas);
		authorAnn.setAuthor(authorTag);
		authorAnn.setBegin(begin);
		authorAnn.setEnd(end);
		authorAnn.addToIndexes();
	}
}
