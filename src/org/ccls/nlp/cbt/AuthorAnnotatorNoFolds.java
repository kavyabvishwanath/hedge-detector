package org.ccls.nlp.cbt;


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.ccls.nlp.cbt.ts.AuthorAnn;
import org.ccls.nlp.cbt.ts.BeliefAnn;
import org.ccls.nlp.cbt.utils.FileUtils;
import org.cleartk.ne.type.NamedEntity;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.util.JCasUtil;

/*
 * Note: belief must already exist in jCas so this annotation must be performed after
 */
public class AuthorAnnotatorNoFolds extends JCasAnnotator_ImplBase{

	public static final String[] PRONOUNS =  {"we", "i", "you", "they", "he", "she", "it"};

	public static boolean isPronoun(String token) {
		String lcase = token.toLowerCase();
		for (String pronoun : PRONOUNS) {
			if (lcase.equals(pronoun)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		String filename = FileUtils.getFilename(jCas);
		String prefix = FileUtils.getDocumentPrefixFilename(filename);

		PrintWriter pw = null;

		try {
			pw = new PrintWriter("factbank_data/test/txtTagged/" + prefix + ".txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}

		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
			
			List<NamedEntityMention> mentions = new ArrayList<NamedEntityMention>(JCasUtil.selectCovered(jCas, NamedEntityMention.class, sentence));
			Iterator<NamedEntityMention> mentionIterator = mentions.iterator();

			while (mentionIterator.hasNext()) {
				NamedEntityMention mention = mentionIterator.next();
				if (mention.getMentionType() != null && !mention.getMentionType().equals("PERSON")) {
					mentionIterator.remove();
				}
			}			
			
			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
			for (int t = 0; t < tokens.size(); t++) {
				Token token = tokens.get(t);				
								
				String tokenText = t == 0 ? token.getCoveredText() : " " + token.getCoveredText();
				pw.print(tokenText);				

				if (JCasUtil.selectCovered(jCas, BeliefAnn.class,token).size() == 0) {
					continue;
				}				
				BeliefAnn belief = JCasUtil.selectCovered(jCas, BeliefAnn.class, token).get(0);
				
				pw.print("/" + belief.getTag());
				
				if (!belief.getTag().equals("RB") && (!belief.getTag().equals("ROB"))) {
					continue;
				}
				String selectedEntity = "UNKNOWN";
				if (mentions.size() > 0) {
					NamedEntity entity = mentions.get(0).getMentionedEntity();
					FSArray array = entity.getMentions();
					for (int index = 0; index < array.size(); index++) {
						NamedEntityMention nem = (NamedEntityMention) array.get(index);
						if (!AuthorAnnotatorNoFolds.isPronoun(nem.getCoveredText())) {
							selectedEntity = nem.getCoveredText();
							break;
						}
					}
				}

				AuthorAnn aa = new AuthorAnn(jCas);
				aa.setBegin(token.getBegin());
				aa.setEnd(token.getEnd());
				aa.setAuthor(selectedEntity);
				aa.addToIndexes();
				
				pw.print("*" + selectedEntity + "$" + mentions.size() + "*");
			}
			pw.println();
		}
		if (pw != null) {
			pw.close();
		}
	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(
				AuthorAnnotatorNoFolds.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("LUCorpusTypeSystem")
				);
	}

}
