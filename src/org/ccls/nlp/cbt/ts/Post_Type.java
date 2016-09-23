
/* First created by JCasGen Mon Jan 05 12:32:25 IST 2015 */
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
 * Updated by JCasGen Mon Jan 05 12:32:25 IST 2015
 * @generated */
public class Post_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Post_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Post_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Post(addr, Post_Type.this);
  			   Post_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Post(addr, Post_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Post.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ccls.nlp.cbt.ts.Post");
 
  /** @generated */
  final Feature casFeat_postAuthor;
  /** @generated */
  final int     casFeatCode_postAuthor;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPostAuthor(int addr) {
        if (featOkTst && casFeat_postAuthor == null)
      jcas.throwFeatMissing("postAuthor", "org.ccls.nlp.cbt.ts.Post");
    return ll_cas.ll_getStringValue(addr, casFeatCode_postAuthor);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPostAuthor(int addr, String v) {
        if (featOkTst && casFeat_postAuthor == null)
      jcas.throwFeatMissing("postAuthor", "org.ccls.nlp.cbt.ts.Post");
    ll_cas.ll_setStringValue(addr, casFeatCode_postAuthor, v);}
    
  
 
  /** @generated */
  final Feature casFeat_quoteAuthor;
  /** @generated */
  final int     casFeatCode_quoteAuthor;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getQuoteAuthor(int addr) {
        if (featOkTst && casFeat_quoteAuthor == null)
      jcas.throwFeatMissing("quoteAuthor", "org.ccls.nlp.cbt.ts.Post");
    return ll_cas.ll_getStringValue(addr, casFeatCode_quoteAuthor);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setQuoteAuthor(int addr, String v) {
        if (featOkTst && casFeat_quoteAuthor == null)
      jcas.throwFeatMissing("quoteAuthor", "org.ccls.nlp.cbt.ts.Post");
    ll_cas.ll_setStringValue(addr, casFeatCode_quoteAuthor, v);}
    
  
 
  /** @generated */
  final Feature casFeat_postId;
  /** @generated */
  final int     casFeatCode_postId;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPostId(int addr) {
        if (featOkTst && casFeat_postId == null)
      jcas.throwFeatMissing("postId", "org.ccls.nlp.cbt.ts.Post");
    return ll_cas.ll_getStringValue(addr, casFeatCode_postId);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPostId(int addr, String v) {
        if (featOkTst && casFeat_postId == null)
      jcas.throwFeatMissing("postId", "org.ccls.nlp.cbt.ts.Post");
    ll_cas.ll_setStringValue(addr, casFeatCode_postId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_postDatetime;
  /** @generated */
  final int     casFeatCode_postDatetime;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPostDatetime(int addr) {
        if (featOkTst && casFeat_postDatetime == null)
      jcas.throwFeatMissing("postDatetime", "org.ccls.nlp.cbt.ts.Post");
    return ll_cas.ll_getStringValue(addr, casFeatCode_postDatetime);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPostDatetime(int addr, String v) {
        if (featOkTst && casFeat_postDatetime == null)
      jcas.throwFeatMissing("postDatetime", "org.ccls.nlp.cbt.ts.Post");
    ll_cas.ll_setStringValue(addr, casFeatCode_postDatetime, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Post_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_postAuthor = jcas.getRequiredFeatureDE(casType, "postAuthor", "uima.cas.String", featOkTst);
    casFeatCode_postAuthor  = (null == casFeat_postAuthor) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_postAuthor).getCode();

 
    casFeat_quoteAuthor = jcas.getRequiredFeatureDE(casType, "quoteAuthor", "uima.cas.String", featOkTst);
    casFeatCode_quoteAuthor  = (null == casFeat_quoteAuthor) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_quoteAuthor).getCode();

 
    casFeat_postId = jcas.getRequiredFeatureDE(casType, "postId", "uima.cas.String", featOkTst);
    casFeatCode_postId  = (null == casFeat_postId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_postId).getCode();

 
    casFeat_postDatetime = jcas.getRequiredFeatureDE(casType, "postDatetime", "uima.cas.String", featOkTst);
    casFeatCode_postDatetime  = (null == casFeat_postDatetime) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_postDatetime).getCode();

  }
}



    