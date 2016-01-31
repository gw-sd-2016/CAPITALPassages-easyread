package controllers;

import models.Suggestion;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.IndexWordSet;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import simplenlg.features.Feature;
import simplenlg.features.Form;
import simplenlg.framework.InflectedWordElement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.WordElement;
import simplenlg.lexicon.XMLLexicon;
import simplenlg.realiser.english.Realiser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A class to demonstrate the functionality of the library.
 * CODE FROM http://extjwnl.sourceforge.net
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class WordNetController {

    private static final String USAGE = "Usage: Examples [properties file]";
    private static final Set<String> HELP_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "--help", "-help", "/help", "--?", "-?", "?", "/?"
    )));

	/* public static void main(String[] args) throws FileNotFoundException, JWNLException, CloneNotSupportedException {
        Dictionary dictionary = null;
        if (args.length != 1) {
            dictionary = Dictionary.getDefaultResourceInstance();
        } else {
            if (HELP_KEYS.contains(args[0])) {
                System.out.println(USAGE);
            } else {
                FileInputStream inputStream = new FileInputStream(args[0]);
                dictionary = Dictionary.getInstance(inputStream);
            }
        }

        if (null != dictionary) {
            new WordNetController(dictionary).go();
        }
    }*/

    public WordNetController() throws JWNLException {
        this.dictionary = Dictionary.getDefaultResourceInstance();
    }


    private IndexWord ACCOMPLISH;
    private IndexWord DOG;
    private IndexWord CAT;
    private IndexWord FUNNY;
    private IndexWord DROLL;
    private final static String MORPH_PHRASE = "running-away";
    private final Dictionary dictionary;


    public HashSet<String> wordIDLookup(String word, String pos) throws JWNLException {
        //correct POS

        POS realPOS;

        pos = pos.toLowerCase();

        if (pos.contains("verb")) realPOS = POS.VERB;
        else if (pos.contains("adverb")) realPOS = POS.ADVERB;
        else if (pos.contains("adjecive")) realPOS = POS.ADJECTIVE;
        else realPOS = POS.NOUN;

        IndexWordSet newWord = this.dictionary.lookupAllIndexWords(word);

        HashSet<String> syn = new HashSet<String>();


        IndexWord w = newWord.getIndexWord(realPOS);

        if (w != null) {
            for (long offset : w.getSynsetOffsets()) {
                for (Word wordNetWord : dictionary.getSynsetAt(realPOS, offset).getWords()) {
                    //System.out.println(wordNetWord.getLemma());
                    syn.add(wordNetWord.getLemma());


                }

            }
        }


        return syn;
    }


    public String convertToGerund(String word) {
        final XMLLexicon xmlLexicon = new XMLLexicon();
        final WordElement wrd = xmlLexicon.getWord(word, LexicalCategory.VERB);
        final InflectedWordElement pluralWord = new InflectedWordElement(wrd);
        pluralWord.setFeature(Feature.FORM, Form.PRESENT_PARTICIPLE);
        final Realiser realiser = new Realiser(xmlLexicon);
        return realiser.realise(pluralWord).toString();
    }


    public HashSet<String> synonymnLookup(String word, String pos) throws JWNLException {

        if (Suggestion.byWord(word).size() == 0) {
/*

			//correct POS


			// https://code.google.com/p/simplenlg/wiki/Section6
			//http://stackoverflow.com/questions/9520501/how-do-you-get-the-past-tense-of-a-verb

			POS realPOS;

			pos = pos.toLowerCase();


			ArrayList<Object> features = new ArrayList<Object>();

			if (pos.contains("verb")) {
				realPOS = POS.VERB;


				//p.realise();


				if (pos.contains("past")) {
					p.setFeature(Feature.TENSE, Tense.PAST);
				}


				if (word.contains("ing")) {
					p.setFeature(Feature.PROGRESSIVE, true);
				} else {
					p.setFeature(Feature.PROGRESSIVE, false);
				}


				Object c = p.getFeature(Feature.PROGRESSIVE);

				Object d = p.getFeature(Feature.TENSE);


				if (word.charAt(word.length() - 1) != 's') {
					p.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
				} else {
					p.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
				}

			/*Object e = p.getFeature(Feature.NUMBER);

			Object f = p.getFeature(Feature.PERSON);

			Object g = p.getFeature(Feature.FORM);*/
/*
                features.add(c);
				features.add(d);
			/*features.add(e);
			features.add(f);
			features.add(g);
			} else if (pos.contains("adverb")) realPOS = POS.ADVERB;
			else if (pos.contains("adjecive")) realPOS = POS.ADJECTIVE;
			else realPOS = POS.NOUN;

			IndexWordSet newWord = this.dictionary.lookupAllIndexWords(word);

			HashSet<String> syn = new HashSet<String>();


			IndexWord w = newWord.getIndexWord(realPOS);


			Lexicon lexicon = Lexicon.getDefaultLexicon();

			if (w != null) {
			/*for(long offset : w.getSynsetOffsets()){
				for(Word wordNetWord : dictionary.getSynsetAt(realPOS, offset).getWords()){*/


            //if(!correct.equals(word)) syn.add(correct);


            //}

            //}
        }


        return null;

    }
    //}


}