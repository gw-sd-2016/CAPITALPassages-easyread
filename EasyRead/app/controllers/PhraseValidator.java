package controllers;

import models.Suggestion;

public interface PhraseValidator {


    // Can check the n-gram frequency of any -gram related to the -suggested-word_ field of the Suggestion Object
    void fetchFrequencies(Suggestion s, String p);

    // Checks if the fequency of the Suggestion object's suggested_word is greater than that of the original word
    void checkOriginal(Suggestion s, String p);


}
