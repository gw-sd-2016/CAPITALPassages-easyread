package controllers;

import models.Suggestion;

/**
 * Interface to allow for more advanced N-Gram Checking to be Added to EasyRead Later
 * A Phrase Validator Object needs to be able to get the frequencies of various n-grams
 * and check that against the frequency of the original word
 * The methods are void and imply that the Suggestion object will be updated in the Validator's methods
 */
public interface PhraseValidator {

    // Can check the n-gram frequency of any -gram related to the -suggested-word_ field of the Suggestion Object
    void fetchFrequencies(Suggestion s, String p);

    // Checks if the fequency of the Suggestion object's suggested_word is greater than that of the original word
    void checkOriginal(Suggestion s, String p);
}