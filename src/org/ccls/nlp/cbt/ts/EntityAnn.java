

/* First created by JCasGen Thu Dec 11 21:34:43 EST 2014 */
package org.ccls.nlp.cbt.ts;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jan 19 23:31:30 IST 2015
 * XML source: /Users/rupayanbasu/Documents/Belief/Code/Greg/CBTaggerPackage/CBTagger_fb_4_svmlite/desc/LUCorpusTypeSystem.xml
 * @generated */
public class EntityAnn extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(EntityAnn.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected EntityAnn() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public EntityAnn(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public EntityAnn(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public EntityAnn(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: entityId

  /** getter for entityId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getEntityId() {
    if (EntityAnn_Type.featOkTst && ((EntityAnn_Type)jcasType).casFeat_entityId == null)
      jcasType.jcas.throwFeatMissing("entityId", "org.ccls.nlp.cbt.ts.EntityAnn");
    return jcasType.ll_cas.ll_getStringValue(addr, ((EntityAnn_Type)jcasType).casFeatCode_entityId);}
    
  /** setter for entityId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setEntityId(String v) {
    if (EntityAnn_Type.featOkTst && ((EntityAnn_Type)jcasType).casFeat_entityId == null)
      jcasType.jcas.throwFeatMissing("entityId", "org.ccls.nlp.cbt.ts.EntityAnn");
    jcasType.ll_cas.ll_setStringValue(addr, ((EntityAnn_Type)jcasType).casFeatCode_entityId, v);}    
   
    
  //*--------------*
  //* Feature: name

  /** getter for name - gets 
   * @generated
   * @return value of the feature 
   */
  public String getName() {
    if (EntityAnn_Type.featOkTst && ((EntityAnn_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "org.ccls.nlp.cbt.ts.EntityAnn");
    return jcasType.ll_cas.ll_getStringValue(addr, ((EntityAnn_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setName(String v) {
    if (EntityAnn_Type.featOkTst && ((EntityAnn_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "org.ccls.nlp.cbt.ts.EntityAnn");
    jcasType.ll_cas.ll_setStringValue(addr, ((EntityAnn_Type)jcasType).casFeatCode_name, v);}    
  }

    