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
import java.util.Stack;
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
import org.ccls.nlp.cbt.ts.EntityAnn;
import org.ccls.nlp.cbt.ts.PostAnn;
import org.ccls.nlp.cbt.ts.QuoteAnn;
import org.cleartk.token.type.Sentence;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;

import com.google.common.base.CharMatcher;

import edu.stanford.nlp.util.StringUtils;

public class ERECorpusCollectionReader extends CollectionReader_ImplBase {

	public static final String PARAM_INPUTDIR = "InputDirectory";
	public static final String PARAM_FILE_EXT = "FileExtension";
	public static final String PARAM_FILE = "File";
	

	private List<File> mFiles;

	private int mCurrentIndex;
	
	private static String erePath;


	public static CollectionReader getCollectionReader(String comboPath) throws ResourceInitializationException {
		System.out.println("Inside ERECorpusCollectionReader.getCollectionReader()");
		String beliefPath = comboPath.split(":")[0];
		erePath = comboPath.split(":")[1];
		return CollectionReaderFactory.createCollectionReader(
				ERECorpusCollectionReader.class,
				TYPE_SYSTEM_DESCRIPTION,
				PARAM_INPUTDIR,
				beliefPath);
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
			if(files[i].getName().contains(".xml")){
				mFiles.add(files[i]);
			}
		}
	}
	
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
		System.out.println("ERECorpusCollectionReader.getNext(): Belief file: " + file.getName());

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
		
		
		String name = file.getName().split("\\.")[0];
		File ereFile = new File(erePath + name + ".ere.xml");
		System.out.println("ERECorpusCollectionReader.getNext(): Entity file: " + ereFile.getName());
		String ereText = FileUtils.file2String(ereFile);
		
		importEntityAnnotationsToCas(ereText,jcas);
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
		Pattern boostedAnnPattern = Pattern.compile("<(CB|NCB|ROB|NA)>(.*?)<\\/\\1>");
		Pattern quoteAuthorPat = Pattern.compile("orig_author=\"(\\S+)\"");
		Pattern postAuthorPat =  Pattern.compile("author=\"(\\S+)\"");
		Pattern postDatetimePat = Pattern.compile("datetime=\"(\\S+)\"");
		Pattern postIdPat = Pattern.compile("id=\"(\\S+)\"");
		
		Pattern linkPat = Pattern.compile("(<a .*>)");
		Pattern postPat = Pattern.compile("(<post .*>)");
		Pattern quotePat = Pattern.compile("(<quote .*>)");
		
		Matcher annMatcher;

		StringBuffer outputBuffer = new StringBuffer();
		int outputBufferLength = 0;
		
		String prevLine = "";
		String postAuthor = "", postId = "", postDatetime = "";
		int postBegin = 0, postEnd = 0;
		String quoteAuthor = "";
		int quoteBegin = 0, quoteEnd = 0;
		String speaker = "";
		Stack<String> quoteAuthors = new Stack<String>();
		
		//replace html and metadata tags with spaces of the same length (to preserve offset values)
		for (String line : textLines) {
			annMatcher = linkPat.matcher(line);
			while(annMatcher.find()){
				String link = annMatcher.group();
				StringBuffer sb = new StringBuffer();
				for(int i=0; i<link.length(); i++){
					sb.append(" ");
				}
				line = line.replaceAll(link, sb.toString());
			}
			line = line.replaceAll("</a>", "    ");
			
			
			
			if (line.equals(prevLine))
				continue;
			if (!CharMatcher.ASCII.matchesAllOf(line)) {
				System.out.println("ASCII match failed. Skipping line: "+line);
//				continue;
			}
			
			if(line.contains("<post")){
				
				
				annMatcher = postAuthorPat.matcher(line);
				while(annMatcher.find()){
					postAuthor = annMatcher.group(1);
				}
				
//				System.out.println(postAuthor);
				
				annMatcher = postIdPat.matcher(line);
				while(annMatcher.find()){
					postId = annMatcher.group(1);
				}
				
				annMatcher = postDatetimePat.matcher(line);
				while(annMatcher.find()){
					postDatetime = annMatcher.group(1);
				}
				
				postBegin = outputBuffer.toString().length();
				
				annMatcher = postPat.matcher(line);
				while(annMatcher.find()){
					String post = annMatcher.group();
					StringBuffer sb = new StringBuffer();
					for(int i=0; i<post.length(); i++){
						sb.append(" ");
					}
					line = line.replaceAll(post, sb.toString());
				}
			}
			if(line.contains("</post>")){
				
				postEnd = outputBuffer.toString().length();
				line = line.replaceAll("</post>", "       ");
				
				PostAnn postAnn = new PostAnn(jcas);
				postAnn.setBegin(postBegin);
				postAnn.setEnd(postEnd);
				postAnn.setPostId(postId);
				postAnn.setPostAuthor(postAuthor);
				postAnn.setPostDatetime(postDatetime);
				postAnn.addToIndexes();
//				System.out.println(postAnn.getPostAuthor());
//				postAuthor = "";
			}
			if(line.contains("<quote")){
				annMatcher = quoteAuthorPat.matcher(line);
				String name = "";
				while(annMatcher.find()){
					name = annMatcher.group(1);
				}
				quoteAuthors.push(name);
				quoteAuthor = quoteAuthors.peek();
				quoteBegin = outputBuffer.toString().length();
				
				annMatcher = quotePat.matcher(line);
				while(annMatcher.find()){
					String quote = annMatcher.group();
					StringBuffer sb = new StringBuffer();
					for(int i=0; i<quote.length(); i++){
						sb.append(" ");
					}
					line = line.replaceAll(quote, sb.toString());
				}
			}
			if(line.contains("</quote>")){
				quoteAuthors.pop();
				if(!quoteAuthors.isEmpty()){
					quoteAuthor = quoteAuthors.peek();
				}
				else{
					quoteAuthor = "";
				}
				line = line.replaceAll("</quote>", "        ");
				quoteEnd = outputBuffer.toString().length();
				
				QuoteAnn quoteAnn = new QuoteAnn(jcas);
				quoteAnn.setBegin(quoteBegin);
				quoteAnn.setEnd(quoteEnd);
				quoteAnn.setOrigAuthor(quoteAuthor);
				quoteAnn.addToIndexes();
			}
			
			if(!quoteAuthors.isEmpty()){
				speaker = quoteAuthor;
			}
			else{
				speaker = postAuthor;
			}
			
			String sent = line;

			annMatcher = boostedAnnPattern.matcher(sent);
			while (annMatcher.find()) {

				try{
					annMatcher.appendReplacement(outputBuffer, annMatcher.group(2));
				}catch(Exception e){
					System.out.println("ERECorpusCollectionReader.importCBAnnotationsToCas(): ERROR: " + annMatcher.group(2));
				}
				
				outputBufferLength = outputBuffer.toString().length();

				//Rupayan - handle special case for contractions eg: can't
				BeliefAnn beliefAnn = new BeliefAnn(jcas);
				String tagText = annMatcher.group(2).toLowerCase();
				if(tagText.contains("\'")){
					int index = 0;
					if(tagText.contains("n\'t")){
//						System.out.println("contains n't");
						index = tagText.indexOf('\'') - 1;
					}
					else{
						index = tagText.indexOf('\'');
					}
					beliefAnn.setBegin(outputBufferLength - annMatcher.group(2).length());
					beliefAnn.setEnd(outputBufferLength - annMatcher.group(2).length() + index);
					String beliefTag = annMatcher.group(1);
					beliefAnn.setTag(beliefTag);	
					beliefAnn.setSpeaker(speaker);
					beliefAnn.addToIndexes();
				}
				else{
					beliefAnn.setBegin(outputBufferLength - annMatcher.group(2).length());
					beliefAnn.setEnd(outputBufferLength);
					String beliefTag = annMatcher.group(1);
					beliefAnn.setTag(beliefTag);	
					beliefAnn.setSpeaker(speaker);
					beliefAnn.addToIndexes();
				}
			}
			annMatcher.appendTail(outputBuffer);
			outputBuffer.append( "\n");

			outputBufferLength = outputBuffer.toString().length();
			prevLine = line;
		}

		jcas.setDocumentText(outputBuffer.toString());
