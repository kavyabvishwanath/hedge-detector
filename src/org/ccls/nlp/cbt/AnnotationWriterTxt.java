package org.ccls.nlp.cbt;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.ccls.nlp.cbt.ts.BeliefAnn;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

public class AnnotationWriterTxt extends JCasAnnotator_ImplBase {

	public static final String DEFAULT_OUTPUT_DIRECTORY = "";  

	private File mOutputDir;

	private int mDocNum;
	
	public static int totalTokenCount = 0;
	
	public void initialize(UimaContext context) throws ResourceInitializationException {

		super.initialize(context);

		mDocNum = 0;
		mOutputDir = new File((String) getContext().getConfigParameterValue(DEFAULT_OUTPUT_DIRECTORY));
		if (!mOutputDir.exists()) {
			mOutputDir.mkdirs();
		}
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {		
		// Now, write out the appropriate data
		FSIterator<Annotation> it = jCas.getAnnotationIndex(SourceDocumentInformation.type).iterator();
		File outFile = null;
		if (it.hasNext()) {
			SourceDocumentInformation fileLoc = (SourceDocumentInformation) it.next();
			try {
				String outFileName = new File(fileLoc.getUri()).getName();
				if (fileLoc.getOffsetInSource() > 0) {
					outFileName += fileLoc.getOffsetInSource();
				}
				outFile = new File(mOutputDir, outFileName);
			} catch (Exception e1) {
				// invalid URL, use default processing below
			}
		}
		if (outFile == null) {
			outFile = new File(mOutputDir, "doc" + mDocNum++);
		}

		String modelFileName = outFile.getName() + ".ecore";
		try {
			List<Sentence> sentences = new ArrayList<Sentence>(JCasUtil.select(jCas, Sentence.class));
			for (Sentence sentence : sentences) {
				List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
				totalTokenCount += tokens.size();
			}
			writeTxt(jCas, outFile, modelFileName);
		} 
		catch(Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}	 
	}  

	private void writeTxt(JCas jCas, File name, String modelFileName) throws IOException {
		PrintWriter pw = null;
		try {
			// write Text
			pw = new PrintWriter(name);
			List<Sentence> sentences = new ArrayList<Sentence>(JCasUtil.select(jCas, Sentence.class));
			for (int s = 0; s < sentences.size(); s++) {
				List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentences.get(s));
				for (int t = 0; t < tokens.size(); t++) {
					Token token = tokens.get(t);
					List<BeliefAnn> beliefs = JCasUtil.selectCovered(jCas,  BeliefAnn.class, token);
					String text = token.getCoveredText();
					if (t > 0) {
						pw.print(" ");
					}
					if (beliefs.size() > 0) {
						pw.print("<" + beliefs.get(0).getTag() + ">");
					}
					pw.print(text);
					if (beliefs.size() > 0) {
						pw.print("</" + beliefs.get(0).getTag() + ">");
					}
				}
				pw.println();
			}
			
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
	
	public static AnalysisEngineDescription getWriterDescription(String outputDirectory)
			throws ResourceInitializationException {
		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
				AnnotationWriterTxt.class,
				CleartkAnnotator.PARAM_IS_TRAINING,
				true,
				DEFAULT_OUTPUT_DIRECTORY,
				outputDirectory
				);
		return aed;
	}
}
