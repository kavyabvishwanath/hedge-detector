package org.ccls.nlp.cbt.Converters;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.Maps;

public class JcasToConll2009 {

	public static void convert(JCas jcas) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter("temp.txt");
		for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, sentence);
			Map<Token, Integer> tokenToId = Maps.newHashMap();

			int i = 1;
			for (Token token : tokens) { tokenToId.put(token, i++); }

			for (Token token : tokens) {

				DependencyNode tokenDepNode = JCasUtil.selectCovered(jcas, DependencyNode.class, token).get(0);
				if (tokenDepNode.getHeadRelations().size() == 0) {
					continue;
				}
				DependencyRelation headRelation = tokenDepNode.getHeadRelations(0);
				pw.println(tokenToId.get(token) + "\t" + token.getCoveredText() + "\t" + "_" + "\t" +
						token.getLemma() + "\t" + "_" + "\t" + token.getPos() + "\t" +
						"_" + "\t" + "_" + "\t" + "_" + "\t" + (headRelation.getHead() instanceof TopDependencyNode ? "0" : tokenToId.get(JCasUtil.selectCovered(jcas, Token.class, headRelation.getHead()).get(0))) + "\t" + "_" + "\t" + headRelation.getRelation());
			}
			pw.println("");
		}
		pw.close();
	}

	public static String relationToString(JCas jCas, DependencyRelation relation, Map<Token, Integer> tokenToId) {
		DependencyNode headNode = relation.getHead();
		DependencyNode childNode = relation.getChild();
		return String.format("%s(%s, %s)", 
				relation.getRelation(), 
				nodeToString(jCas, headNode, tokenToId),
				nodeToString(jCas, childNode, tokenToId));
	}

	public static String nodeToString(JCas jCas, DependencyNode node, Map<Token, Integer> tokenToId) {
		if (node instanceof TopDependencyNode) {
			return "ROOT-0";
		} else {
			Token token = JCasUtil.selectCovered(jCas, Token.class, node).get(0);
			return String.format("%s-%d", node.getCoveredText(), tokenToId.get(token));
		}
	}	

}