package org.ccls.nlp.cbt.ts;

/* First created by JCasGen Thu Nov 15 14:11:13 EST 2012 */


import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jan 19 23:31:30 IST 2015
 * XML source: /Users/rupayanbasu/Documents/Belief/Code/Greg/CBTaggerPackage/CBTagger_fb_4_svmlite/desc/LUCorpusTypeSystem.xml
 * @generated */
public class BeliefAnn extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(BeliefAnn.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected BeliefAnn() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public BeliefAnn(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public BeliefAnn(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public BeliefAnn(JCas jcas, int begin, int end) {
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
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: Tag

  /** getter for Tag - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTag() {
    if (BeliefAnn_Type.featOkTst && ((BeliefAnn_Type)jcasType).casFeat_Tag == null)
      jcasType.jcas.throwFeatMissing("Tag", "org.ccls.nlp.cbt.ts.BeliefAnn");
    return jcasType.ll_cas.ll_getStringValue(addr, ((BeliefAnn_Type)jcasType).casFeatCode_Tag);}
    
  /** setter for Tag - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTag(String v) {
    if (BeliefAnn_Type.featOkTst && ((BeliefAnn_Type)jcasType).casFeat_Tag == null)
      jcasType.jcas.throwFeatMissing("Tag", "org.ccls.nlp.cbt.ts.BeliefAnn");
    jcasType.ll_cas.ll_setStringValue(addr, ((BeliefAnn_Type)jcasType).casFeatCode_Tag, v);}    
   
    
  //*--------------*
  //* Feature: Speaker

  /** getter for Speaker - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSpeaker() {
    if (BeliefAnn_Type.featOkTst && ((BeliefAnn_Type)jcasType).casFeat_Speaker == null)
      jcasType.jcas.throwFeatMissing("Speaker", "org.ccls.nlp.cbt.ts.BeliefAnn");
    return jcasType.ll_cas.ll_getStringValue(addr, ((BeliefAnn_Type)jcasType).casFeatCode_Speaker);}
    
  /** setter for Speaker - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSpeaker(String v) {
    if (BeliefAnn_Type.featOkTst && ((BeliefAnn_Type)jcasType).casFeat_Speaker == null)
      jcasType.jcas.throwFeatMissing("Speaker", "org.ccls.nlp.cbt.ts.BeliefAnn");
    jcasType.ll_cas.ll_setStringValue(addr, ((BeliefAnn_Type)jcasType).casFeatCode_Speaker, v);}    
  }

    