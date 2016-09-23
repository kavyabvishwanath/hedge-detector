
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
public class BeliefAnn_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (BeliefAnn_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = BeliefAnn_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new BeliefAnn(addr, BeliefAnn_Type.this);
  			   BeliefAnn_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new BeliefAnn(addr, BeliefAnn_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = BeliefAnn.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ccls.nlp.cbt.ts.BeliefAnn");
 
  /** @generated */
  final Feature casFeat_Tag;
  /** @generated */
  final int     casFeatCode_Tag;
  /** @generated */ 
  public String getTag(int addr) {
        if (featOkTst && casFeat_Tag == null)
      jcas.throwFeatMissing("Tag", "org.ccls.nlp.cbt.ts.BeliefAnn");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Tag);
  }
  /** @generated */    
  public void setTag(int addr, String v) {
        if (featOkTst && casFeat_Tag == null)
      jcas.throwFeatMissing("Tag", "org.ccls.nlp.cbt.ts.BeliefAnn");
    ll_cas.ll_setStringValue(addr, casFeatCode_Tag, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Speaker;
  /** @generated */
  final int     casFeatCode_Speaker;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSpeaker(int addr) {
        if (featOkTst && casFeat_Speaker == null)
      jcas.throwFeatMissing("Speaker", "org.ccls.nlp.cbt.ts.BeliefAnn");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Speaker);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSpeaker(int addr, String v) {
        if (featOkTst && casFeat_Speaker == null)
      jcas.throwFeatMissing("Speaker", "org.ccls.nlp.cbt.ts.BeliefAnn");
    ll_cas.ll_setStringValue(addr, casFeatCode_Speaker, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public BeliefAnn_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Tag = jcas.getRequiredFeatureDE(casType, "Tag", "uima.cas.String", featOkTst);
    casFeatCode_Tag  = (null == casFeat_Tag) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Tag).getCode();

 
    casFeat_Speaker = jcas.getRequiredFeatureDE(casType, "Speaker", "uima.cas.String", featOkTst);
    casFeatCode_Speaker  = (null == casFeat_Speaker) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Speaker).getCode();

  }
}



    