package controllers;

import models.Suggestion;

/**
 * Interface to allow for more advanced N-Gram Checking to be Added to EasyRead Later
 * A Phrase Validator Object needs to be able to get the frequencies of various n-grams
 * and check that against the frequency of the original word
 * The methods are void and imply that the Suggestion object will be updated in the Validator's methods
 */
public interface PhraseValidator {

    /**
     * This method gets the frequency of the three-gram for the Suggestion object
     * @param s Suggestion object that needs its frequency filled in
     * @param p 3-gram who's frequency will be added to the Suggestion object's frequency field
     */
    void fetchFrequencies(Suggestion s, String p);

    /**
     * This method zeroes out the frequency field of the Suggestion object for suggestions
     * that form less frequently used phrases than the original
     * @param s Suggestion object holding the suggested word and the original
     * @param p String representing the three gram
     */
    void checkOriginal(Suggestion s, String p);
}