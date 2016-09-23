package org.ccls.nlp.cbt;
/**
 * @author Vinodkumar Prabhakaran.
 * 
 * This is an AnalysisEngine which writes all annotations into an xmi file
 *  
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLSerializer;
import org.ccls.nlp.cbt.ts.BeliefAnn;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
//import org.cleartk.examples.ExampleComponents;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;
import org.xml.sax.SAXException;



public class CBTagsWriter extends JCasAnnotator_ImplBase {

	  public static final String DEFAULT_OUTPUT_DIRECTORY = "C:/home/Projects//workingSpace/Data/AutoExtraction/Raw";  
	  //public static final String FILE_NAME = "XmiOut";
	  
	  private File mOutputDir;
	  
	  private int mDocNum;

	  public void initialize(UimaContext context) throws ResourceInitializationException {
		    
		  	super.initialize(context);
		  
		  	mDocNum = 0;
		    mOutputDir = new File((String) getContext().getConfigParameterValue(DEFAULT_OUTPUT_DIRECTORY));
		    if (!mOutputDir.exists()) {
		      mOutputDir.mkdirs();
		    }
		  }
	  
	  private void writeXmi(JCas jCas, File name, String modelFileName) throws IOException, SAXException {
		   FileOutputStream out = null;
		   try {
				FileWriter fw = new FileWriter(name);
		    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
//		    	System.out.println(sentence.getCoveredText());
		    	for (Token token : JCasUtil.selectCovered(jCas, Token.class, sentence)) {
		    		String tokenStr = token.getCoveredText();
//		    		System.out.println(tokenStr);
		    		if (JCasUtil.selectCovered(jCas, BeliefAnn.class, token).size()>0) {
		    			String tag = JCasUtil.selectCovered(jCas, BeliefAnn.class, token).get(0).getTag();
		    			String openTag = "<"+tag+">";
		    			String closeTag = "</"+tag+">";
		    			tokenStr = openTag + tokenStr + closeTag;
		    		}
		    		fw.write(tokenStr+" ");
//		    		System.out.print(tokenStr);
		    	}
		    	fw.write("\n");
//		    	System.out.println();
		    }
		    fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

//		      out = new FileOutputStream(name);
//		      XmiCasSerializer ser = new XmiCasSerializer(aCas.getTypeSystem());
//		      XMLSerializer xmlSer = new XMLSerializer(out, false);
//		      ser.serialize(aCas, xmlSer.getContentHandler());
//		   } finally {
//		      if (out != null) {
//		        out.close();
//		      }
//		   }
	  }
	  
	  public void process(JCas jCas) throws AnalysisEngineProcessException {
		  	System.out.println("Inside CBTagsWriter.process()");
		  
		    FSIterator<Annotation> it = jCas.getAnnotationIndex(SourceDocumentInformation.type).iterator();
		    File outFile = null;
		    if (it.hasNext()) {
		      SourceDocumentInformation fileLoc = (SourceDocumentInformation) it.next();
		      File inFile;
		      try {
		        //inFile = new File(new URL(fileLoc.getUri()).getPath());
		        String outFileName = new File(fileLoc.getUri()).getName();
		        if (fileLoc.getOffsetInSource() > 0) {
		          outFileName += fileLoc.getOffsetInSource();
		        }
		        outFile = new File(mOutputDir, outFileName + ".xml");
		      } catch (Exception e1) {
		        // invalid URL, use default processing below
		      }
		    }
		    if (outFile == null) {
		      outFile = new File(mOutputDir, "doc" + mDocNum++ + ".tagged");
		    }
		  
		  String modelFileName = outFile.getName() + ".ecore";
		  try {
		      writeXmi(jCas, outFile, modelFileName);
		  } 
		  catch(Exception e) {
		    	e.printStackTrace();
		  }		  
	  }  
	  
	 /* public void initialize(UimaContext context) throws ResourceInitializationException {
		    super.initialize(context);
			outDir = (String) getContext().getConfigParameterValue(DEFAULT_OUTPUT_DIRECTORY);
			file = (String) getContext().getConfigParameterValue(FILE_NAME);			
	  }
*/
	  public static AnalysisEngineDescription getWriterDescription(String outputDirectory)
	      throws ResourceInitializationException {
		  AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
		    CBTagsWriter.class,
//	        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
	        DEFAULT_OUTPUT_DIRECTORY,
	        outputDirectory
	        );
	    return aed;
  }
}