//		System.out.println(outputBuffer.toString());
	} 
	
	private void importEntityAnnotationsToCas(String text,JCas jcas) {

		String textLines[] = Pattern.compile("\\r?\\n").split(text);
		Pattern nerIdPattern = Pattern.compile("id=\"(ent-[0-9]+)\"");
		Pattern offsetPat = Pattern.compile("offset=\"([0-9]+)\"");
		Pattern lengthPat = Pattern.compile("length=\"([0-9]+)\"");
		Pattern entMentionPat = Pattern.compile(">(.*?)<");

		String entityId = "";
		String name = "";
		int begin = 0, end = 0;
		Matcher matcher;
		
		for (String line : textLines) {
//			System.out.println(line);
			if(line.contains("<entity ")){
//				System.out.println(line);
				matcher = nerIdPattern.matcher(line);
				while(matcher.find()){
					entityId = matcher.group(1);
				}
				continue;
			}
			if(line.contains("<entity_mention ")){
				matcher = entMentionPat.matcher(line); 
				while(matcher.find()){
					name = matcher.group(1);
				}
				
				matcher = offsetPat.matcher(line);
				while(matcher.find()){
					begin = Integer.parseInt(matcher.group(1));
				}
				
				matcher = lengthPat.matcher(line);
				while(matcher.find()){
					end = begin + Integer.parseInt(matcher.group(1));
				}
				
				EntityAnn entityAnn = new EntityAnn(jcas);
				entityAnn.setBegin(begin);
				entityAnn.setEnd(end);
				entityAnn.setEntityId(entityId);		
				entityAnn.setName(name);
				entityAnn.addToIndexes();
//				System.out.println("Entity: " + entityAnn.getCoveredText());
			}
			else{
				entityId = "";
			}
		
		}
	}
}

