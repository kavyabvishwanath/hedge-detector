

/* First created by JCasGen Mon Jan 05 12:54:48 IST 2015 */
package org.ccls.nlp.cbt.ts;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jan 19 23:31:30 IST 2015
 * XML source: /Users/rupayanbasu/Documents/Belief/Code/Greg/CBTaggerPackage/CBTagger_fb_4_svmlite/desc/LUCorpusTypeSystem.xml
 * @generated */
public class QuoteAnn extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(QuoteAnn.class);
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
  protected QuoteAnn() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public QuoteAnn(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public QuoteAnn(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public QuoteAnn(JCas jcas, int begin, int end) {
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
  //* Feature: origAuthor

  /** getter for origAuthor - gets 
   * @generated
   * @return value of the feature 
   */
  public String getOrigAuthor() {
    if (QuoteAnn_Type.featOkTst && ((QuoteAnn_Type)jcasType).casFeat_origAuthor == null)
      jcasType.jcas.throwFeatMissing("origAuthor", "org.ccls.nlp.cbt.ts.QuoteAnn");
    return jcasType.ll_cas.ll_getStringValue(addr, ((QuoteAnn_Type)jcasType).casFeatCode_origAuthor);}
    
  /** setter for origAuthor - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setOrigAuthor(String v) {
    if (QuoteAnn_Type.featOkTst && ((QuoteAnn_Type)jcasType).casFeat_origAuthor == null)
      jcasType.jcas.throwFeatMissing("origAuthor", "org.ccls.nlp.cbt.ts.QuoteAnn");
    jcasType.ll_cas.ll_setStringValue(addr, ((QuoteAnn_Type)jcasType).casFeatCode_origAuthor, v);}    
  }

    