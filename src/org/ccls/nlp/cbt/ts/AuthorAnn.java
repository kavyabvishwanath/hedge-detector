

/* First created by JCasGen Thu Nov 15 14:11:13 EST 2012 */
package org.ccls.nlp.cbt.ts;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jan 19 23:31:30 IST 2015
 * XML source: /Users/rupayanbasu/Documents/Belief/Code/Greg/CBTaggerPackage/CBTagger_fb_4_svmlite/desc/LUCorpusTypeSystem.xml
 * @generated */
public class AuthorAnn extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(AuthorAnn.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected AuthorAnn() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public AuthorAnn(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public AuthorAnn(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public AuthorAnn(JCas jcas, int begin, int end) {
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
  //* Feature: Author

  /** getter for Author - gets 
   * @generated
   * @return value of the feature 
   */
  public String getAuthor() {
    if (AuthorAnn_Type.featOkTst && ((AuthorAnn_Type)jcasType).casFeat_Author == null)
      jcasType.jcas.throwFeatMissing("Author", "org.ccls.nlp.cbt.ts.AuthorAnn");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AuthorAnn_Type)jcasType).casFeatCode_Author);}
    
  /** setter for Author - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setAuthor(String v) {
    if (AuthorAnn_Type.featOkTst && ((AuthorAnn_Type)jcasType).casFeat_Author == null)
      jcasType.jcas.throwFeatMissing("Author", "org.ccls.nlp.cbt.ts.AuthorAnn");
    jcasType.ll_cas.ll_setStringValue(addr, ((AuthorAnn_Type)jcasType).casFeatCode_Author, v);}    
  }

    