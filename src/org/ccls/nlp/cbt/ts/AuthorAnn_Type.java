
/* First created by JCasGen Thu Nov 15 14:11:13 EST 2012 */
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
public class AuthorAnn_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (AuthorAnn_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = AuthorAnn_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new AuthorAnn(addr, AuthorAnn_Type.this);
  			   AuthorAnn_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new AuthorAnn(addr, AuthorAnn_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = AuthorAnn.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ccls.nlp.cbt.ts.AuthorAnn");
 
  /** @generated */
  final Feature casFeat_Author;
  /** @generated */
  final int     casFeatCode_Author;
  /** @generated */ 
  public String getAuthor(int addr) {
        if (featOkTst && casFeat_Author == null)
      jcas.throwFeatMissing("Author", "org.ccls.nlp.cbt.ts.AuthorAnn");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Author);
  }
  /** @generated */    
  public void setAuthor(int addr, String v) {
        if (featOkTst && casFeat_Author == null)
      jcas.throwFeatMissing("Author", "org.ccls.nlp.cbt.ts.AuthorAnn");
    ll_cas.ll_setStringValue(addr, casFeatCode_Author, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public AuthorAnn_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Author = jcas.getRequiredFeatureDE(casType, "Author", "uima.cas.String", featOkTst);
    casFeatCode_Author  = (null == casFeat_Author) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Author).getCode();

  }
}



    