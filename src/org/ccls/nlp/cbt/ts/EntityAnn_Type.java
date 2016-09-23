
/* First created by JCasGen Thu Dec 11 21:34:43 EST 2014 */
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
public class EntityAnn_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (EntityAnn_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = EntityAnn_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new EntityAnn(addr, EntityAnn_Type.this);
  			   EntityAnn_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new EntityAnn(addr, EntityAnn_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = EntityAnn.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ccls.nlp.cbt.ts.EntityAnn");
 
  /** @generated */
  final Feature casFeat_entityId;
  /** @generated */
  final int     casFeatCode_entityId;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getEntityId(int addr) {
        if (featOkTst && casFeat_entityId == null)
      jcas.throwFeatMissing("entityId", "org.ccls.nlp.cbt.ts.EntityAnn");
    return ll_cas.ll_getStringValue(addr, casFeatCode_entityId);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setEntityId(int addr, String v) {
        if (featOkTst && casFeat_entityId == null)
      jcas.throwFeatMissing("entityId", "org.ccls.nlp.cbt.ts.EntityAnn");
    ll_cas.ll_setStringValue(addr, casFeatCode_entityId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_name;
  /** @generated */
  final int     casFeatCode_name;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getName(int addr) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "org.ccls.nlp.cbt.ts.EntityAnn");
    return ll_cas.ll_getStringValue(addr, casFeatCode_name);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setName(int addr, String v) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "org.ccls.nlp.cbt.ts.EntityAnn");
    ll_cas.ll_setStringValue(addr, casFeatCode_name, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public EntityAnn_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_entityId = jcas.getRequiredFeatureDE(casType, "entityId", "uima.cas.String", featOkTst);
    casFeatCode_entityId  = (null == casFeat_entityId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_entityId).getCode();

 
    casFeat_name = jcas.getRequiredFeatureDE(casType, "name", "uima.cas.String", featOkTst);
    casFeatCode_name  = (null == casFeat_name) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_name).getCode();

  }
}



    