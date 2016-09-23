package org.ccls.nlp.cbt;

/**

 * 
 * This is an AnalysisEngine which writes all annotations into an xmi file
 *  
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLSerializer;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.token.type.Sentence;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;
import org.xml.sax.SAXException;

public class AnnotationWriter extends JCasAnnotator_ImplBase {

	public static final String DEFAULT_OUTPUT_DIRECTORY = "";  

	private File mOutputDir;

	private int mDocNum;

	public static int sentenceCount = 0;
	public static int parts = 0;
	public static String mode = "";
	
	public void initialize(UimaContext context) throws ResourceInitializationException {

		super.initialize(context);

		mDocNum = 0;
		mOutputDir = new File((String) getContext().getConfigParameterValue(DEFAULT_OUTPUT_DIRECTORY));
		if (!mOutputDir.exists()) {
			mOutputDir.mkdirs();
		}
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {
		// first, filter based upon sentences we require
		Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
		List<Sentence> badSentences = new ArrayList<Sentence>();
		for (Sentence sentence : sentences) {
			int slot = 1 + (sentenceCount % parts);
			if (mode.equals("train")) {
				if (slot > 4) {
					badSentences.add(sentence);
				}
			} else if (mode.equals("test")) {
				if (slot != 6) {
					badSentences.add(sentence);
				}
			} else if (mode.equals("dev")) {
				if (slot != 5) {
					badSentences.add(sentence);
				}
				
			}
			sentenceCount++;
		}
		for (Sentence sentence : badSentences) {
			jCas.removeFsFromIndexes(sentence);
		}
		
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
				outFile = new File(mOutputDir, outFileName + ".xmi");
			} catch (Exception e1) {
				// invalid URL, use default processing below
			}
		}
		if (outFile == null) {
			outFile = new File(mOutputDir, "doc" + mDocNum++ + ".xmi");
		}

		String modelFileName = outFile.getName() + ".ecore";
		try {
			writeXmi(jCas.getCas(), outFile, modelFileName);
		} 
		catch(Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}	 
	}  

	private void writeXmi(CAS aCas, File name, String modelFileName) throws IOException, SAXException {
		FileOutputStream out = null;
		try {
			// write XMI
			out = new FileOutputStream(name);
			XmiCasSerializer ser = new XmiCasSerializer(aCas.getTypeSystem());
			XMLSerializer xmlSer = new XMLSerializer(out, false);
			ser.serialize(aCas, xmlSer.getContentHandler());
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	public static AnalysisEngineDescription getWriterDescription(String outputDirectory)
			throws ResourceInitializationException {
		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
				AnnotationWriter.class,
				CleartkAnnotator.PARAM_IS_TRAINING,
				true,
				DEFAULT_OUTPUT_DIRECTORY,
				outputDirectory
				);
		return aed;
	}
}
