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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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


}