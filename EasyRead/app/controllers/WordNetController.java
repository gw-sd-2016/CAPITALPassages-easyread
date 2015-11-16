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

	public WordNetController(Dictionary dictionary) throws JWNLException {
		this.dictionary = dictionary;
		ACCOMPLISH = dictionary.getIndexWord(POS.VERB, "accomplish");
		DOG = dictionary.getIndexWord(POS.NOUN, "dog");
		CAT = dictionary.lookupIndexWord(POS.NOUN, "cat");
		FUNNY = dictionary.lookupIndexWord(POS.ADJECTIVE, "funny");
		DROLL = dictionary.lookupIndexWord(POS.ADJECTIVE, "droll");


	}

	public HashSet<String> wordIDLookup(String word) throws JWNLException{
		IndexWordSet newWord = this.dictionary.lookupAllIndexWords(word);

		HashSet<String> syn = new HashSet<String>();

		for(IndexWord w : newWord.getIndexWordCollection()){
			long[] synonymOffsets = w.getSynsetOffsets();


			for(long offset : synonymOffsets){
				for(Word wordNetWord : dictionary.getSynsetAt(w.getPOS(), offset).getWords()){
					//System.out.println(wordNetWord.getLemma());
					syn.add(wordNetWord.getLemma());
				}
			}	
		}
		return syn;
	}


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

	public void go() throws JWNLException, CloneNotSupportedException {
		demonstrateMorphologicalAnalysis(MORPH_PHRASE);
		demonstrateListOperation(ACCOMPLISH);
		demonstrateTreeOperation(DOG);
		demonstrateAsymmetricRelationshipOperation(DOG, CAT);
		demonstrateSymmetricRelationshipOperation(FUNNY, DROLL);
	}

	private void demonstrateMorphologicalAnalysis(String phrase) throws JWNLException {
		// "running-away" is kind of a hard case because it involves
		// two words that are joined by a hyphen, and one of the words
		// is not stemmed. So we have to both remove the hyphen and stem
		// "running" before we get to an entry that is in WordNet
		System.out.println("Base form for \"" + phrase + "\": " +
				dictionary.lookupIndexWord(POS.VERB, phrase));
	}

	private void demonstrateListOperation(IndexWord word) throws JWNLException {
		// Get all of the hypernyms (parents) of the first sense of <var>word</var>
		PointerTargetNodeList hypernyms = PointerUtils.getDirectHypernyms(word.getSenses().get(0));
		System.out.println("Direct hypernyms of \"" + word.getLemma() + "\":");
		hypernyms.print();
	}

	private void demonstrateTreeOperation(IndexWord word) throws JWNLException {
		// Get all the hyponyms (children) of the first sense of <var>word</var>
		PointerTargetTree hyponyms = PointerUtils.getHyponymTree(word.getSenses().get(0));
		System.out.println("Hyponyms of \"" + word.getLemma() + "\":");
		hyponyms.print();
	}

	private void demonstrateAsymmetricRelationshipOperation(IndexWord start, IndexWord end) throws JWNLException, CloneNotSupportedException {
		// Try to find a relationship between the first sense of <var>start</var> and the first sense of <var>end</var>
		RelationshipList list = RelationshipFinder.findRelationships(start.getSenses().get(0), end.getSenses().get(0), PointerType.HYPERNYM);
		System.out.println("Hypernym relationship between \"" + start.getLemma() + "\" and \"" + end.getLemma() + "\":");
		for (Object aList : list) {
			((Relationship) aList).getNodeList().print();
		}
		System.out.println("Common Parent Index: " + ((AsymmetricRelationship) list.get(0)).getCommonParentIndex());
		System.out.println("Depth: " + list.get(0).getDepth());
	}

	private void demonstrateSymmetricRelationshipOperation(IndexWord start, IndexWord end) throws JWNLException, CloneNotSupportedException {
		// find all synonyms that <var>start</var> and <var>end</var> have in common
		RelationshipList list = RelationshipFinder.findRelationships(start.getSenses().get(0), end.getSenses().get(0), PointerType.SIMILAR_TO);
		System.out.println("Synonym relationship between \"" + start.getLemma() + "\" and \"" + end.getLemma() + "\":");
		for (Object aList : list) {
			((Relationship) aList).getNodeList().print();
		}
		System.out.println("Depth: " + list.get(0).getDepth());
	}
}