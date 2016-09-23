
/* First created by JCasGen Mon Jan 05 12:54:48 IST 2015 */
package org.ccls.nlp.cbt.ts;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Mon Jan 19 23:31:30 IST 2015
 * @generated */
public class QuoteAnn_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (QuoteAnn_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = QuoteAnn_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new QuoteAnn(addr, QuoteAnn_Type.this);
  			   QuoteAnn_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new QuoteAnn(addr, QuoteAnn_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = QuoteAnn.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ccls.nlp.cbt.ts.QuoteAnn");
 
  /** @generated */
  final Feature casFeat_origAuthor;
  /** @generated */
  final int     casFeatCode_origAuthor;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getOrigAuthor(int addr) {
        if (featOkTst && casFeat_origAuthor == null)
      jcas.throwFeatMissing("origAuthor", "org.ccls.nlp.cbt.ts.QuoteAnn");
    return ll_cas.ll_getStringValue(addr, casFeatCode_origAuthor);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setOrigAuthor(int addr, String v) {
        if (featOkTst && casFeat_origAuthor == null)
      jcas.throwFeatMissing("origAuthor", "org.ccls.nlp.cbt.ts.QuoteAnn");
    ll_cas.ll_setStringValue(addr, casFeatCode_origAuthor, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public QuoteAnn_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_origAuthor = jcas.getRequiredFeatureDE(casType, "origAuthor", "uima.cas.String", featOkTst);
    casFeatCode_origAuthor  = (null == casFeat_origAuthor) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_origAuthor).getCode();

  }
}



    