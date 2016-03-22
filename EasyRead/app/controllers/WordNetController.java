package controllers;

import models.Suggestion;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.IndexWordSet;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import simplenlg.features.*;
import simplenlg.features.Feature;
import simplenlg.features.Form;
import simplenlg.features.Tense;
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


// All conversion methods based on - http://stackoverflow.com/questions/33389184/simplenlg-how-to-get-the-plural-of-a-noun

    final XMLLexicon xmlLexicon = new XMLLexicon();
    final Realiser realiser = new Realiser(xmlLexicon);

    public String convertToPP(String word) {
        WordElement wrd = xmlLexicon.getWord(word, LexicalCategory.VERB);
        InflectedWordElement verb = new InflectedWordElement(wrd);
        verb.setFeature(Feature.FORM, Form.PAST_PARTICIPLE);
        return realiser.realise(verb).toString();
    }

    public String convertToGerund(String word) {
        WordElement wrd = xmlLexicon.getWord(word, LexicalCategory.VERB);
        InflectedWordElement verb = new InflectedWordElement(wrd);
        verb.setFeature(Feature.FORM, Form.PRESENT_PARTICIPLE);
        return realiser.realise(verb).toString();
    }

    public String convertToPast(String word) {
        WordElement wrd = xmlLexicon.getWord(word, LexicalCategory.VERB);
        InflectedWordElement verb = new InflectedWordElement(wrd);
        verb.setFeature(Feature.TENSE, Tense.PAST);
        return realiser.realise(verb).toString();
    }

    public String convertToThird(String word) {
        WordElement wrd = xmlLexicon.getWord(word, LexicalCategory.VERB);
        InflectedWordElement verb = new InflectedWordElement(wrd);
        verb.setFeature(Feature.PERSON, Person.THIRD);
        return realiser.realise(verb).toString();
    }

    public String compoundWordResolution(String w){
        if(w.contains(" ")){
            w = w.split(" ")[0];
        }
        return w;

    }

    /*
         case ("VBZ"):
                return "Verb, 3rd person Singular Present";
            case ("VBP"):
                return "Verb, non 3rd person singular present";
            case ("VBN"):
                return "Verb, past participle";
            case ("VBG"):
                return "Verb, gerund or present participle";
            case ("VBD"):
                return "Verb, past tense";
            case ("VB"):
                return "Verb, base form";
     */


    public String realizeVerb(String w, String pos){
        String correct;
        if(pos.contains("present participle")){
            correct = convertToGerund(w);
        } else if(pos.contains("past participle")){
            correct = convertToPP(w);
        } else if(pos.contains("past tense")){
            correct = convertToPast(w);
        } else if(pos.equals("3rd person Singular Present")){
            correct = convertToThird(w);
        } else{
            correct = w;
        }

        System.out.println(correct);
        return correct;
    }

    public String realizeNoun(String w, String pos){
        String correct = w;
        if(pos.contains("plural")) {
            WordElement word = xmlLexicon.getWord(w, LexicalCategory.NOUN);
            InflectedWordElement pluralWord = new InflectedWordElement(word);
            pluralWord.setPlural(true);
            correct = realiser.realiseSentence(pluralWord).toString();
        }
        return correct;
    }


    public HashSet<String> synonymnLookup(String word, String pos) throws JWNLException {

        if (Suggestion.byWord(word).size() == 0) {

            //correct POS


            // https://code.google.com/p/simplenlg/wiki/Section6
            //http://stackoverflow.com/questions/9520501/how-do-you-get-the-past-tense-of-a-verb

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
                        String str = compoundWordResolution(wordNetWord.getLemma());
                        if(realPOS == POS.VERB) syn.add(realizeVerb(str, pos));
                        else if(realPOS == POS.NOUN){
                           syn.add(realizeNoun(str,pos));
                        }
                        else{
                            syn.add(wordNetWord.getLemma());
                        }
                    }
                }
            }
            return syn;
        }
        return null;
    }


}