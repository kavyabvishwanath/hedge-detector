
/* First created by JCasGen Thu Dec 11 21:34:42 EST 2014 */
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
public class LUCSentence_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (LUCSentence_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = LUCSentence_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new LUCSentence(addr, LUCSentence_Type.this);
  			   LUCSentence_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new LUCSentence(addr, LUCSentence_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = LUCSentence.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ccls.nlp.cbt.ts.LUCSentence");
 
  /** @generated */
  final Feature casFeat_Fold;
  /** @generated */
  final int     casFeatCode_Fold;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getFold(int addr) {
        if (featOkTst && casFeat_Fold == null)
      jcas.throwFeatMissing("Fold", "org.ccls.nlp.cbt.ts.LUCSentence");
    return ll_cas.ll_getIntValue(addr, casFeatCode_Fold);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFold(int addr, int v) {
        if (featOkTst && casFeat_Fold == null)
      jcas.throwFeatMissing("Fold", "org.ccls.nlp.cbt.ts.LUCSentence");
    ll_cas.ll_setIntValue(addr, casFeatCode_Fold, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public LUCSentence_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Fold = jcas.getRequiredFeatureDE(casType, "Fold", "uima.cas.Integer", featOkTst);
    casFeatCode_Fold  = (null == casFeat_Fold) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Fold).getCode();

  }
}



    