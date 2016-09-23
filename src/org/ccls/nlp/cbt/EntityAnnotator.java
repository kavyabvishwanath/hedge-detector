package org.ccls.nlp.cbt;

import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.ccls.nlp.cbt.ts.BeliefAnn;
import org.ccls.nlp.cbt.ts.EntityAnn;
import org.ccls.nlp.cbt.ts.PostAnn;
import org.ccls.nlp.cbt.ts.QuoteAnn;
import org.ccls.nlp.cbt.utils.Tree;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.util.JCasUtil;

public class EntityAnnotator extends CleartkAnnotator<String> {
	
	private HashMap<String,HashSet<EntityBelief>> entityBeliefs;
	private static int counter = 0;
	
	
	private class EntityBelief{
		String text;
		String beliefToken;
		String beliefTag;
		String speaker;
		String entityId;
		String entityName;
		
		public EntityBelief(String t, String ann, String tag, String spk, String eid, String name){
			text = t;
			beliefToken = ann;
			beliefTag = tag;
			speaker = spk;
			entityId = eid;
			entityName = name;
		}
		
		@Override
		public int hashCode(){
			return text.hashCode() + beliefToken.hashCode() + beliefTag.hashCode() + speaker.hashCode() + entityId.hashCode() + entityName.hashCode();
		}
		
		@Override
		public boolean equals(Object o){
			if(o == null){
				return false;
			}
			EntityBelief eb = (EntityBelief) o;
			if(this.text.equals(eb.text) && this.beliefToken.equals(eb.beliefToken) && this.beliefTag.equals(eb.beliefTag) && this.speaker.equals(eb.speaker) && this.entityId.equals(eb.entityId) && this.entityName.equals(eb.entityName)){
				return true;
			}
			return false;
		}
	}
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
	}
	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		System.out.println("Inside process");
		entityBeliefs = new HashMap<String,HashSet<EntityBelief>>();
		counter++;
		Annotation beliefScopeSpan;
		for(BeliefAnn beliefAnn: JCasUtil.select(jCas, BeliefAnn.class)){
			beliefScopeSpan = getBeliefScopeSpan(jCas, beliefAnn);
			if(beliefScopeSpan == null){
				continue;
			}
			
			for(EntityAnn entityAnn: JCasUtil.selectCovered(jCas, EntityAnn.class, beliefScopeSpan)){
				String text = beliefScopeSpan.getCoveredText();
				String entityId = entityAnn.getEntityId();
				String name = entityAnn.getName();
				
				EntityBelief eb = new EntityBelief(text, beliefAnn.getCoveredText(), beliefAnn.getTag(), beliefAnn.getSpeaker(), entityId, name);
				
				try{
					if(!entityBeliefs.containsKey(entityId)){
						entityBeliefs.put(entityId, new HashSet<EntityBelief>());
					}
					entityBeliefs.get(entityId).add(eb);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("Doc: " + counter);
		for(String entity_id: entityBeliefs.keySet()){
			for(EntityBelief belief: entityBeliefs.get(entity_id)){
				System.out.println(belief.speaker + "\t" + belief.entityName + "\t" + belief.entityId + "\t" + belief.beliefTag + "\t" + belief.beliefToken + "\t" + belief.text.replaceAll("\\n", " "));
			}
			System.out.println();
		}
	}
	
	public Annotation getBeliefScopeSpan(JCas jCas, BeliefAnn beliefAnn){
		DependencyNode node;
		if (JCasUtil.selectCovered(jCas, DependencyNode.class,  beliefAnn).size() > 0) {
			node = JCasUtil.selectCovered(jCas, DependencyNode.class,  beliefAnn).get(0);
		}
		else{
			return null;
		}
		
		FSArray fsarray;
		int minBegin = Integer.MAX_VALUE;
		int maxEnd = Integer.MIN_VALUE;
		
		LinkedList<DependencyNode> q = new LinkedList<DependencyNode>();
		q.add(node);
		
		while(!q.isEmpty()){
			DependencyNode child = q.removeFirst();
			fsarray = child.getChildRelations();
			if(fsarray.size() > 0){
				for(int i=0; i<fsarray.size(); i++){
					q.add(((DependencyRelation) fsarray.get(i)).getChild());
				}
			}
			
			if(child.getBegin() < minBegin){
				minBegin = child.getBegin();
			}
			if(child.getEnd() > maxEnd){
				maxEnd = child.getEnd();
			}
		}
		return new Annotation(jCas, minBegin, maxEnd);
	}
	
	public static AnalysisEngineDescription getClassifierDescription(String modelFileName)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(
				EntityAnnotator.class, 
				CleartkAnnotator.PARAM_IS_TRAINING,
				false,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				modelFileName
				);
	}
}
