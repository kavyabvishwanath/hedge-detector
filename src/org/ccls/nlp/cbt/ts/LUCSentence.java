

/* First created by JCasGen Thu Dec 11 21:34:42 EST 2014 */
package org.ccls.nlp.cbt.ts;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jan 19 23:31:30 IST 2015
 * XML source: /Users/rupayanbasu/Documents/Belief/Code/Greg/CBTaggerPackage/CBTagger_fb_4_svmlite/desc/LUCorpusTypeSystem.xml
 * @generated */
public class LUCSentence extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(LUCSentence.class);
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
  protected LUCSentence() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public LUCSentence(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public LUCSentence(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public LUCSentence(JCas jcas, int begin, int end) {
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
  //* Feature: Fold

  /** getter for Fold - gets 
   * @generated
   * @return value of the feature 
   */
  public int getFold() {
    if (LUCSentence_Type.featOkTst && ((LUCSentence_Type)jcasType).casFeat_Fold == null)
      jcasType.jcas.throwFeatMissing("Fold", "org.ccls.nlp.cbt.ts.LUCSentence");
    return jcasType.ll_cas.ll_getIntValue(addr, ((LUCSentence_Type)jcasType).casFeatCode_Fold);}
    
  /** setter for Fold - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFold(int v) {
    if (LUCSentence_Type.featOkTst && ((LUCSentence_Type)jcasType).casFeat_Fold == null)
      jcasType.jcas.throwFeatMissing("Fold", "org.ccls.nlp.cbt.ts.LUCSentence");
    jcasType.ll_cas.ll_setIntValue(addr, ((LUCSentence_Type)jcasType).casFeatCode_Fold, v);}    
  }

    