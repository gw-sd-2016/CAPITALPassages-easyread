package controllers;

import models.*;
import net.sf.extjwnl.JWNLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PassageAnalysisController {

    private PhraseValidator tertiaryValidator = new GoogleNGramsValidator();
    private WordNetController c;


    /**
     * This method equates an age to a grade level using Grade = Age - 6
     * @param age Double representing Age to be converted
     * @return Double representing grade level
     */
    public double convertToGrade(double age) {
        if (age <= 6) return 0;
        else if (age > 18) return 13;
        return age - 6;
    }

    /**
     * This method equates an age to a grade level using Kindergarten as the base at age 6
     * Ages after highschool are scaled to College
     * Ages before completion of Kindergarten are scaled to Kindergarten
     * @param age Double representing Age to be converted
     * @return String representing Readable grade level for use in front end
     */
    public static String toGrade(double age){
        if (age <= 6) return "K";
        else if (age > 18) return "College";
        return String.valueOf((int)age - 6);
    }

    /**
     * This method outputs a String representation of the Grade Level
     * This method produced a more readable K or College rather than Grade 0 or Grade 13
     * @param grade Grade to convert
     * @return String object represented readable grade level
     */
    public static String displayGrade(int grade){
        if (grade == 0) return "K";
        else if (grade == 13) return "College";
        return String.valueOf(grade);
    }

    /**
     * This method scales any grade levels above 12 or below 0 to the appropriate range
     * This is a helper method to implement bounds checking for grades that may be produced in error
     * @param grade Double representing the grade level to be checked
     * @return
     */
    public double gradeResolution(double grade) {
        if (grade > 12) return 13;
        else if(grade < 0) return 0;
        else return grade;
    }

    /**
     * Computes the average age of acquisition for all words in the Passage that are in the database
     * Age of Acquisition is the age at which the average US student learns a word
     * @param p Passage Object to be analyzed
     * @return Returns a double representing the average Age of Acquisition of the words in the input String
     */
    public double averageAge(SimplePassage p) {
        double average = 0;
        int count = 0;
        for (Sentence s : p.sentences) {
            for (String x : s.text.split(" ")) {
                Word w = Word.byRawString(x);
                if (w != null && !Word.isStopWord(w)) {
                    average += w.ageOfAcquisition;
                    count++;

                }
            }
        }


        return (count > 0) ? average / count : 0;
    }

    /**
     * Computes the average age of acquisition for all words in the String that are in the database
     * Age of Acquisition is the age at which the average US student learns a word
     * @param p String to be analyzed
     * @return Returns a double representing the average Age of Acquisition of the words in the input String
     */
    public double averageAgeForRange(String p) {
        double average = 0;
        int count = 0;

        for (String x : p.split(" ")) {
            Word w = Word.byRawString(x);
            if (w != null && !Word.isStopWord(w)) {
                average += w.ageOfAcquisition;
                count++;

            }
        }
        return (count > 0) ? average / count : 0;
    }

    /**
     * Computes the average age of acquisition for all words in the input String that are in the database
     * Age of Acquisition is the age at which the average US student learns a word
     * Age of Acquisition + 6 is the Grade Level
     * @param p String to be analyzed
     * @return Returns a Double representing the average US grade level of the input String
     */
    public double determineGradeLevelForString(String p){
        int combined = 0;
        int numAlgorithms = 0;

        double ARI = calculateARI(p);
        if (ARI > 0 && ARI < 14) {
            combined += ARI;
            numAlgorithms++;
        }

        double FK = calculateFKScore(p);
        if (FK > 0 && FK < 14) {
            combined += FK;
            numAlgorithms++;
        }

        return (combined + convertToGrade(averageAgeForRange(p))) / (numAlgorithms + 1);
    }

    /**
     * Computes the average age of acquisition for all words in the Passage that are in the database
     * Age of Acquisition is the age at which the average US student learns a word
     * Age of Acquisition + 6 is the Grade Level
     * After analysis the grade level of the input passage object is updated
     * @param p Passage Object to be analyzed
     */
    public void determineGradeLevel(SimplePassage p) {
        int combined = 0;
        int numAlgorithms = 0;

        double ARI = calculateARI(p);
        if (ARI > 0 && ARI < 14) {
            combined += ARI;
            numAlgorithms++;
        }

        double FK = calculateFKScore(p);
        if (FK > 0 && FK < 14) {
            combined += FK;
            numAlgorithms++;
        }

        if (p.text.split(" ").length > 100) {
            double CL = gradeResolution(calculateCLScore(p.text, p.numWords));
            if (CL > 0 && CL < 14) {
                combined += CL;
                numAlgorithms++;
            }

            if (numAlgorithms > 0) p.grade = Math.round(combined / numAlgorithms);
            else p.grade = 0;
        }

        p.grade = (int) Math.round((combined + convertToGrade(averageAge(p))) / (numAlgorithms + 1));

        try {
            p.save();
        } catch (Exception e) {

        }
    }

    /**
     * Calculated the US grade level of the input text for Strings of 100 words of longer according to the Coleman-Liau Index
     * https://en.wikipedia.org/wiki/Coleman%E2%80%93Liau_index
     * @param text String of text to be analyzed
     * @param numWords number of words in the input String
     * @return double representing the US grade level of the input passage if it's at least 100 words in length
     */
    public double calculateCLScore(String text, int numWords) {
        String[] words = text.split(" ");

        int letterCounter = 0;
        int sentenceCounter = 0;
        int counter = 0;

        ArrayList<Integer> letterAveragesPer100 = new ArrayList<Integer>();
        ArrayList<Integer> sentenceAveragesPer100 = new ArrayList<Integer>();

        for (String w : words) {
            if (w.contains(".")) sentenceCounter++;
            letterCounter += w.length();
            counter++;
            if (counter == 100 && numWords > 99) {
                counter = 0;
                letterAveragesPer100.add(letterCounter);
                sentenceAveragesPer100.add(sentenceCounter);
                letterCounter = 0;
                sentenceCounter = 0;

            } else {
                letterAveragesPer100.add(letterCounter);
                sentenceAveragesPer100.add(sentenceCounter);
            }
        }

        int l = 0;
        for (int i : letterAveragesPer100) {
            l += i;
        }
        l = l / letterAveragesPer100.size();

        int s = 0;
        for (int i : sentenceAveragesPer100) {
            s += i;
        }
        s = l / sentenceAveragesPer100.size();

        double result = (.0588 * Double.valueOf(l)) - (.296 * Double.valueOf(s)) - 15.8;

        System.out.println("CLI = " + result);

        return result;
    }


    /**
     * Calculates a US grade level for the Passage according to the Automated Readability Index
     * https://en.wikipedia.org/wiki/Automated_readability_index
     * @param p Passage object who's text will be analyzed
     * @return Returns a Double representing the US grade level of the input passage
     */
    public double calculateARI(SimplePassage p) {
        double sentences = Double.valueOf(p.sentences.size());

        double ARI = 4.71 * (Double.valueOf(p.num_characters) / Double.valueOf(p.numWords)) + .5 * (Double.valueOf(p.numWords) / sentences) - 21.43;

        System.out.println("ARI = " + ARI);

        return ARI;
    }

    /**
     * Calculates a US Grade level according to the Automated Readability Test
     * https://en.wikipedia.org/wiki/Automated_readability_index
     * @param p String of text to be analyzed
     * @return Returns a double represeting the US grade level of the input String
     */
    public double calculateARI(String p) {
        double numWords = p.split(" ").length;

        double sentences = Double.valueOf(p.split(" ").length);

        double ARI = 4.71 * (Double.valueOf(p.replace(" ", "").toCharArray().length) / numWords) + .5 * (numWords / sentences) - 21.43;

        System.out.println("ARI = " + ARI);

        return ARI;
    }

    /**
     * Calculates a US Grade level for the Passage according to the Flesch Kincaid Readability Test
     * https://en.wikipedia.org/wiki/Flesch%E2%80%93Kincaid_readability_tests
     * @param p Passage Object who's text needs to be analyzed
     * @return Returns a double representing the US grade level of the input Passage
     */
    public double calculateFKScore(SimplePassage p) {
        double sentences = Double.valueOf(p.sentences.size());

        double score = (.39 * (Double.valueOf(p.numWords) / sentences)) + (11.8 * (Double.valueOf(p.numSyllables) / Double.valueOf(p.numWords))) - 15.59;

        System.out.println("FK Score: " + score);

        return score;
    }

    /**
     * Calculates the Grade level for the text according to the Flesch Kincaid Readability Test
     * https://en.wikipedia.org/wiki/Flesch%E2%80%93Kincaid_readability_tests
     * @param p String of text to be analyzed
     * @return Returns a double representing the US grade level of the input String
     */
    public double calculateFKScore(String p) {
        double numWords = Double.valueOf(p.split(" ").length);
        double sentences = Double.valueOf(p.split(".").length);

        double numSyllables = 0;
        for(String s : p.split(" ")){
            Word w = Word.byRawString(s);
            if(w != null){
                numSyllables += w.numSyllables;
            }
        }

        double score = (.39 * (numWords) / sentences) + (11.8 * (Double.valueOf(numSyllables) /numWords)) - 15.59;

        System.out.println("FK Score: " + score);

        return score;
    }

    // Save Calculated grade levels to prevent duplicate requests
    private HashMap<String, Double> foundGradeLevels;

    /**
     *
     * @param oldSentence String representing the original sentence from the text where the suggestion is needed
     * @param word String representing the word that the Suggestions is for
     * @param sugg String representing the suggested word being checked
     * @return true if the Sentence with the suggestion is not more difficult than the original
     */
    public boolean suggestionIsGood(String oldSentence, String word, String sugg){
        if(foundGradeLevels == null) foundGradeLevels = new HashMap<String, Double>();
        double diffOne;


        if(foundGradeLevels.containsKey(oldSentence)){
            diffOne = foundGradeLevels.get(oldSentence);
        } else{
            diffOne = determineGradeLevelForString(oldSentence);
            foundGradeLevels.put(oldSentence, diffOne);
        }

        double diffTwo;

        if(foundGradeLevels.containsKey(oldSentence.replace(word, sugg))){
            diffTwo = foundGradeLevels.get(oldSentence.replace(word, sugg));
        } else {
            diffTwo =  determineGradeLevelForString(oldSentence.replace(word, sugg));
            foundGradeLevels.put(oldSentence.replace(word, sugg), diffTwo);
        }

        return diffTwo < diffOne;
    }


    // Save Suggestions for each sentence that have already been found to avoid duplicate requests
    public HashMap<Suggestion, String> suggestionMapping;

    /**
     *
     * @param p POS object representing the Part of Speech of the word
     * @param word String representing the word to get suggestions for
     * @param sentence String representing the sentence from the passage where the word is used
     * @param passageId Id of the passage object that this word comes from
     * @param ogText String of the original - un lematized text - from the passage where the word was used
     * @throws JWNLException Exception thrown if there is a problem fetching synonyms from WordNet
     */
    public void generateSuggestionsForWord(POS p, String word, String sentence, Long passageId, String ogText) throws JWNLException {

        if(suggestionMapping == null) suggestionMapping = new HashMap<Suggestion, String>();

        if (p != null && POS.isSignificant(p.name) && Word.byLemma(word) != null && !Word.isStopWord(Word.byLemma(word)) || Word.byLemma(word) != null && !Word.isStopWord(Word.byLemma(word))) {
            if (c == null) c = new WordNetController();

            Word w = Word.byLemma(word);

            //Only get Suggestions for words than have none
            if (Suggestion.byWord(word).size() == 0) {

                HashSet<String> suggestions = c.synonymnLookup(word, p.name);

                for (String root : suggestions) {
                    Word r = Word.byLemma(root);
                    if (!word.toLowerCase().equals(root.toLowerCase())
                            && w != null
                            && r != null
                            && !isProperNoun(w)) {

                        // if the suggestion is not for a proper noun or stop word or more difficult than the original
                        if(suggestionIsGood(sentence, ogText, root)){
                            Suggestion s = new Suggestion();
                            s.word = ogText;

                            String threeGram = "";
                            String[] splitS = sentence.split(" ");

                            String iPOS = "";
                            s.suggestedWord = root;
                            threeGram = buildThreeGram(splitS, sentence, ogText, root);
                            s.simple_passage_id = passageId;
                            s.save();

                            tertiaryValidator.fetchFrequencies(s, threeGram);
                            suggestionMapping.put(s, threeGram);
                        }

                    }

                }
            }
        }
    }


    /**
     * This method decides whether or not a Word can be used as Proper Noun
     * @param w Word object representing the word to check
     * @return Returns true if the Word can ever be used as a proper noun
     */
    private boolean isProperNoun(Word w) {
        for (POS p : w.partsOfSpeech) {
            if (p.name.indexOf("Proper Noun") != -1) return true;
        }
        return false;
    }

    /**
     * Constructs a Three-Gram from the sentence around the Suggested word
     * A three gram is a series of three words to send to an N-Grams frequency validator
     * If the sentence has less than three words, the maximum number of words are used
     * @param splitS Array of Strings from the sentence
     * @param sentence String representing full sentence
     * @param ogText String representing the original sentence without changes
     * @param root String representing the Suggested word
     * @return String representing the 3-gram
     */
    private String buildThreeGram(String[] splitS, String sentence, String ogText, String root) {
        String threeGram = "";
        if (splitS.length <= 3) threeGram = sentence;
        else {
            for (int i = 0; i < splitS.length; i++) {
                if (splitS[i].toLowerCase().equals(ogText.toLowerCase())) {
                    if (i > 0 && (i + 1) < splitS.length) {
                        threeGram = splitS[i - 1] + " " + root + " " + splitS[i + 1];
                        break;
                    } else if (i > 0) {
                        if (i - 2 > 0) {
                            threeGram = splitS[i - 2] + " ";
                        }
                        threeGram += splitS[i - 1] + " " + root;
                        break;
                    } else if ((i + 1) < splitS.length) {
                        threeGram += root + " " + splitS[i + 1];
                        if ((i + 2) < splitS.length) {
                            threeGram += splitS[i + 2];
                        }
                        break;
                    }
                }
            }
        }

        int lastIndex = threeGram.length() - 1;

        for(int i = threeGram.length() - 1; i >= 0; i--){
            if(!Character.isLetter(threeGram.charAt(i))){
                lastIndex--;
            } else break;
        }

        if(lastIndex != -1) threeGram = threeGram.substring(0, lastIndex + 1);

        return threeGram;
    }

    /**
     * This method sets  Suggestions where the suggested phrase is less common than the original phrase
     * to a frequency of 0.
     */
    public void reviseSuggestions(){
        if(suggestionMapping != null){
            for(Suggestion s: suggestionMapping.keySet()){
                tertiaryValidator.checkOriginal(s, suggestionMapping.get(s));
            }
        }
    }
}
