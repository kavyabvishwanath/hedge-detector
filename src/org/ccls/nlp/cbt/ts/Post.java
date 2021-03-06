

/* First created by JCasGen Mon Jan 05 12:32:25 IST 2015 */
package org.ccls.nlp.cbt.ts;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jan 05 12:32:25 IST 2015
 * XML source: /Users/rupayanbasu/Documents/Belief/Code/Greg/CBTaggerPackage/CBTagger_fb_4_svmlite/desc/LUCorpusTypeSystem.xml
 * @generated */
public class Post extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Post.class);
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
  protected Post() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Post(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Post(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Post(JCas jcas, int begin, int end) {
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
  //* Feature: postAuthor

  /** getter for postAuthor - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPostAuthor() {
    if (Post_Type.featOkTst && ((Post_Type)jcasType).casFeat_postAuthor == null)
      jcasType.jcas.throwFeatMissing("postAuthor", "org.ccls.nlp.cbt.ts.Post");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Post_Type)jcasType).casFeatCode_postAuthor);}
    
  /** setter for postAuthor - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPostAuthor(String v) {
    if (Post_Type.featOkTst && ((Post_Type)jcasType).casFeat_postAuthor == null)
      jcasType.jcas.throwFeatMissing("postAuthor", "org.ccls.nlp.cbt.ts.Post");
    jcasType.ll_cas.ll_setStringValue(addr, ((Post_Type)jcasType).casFeatCode_postAuthor, v);}    
   
    
  //*--------------*
  //* Feature: quoteAuthor

  /** getter for quoteAuthor - gets 
   * @generated
   * @return value of the feature 
   */
  public String getQuoteAuthor() {
    if (Post_Type.featOkTst && ((Post_Type)jcasType).casFeat_quoteAuthor == null)
      jcasType.jcas.throwFeatMissing("quoteAuthor", "org.ccls.nlp.cbt.ts.Post");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Post_Type)jcasType).casFeatCode_quoteAuthor);}
    
  /** setter for quoteAuthor - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setQuoteAuthor(String v) {
    if (Post_Type.featOkTst && ((Post_Type)jcasType).casFeat_quoteAuthor == null)
      jcasType.jcas.throwFeatMissing("quoteAuthor", "org.ccls.nlp.cbt.ts.Post");
    jcasType.ll_cas.ll_setStringValue(addr, ((Post_Type)jcasType).casFeatCode_quoteAuthor, v);}    
   
    
  //*--------------*
  //* Feature: postId

  /** getter for postId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPostId() {
    if (Post_Type.featOkTst && ((Post_Type)jcasType).casFeat_postId == null)
      jcasType.jcas.throwFeatMissing("postId", "org.ccls.nlp.cbt.ts.Post");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Post_Type)jcasType).casFeatCode_postId);}
    
  /** setter for postId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPostId(String v) {
    if (Post_Type.featOkTst && ((Post_Type)jcasType).casFeat_postId == null)
      jcasType.jcas.throwFeatMissing("postId", "org.ccls.nlp.cbt.ts.Post");
    jcasType.ll_cas.ll_setStringValue(addr, ((Post_Type)jcasType).casFeatCode_postId, v);}    
   
    
  //*--------------*
  //* Feature: postDatetime

  /** getter for postDatetime - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPostDatetime() {
    if (Post_Type.featOkTst && ((Post_Type)jcasType).casFeat_postDatetime == null)
      jcasType.jcas.throwFeatMissing("postDatetime", "org.ccls.nlp.cbt.ts.Post");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Post_Type)jcasType).casFeatCode_postDatetime);}
    
  /** setter for postDatetime - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPostDatetime(String v) {
    if (Post_Type.featOkTst && ((Post_Type)jcasType).casFeat_postDatetime == null)
      jcasType.jcas.throwFeatMissing("postDatetime", "org.ccls.nlp.cbt.ts.Post");
    jcasType.ll_cas.ll_setStringValue(addr, ((Post_Type)jcasType).casFeatCode_postDatetime, v);}    
  }

    