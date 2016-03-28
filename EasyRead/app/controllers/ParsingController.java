package controllers;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import models.POS;
import models.Sentence;
import models.SimplePassage;
import models.Word;
import net.sf.extjwnl.JWNLException;

import java.util.List;
import java.util.Properties;

public class ParsingController {

    private PassageAnalysisController a = new PassageAnalysisController();
    private StanfordCoreNLP pipeline;


    private void initPipeline() {
        Properties props = new Properties();
        // do we still need all these annotators
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);

    }

    public void reviseSuggestions(){
        a.reviseSuggestions();
    }


    // code from: http://nlp.stanford.edu/software/corenlp.shtml
    /*
	 * 	Manning, Christopher D., Surdeanu, Mihai, Bauer, John, Finkel, Jenny, Bethard, Steven J., and McClosky, David. 2014. The Stanford CoreNLP Natural Language Processing Toolkit. In Proceedings of 52nd Annual Meeting of the Association for Computational Linguistics: System Demonstrations, pp. 55-60. [pdf] [bib]
	 */
    public void parse(SimplePassage passage, String t) throws JWNLException {

        //int startingCount = this.sentences.size();

        if (pipeline == null) initPipeline();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(t);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        int i = 0;

        int originalSCount =/*passage.sentences.size();*/ 0;

        if (originalSCount == 0) {
            passage.num_characters = 0;
            passage.numSyllables = 0;
        }

        passage.numWords = 0;

        long startingSentenceID = 0;
        for (Sentence s : Sentence.all()) if (s.id > startingSentenceID) startingSentenceID = s.id;

        int sentenceNum = 0;
        for (CoreMap sentence : sentences) {
            Sentence s = new Sentence(sentence.toString(), i);
            if (startingSentenceID > 0) startingSentenceID++;
            i++;


            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                // this is the POi.S tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);

                String lemma = token.originalText();
                // System.out.println("LEMMA : " + lemma);


                POS p = new POS(pos);

                if (lemma.length() > 1 || Character.isLetter(lemma.charAt(0))) {
						/*if(originalSCount == 0) {*/
                    new MashapeController().getNumSyllablesForWord(lemma, passage);


                    if(originalSCount == 0) passage.numWords++;
                    a.generateSuggestionsForWord(p, lemma, s.text, passage.id, token.originalText());
                    //}

                }

                Word thisWord = Word.byLemma(lemma);


                if (thisWord != null && thisWord.partsOfSpeech != null) {
                    thisWord.partsOfSpeech.add(p);


                    thisWord.save();


                    //}
                }

                s.words.add(thisWord);
            }

            if (originalSCount == 0) passage.sentences.add(s);
            sentenceNum++;
        }

        if(originalSCount == 0) a.determineGradeLevel(passage);
        try {
            passage.save();
        } catch (Exception e) {
            System.out.println("Couldn't Save");
        }


    }
}