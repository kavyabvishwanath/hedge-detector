package org.ccls.nlp.cbt;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.JCas;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.util.JCasUtil;

/**
 * Runs Morgan's Hedge Classifier on a document
 * Created by Risa on 7/26/16.
 */
public class HedgeClassifier {
    public static String NO = "_";//output from the classifier that indicates a token isn't a hedge

    //word judgement confidence type in the order returned by classify.py ex. thinks S   1   hRel
    public static int wordIndex = 0;
    public static int judgementIndex = 1;
    public static int confidenceIndex = 2;
    public static int typeIndex = 3;

    /**
     * For a JCas, writes each token and its POS, separated by a tab, to a line of temp_hedge.txt.
     * Sentences are separated by two new line characters.
     * @param jCas The JCas of the document to be annotated with hedges
     * @param sentences The sentences of the document to be annotated with hedges
     */
    private void writeFile(JCas jCas, List<Sentence> sentences) {
        try {
            FileWriter tokensWriter = new FileWriter(new File("hedgeClassifier/temp_hedge_tokens.txt"));
            FileWriter depsWriter = new FileWriter(new File("hedgeClassifier/temp_hedge_deps.txt"));
            for (Sentence sentence : sentences) {

                List<DependencyNode> nodes = JCasUtil.selectCovered(jCas, DependencyNode.class, sentence);
                if (nodes.size() == 0) // if there are no dependencies, print dummy so token and dependency sentence indices match
                    depsWriter.append("NO DEPS\n");
                for (DependencyNode node : nodes) {
                    FSArray fsarray = node.getChildRelations();
                    for (int i = 0; i < fsarray.size(); i++) {
                        DependencyRelation dependency = (DependencyRelation) fsarray.get(i);
                        DependencyNode child = dependency.getChild();
                        Token parentToken = JCasUtil.selectCovered(jCas, Token.class, node).get(0);
                        Token childToken = JCasUtil.selectCovered(jCas, Token.class, child).get(0);
                        depsWriter.append(dependency.getRelation());
                        depsWriter.append('\t');
                        depsWriter.append(parentToken.getLemma());
                        depsWriter.append('\t');
                        depsWriter.append(childToken.getLemma());
                        depsWriter.append('\t');
                        depsWriter.append(Integer.toString(parentToken.getBegin()));
                        depsWriter.append('\t');
                        depsWriter.append(Integer.toString(childToken.getBegin()));
                        depsWriter.append('\t');
                        depsWriter.append(parentToken.getPos());
                        depsWriter.append('\t');
                        depsWriter.append(childToken.getPos());
                        depsWriter.append('\n');
                    }
                }
                depsWriter.append("\n\n");

                List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
                for (Token token : tokens) {
                    if (token.getCoveredText().length() == 1 && Character.isISOControl(token.getCoveredText().charAt(0)))
                        //Had some issues with ISO character, so replace them in the txt file. The original characters
                        //are put back after running the classifier.
                        tokensWriter.append("ISO");
                    else
                        tokensWriter.append(token.getCoveredText());
                    tokensWriter.append('\t');
                    tokensWriter.append(token.getPos());
                    tokensWriter.append('\t');
                    tokensWriter.append(token.getLemma());
                    tokensWriter.append('\t');
                    //print beginning index of this token, so we can match it in dependencies
                    tokensWriter.append(Integer.toString(token.getBegin()));
                    tokensWriter.append('\n');
                }
                tokensWriter.append("\n\n");

            }
            tokensWriter.flush();
            tokensWriter.close();
            depsWriter.flush();
            depsWriter.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * For each sentence in a document, adds an association between a token and it's hedge description, only if it is
     * a hedge--- tokens that are not hedges are not added to the sentence map. Each sentence map is then added to a
     * list of hedge features. This list is returned when all sentences have been processed.
     * @param jCas The JCas of the document to be annotated with hedges
     * @return hedgeFeatures A list of sentences where each sentence is a map of tokens to hedge feature descriptions.
     */
    public List<Map<Token, HedgeInfo>> runClassifier(JCas jCas) {
        //System.out.println("Inside Hedge Classifier: runClassifier()");//to check if classifier is running
        List<Map<Token, HedgeInfo>> hedgeFeatures = new ArrayList<>();
        List<Sentence> sentences = new ArrayList<>(JCasUtil.select(jCas, Sentence.class));

        writeFile(jCas, sentences);//writes temp_hedge_tokens.txt, temp_hedge_deps.txt

        try {
            //run classifier on temp_hedge.txt
            ProcessBuilder pb = new ProcessBuilder("python", "hedgeClassifier/classify.py",
                "hedgeClassifier/temp_hedge_tokens.txt", "hedgeClassifier/temp_hedge_deps.txt");
            Process p = pb.start();
            p.waitFor();

            // read and print any errors from the classifier, exit system if there was an error
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorLine;
            boolean err = false;
            while ((errorLine = stdError.readLine()) != null) {
                System.err.println(errorLine);
                err = true;
            }
            stdError.close();
            if (err) {
                System.err.println("Error running classify.py");
                System.exit(70);
            }

            //read and process output from the classifier
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String inputLine;
            Map<Token, HedgeInfo> sentenceMap = new HashMap<>();
            List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentences.remove(0));
            List<Token> phrase = new ArrayList<>();
            int phraseIndex = 0;
            String[] phraseInfo = new String[4];//{phrase,"M", conf, type}
            phraseInfo[1] = "M";
            int t = 0;

            while ((inputLine = in.readLine()) != null ) {
                if (inputLine.equals("")) {
                    //add sentence to hedgeFeatures
                    hedgeFeatures.add(sentenceMap);
                    sentenceMap = new HashMap<>();
                    if (sentences.size() > 0) {
                        //get next sentence's tokens
                        tokens = JCasUtil.selectCovered(jCas, Token.class, sentences.remove(0));
                        t = 0;
                    }
                }
                else if (t >= tokens.size()) {
                    System.err.println("hedge classifier doesn't line up with input-- out of tokens");
                    System.exit(70);
                }
                else {
                    //read input line and process it
                    String[] line = inputLine.split("\t");
                    if (phraseIndex > 0 && !(line[judgementIndex].startsWith("M") && line[judgementIndex].endsWith("" + phraseIndex))) {
                        //last token ended a hedge phrase-- add phrase to sentenceMap
                        StringBuilder sb = new StringBuilder();
                        for (Token token : phrase) {
                            sb.append(token.getCoveredText());
                            sb.append(" ");
                        }
                        phraseInfo[0] = sb.toString();
                        for (Token token : phrase) {
                            sentenceMap.put(token, featureDescription(phraseInfo));
                        }
                        phraseIndex = 0;
                        phrase.clear();
                    }
                    if (line[judgementIndex].equals(NO) || line[confidenceIndex].equals("0")) {
                        //token is not a hedge, don't add it
                        t++;
                    }
                    else if(line[judgementIndex].startsWith("M") && line[judgementIndex].endsWith("" + phraseIndex)) {
                        //token is part of multi-word hedge--- continue/start phrase
                        phrase.add(tokens.get(t++));
                        phraseIndex++;
                        phraseInfo[2] = line[2];
                        phraseInfo[3] = line[3];
                    }
                    else {
                        //token not part of phrase-- add to sentence map
                        line[wordIndex] = tokens.get(t).getCoveredText();//get ride of ISO if present
                        sentenceMap.put(tokens.get(t++), featureDescription(line));
                    }
                }
            }
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return hedgeFeatures;
    }

    /**
     * Returns the description of a token that is a hedge, given an array with the necessary information
     * Only call for tokens that are hedges--- non-hedges will get an indexOutOfBounds exception.
     * @param line An array of the form {tokenText, judgement, confidence, type}
     * @return A String of the form type_token_judgement_confidence
     *          example: hRel_estimate_S_1
     */
    public HedgeInfo featureDescription(String[] line) {
        return new HedgeInfo(line[wordIndex], line[typeIndex], line[judgementIndex], Double.valueOf(line[confidenceIndex]));
    }


    public class HedgeInfo {
        public String type;
        public String word;
        public String judgment;
        public double confidence;

        public HedgeInfo(String word, String type, String judgment, double confidence) {
            this.word = word;
            this.type = type;
            this.judgment = judgment;
            this.confidence = confidence;
        }

    }

}
