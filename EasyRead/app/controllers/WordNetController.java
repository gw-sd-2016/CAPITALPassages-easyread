package controllers;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.relationship.AsymmetricRelationship;
import net.sf.extjwnl.data.relationship.Relationship;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;
import net.sf.extjwnl.dictionary.Dictionary;

import simplenlg.framework.*;
import simplenlg.lexicon.*;
import simplenlg.realiser.english.*;
import simplenlg.phrasespec.*;
import simplenlg.features.*;


import java.util.*;

/**
 * A class to demonstrate the functionality of the library.
 * CODE FROM http://extjwnl.sourceforge.net
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

	public WordNetController() throws JWNLException{
		this.dictionary = Dictionary.getDefaultResourceInstance();
	}


	private IndexWord ACCOMPLISH;
	private IndexWord DOG;
	private IndexWord CAT;
	private IndexWord FUNNY;
	private IndexWord DROLL;
	private final static String MORPH_PHRASE = "running-away";
	private final Dictionary dictionary;




	public HashSet<String> wordIDLookup(String word, String pos) throws JWNLException{
		//correct POS

		POS realPOS; 

		pos = pos.toLowerCase();

		if(pos.contains("verb")) realPOS = POS.VERB;
		else if(pos.contains("adverb")) realPOS = POS.ADVERB;
		else if(pos.contains("adjecive")) realPOS = POS.ADJECTIVE;
		else realPOS = POS.NOUN;

		IndexWordSet newWord = this.dictionary.lookupAllIndexWords(word);

		HashSet<String> syn = new HashSet<String>();


		IndexWord w = newWord.getIndexWord(realPOS);

		if(w != null){
			for(long offset : w.getSynsetOffsets()){
				for(Word wordNetWord : dictionary.getSynsetAt(realPOS, offset).getWords()){
					//System.out.println(wordNetWord.getLemma());
					syn.add(wordNetWord.getLemma());



				}

			}
		}



		return syn;
	}

	public HashSet<String> synonymnLookup(String word, String pos) throws JWNLException{
		//correct POS


		// https://code.google.com/p/simplenlg/wiki/Section6
		//http://stackoverflow.com/questions/9520501/how-do-you-get-the-past-tense-of-a-verb

		POS realPOS;

		pos = pos.toLowerCase();


		ArrayList<Object> features = new ArrayList<Object>();

		if(pos.contains("verb")) {
			realPOS = POS.VERB;

			SPhraseSpec p = new NLGFactory().createClause();
			//p.setSubject("Mary");
			p.setVerb(word);
			//p.setObject("the monkey");


			if(pos.contains("past")){
				p.setFeature(Feature.TENSE, Tense.PAST);
			}


			if(word.contains("ing")){
				p.setFeature(Feature.PROGRESSIVE, true);
			} else {
				p.setFeature(Feature.PROGRESSIVE, false);
			}


			Object c = p.getFeature(Feature.PROGRESSIVE);

			Object d = p.getFeature(Feature.TENSE);


			if(word.charAt(word.length() - 1) != 's'){
				p.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
			} else {
				p.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
			}

			/*Object e = p.getFeature(Feature.NUMBER);

			Object f = p.getFeature(Feature.PERSON);

			Object g = p.getFeature(Feature.FORM);*/

			features.add(c);
			features.add(d);
			/*features.add(e);
			features.add(f);
			features.add(g);*/
		}
		else if(pos.contains("adverb")) realPOS = POS.ADVERB;
		else if(pos.contains("adjecive")) realPOS = POS.ADJECTIVE;
		else realPOS = POS.NOUN;

		IndexWordSet newWord = this.dictionary.lookupAllIndexWords(word);

		HashSet<String> syn = new HashSet<String>();


		IndexWord w = newWord.getIndexWord(realPOS);


		Lexicon lexicon = Lexicon.getDefaultLexicon();

		if(w != null){
			for(long offset : w.getSynsetOffsets()){
				for(Word wordNetWord : dictionary.getSynsetAt(realPOS, offset).getWords()){
					WordElement wor = lexicon.getWord(wordNetWord.getLemma(), LexicalCategory.VERB);
					InflectedWordElement infl = new InflectedWordElement(wor);

					if(features.size() > 0){
						infl.setFeature(Feature.PROGRESSIVE, features.get(0));
						infl.setFeature(Feature.TENSE, features.get(1));
						/*infl.setFeature(Feature.NUMBER, features.get(2));
						infl.setFeature(Feature.PERSON, features.get(2));
						infl.setFeature(Feature.FORM, features.get(3));*/
					}

					Realiser realiser = new Realiser(lexicon);
					String correct = realiser.realise(infl).getRealisation();
					System.out.println(correct);

					if(!correct.equals(word)) syn.add(correct);


				}

			}
		}



		return syn;
	}


}