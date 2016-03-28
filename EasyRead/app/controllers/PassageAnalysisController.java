package controllers;

import models.*;
import net.davidashen.text.Hyphenator;
import net.sf.extjwnl.JWNLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PassageAnalysisController {

    private PhraseValidator secondaryValidator = new MashapeController();
    private GoogleNGramsValidator tertiaryValidator = new GoogleNGramsValidator();
    private WordNetController c;


    public double convertToGrade(double age) {
        if (age <= 6) return 0;
        else if (age > 18) return 13;
        return age - 6;
    }

    public static String toGrade(double age){
        if (age <= 6) return "K";
        else if (age > 18) return "College";
        return String.valueOf((int)age - 6);
    }

    public static String displayGrade(int grade){
        if (grade == 0) return "K";
        else if (grade == 13) return "College";
        return String.valueOf(grade);
    }

    public double gradeResolution(double grade) {
        if (grade > 12) return 13;
        else return grade;
    }

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


    public double calculateARI(SimplePassage p) {
        double sentences = Double.valueOf(p.sentences.size());

        double ARI = 4.71 * (Double.valueOf(p.num_characters) / Double.valueOf(p.numWords)) + .5 * (Double.valueOf(p.numWords) / sentences) - 21.43;

        System.out.println("ARI = " + ARI);

        return ARI;
    }


    public double calculateARI(String p) {


        double numWords = p.split(" ").length;

        double sentences = Double.valueOf(p.split(" ").length);

        double ARI = 4.71 * (Double.valueOf(p.replace(" ", "").toCharArray().length) / numWords) + .5 * (numWords / sentences) - 21.43;

        System.out.println("ARI = " + ARI);

        return ARI;
    }

    public double calculateFKScore(SimplePassage p) {
        double sentences = Double.valueOf(p.sentences.size());

        double score = (.39 * (Double.valueOf(p.numWords) / sentences)) + (11.8 * (Double.valueOf(p.numSyllables) / Double.valueOf(p.numWords))) - 15.59;

        System.out.println("FK Score: " + score);

        return score;
    }


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

    // at least 30 sentences
    public double calculateSMOGIndex(SimplePassage p) {
        double words = p.numWords;
        double sentences = p.sentences.size();

        double polysyllables = 0;

		/*for(Sentence s : p.sentences){
            for(Word w : s.words){
				if(w.numSyllables > 2) polysyllables++;
			}
		}*/

        return 1.0430 * Math.sqrt(polysyllables * (30 / sentences)) + 3.1291;
    }



    public boolean suggestionIsGood(String oldSentence, String word, String sugg){
        double diffOne = determineGradeLevelForString(oldSentence);
        double diffTwo = determineGradeLevelForString(oldSentence.replace(word, sugg));

        return diffTwo < diffOne;
    }


    public HashMap<Suggestion, String> suggestionMapping;

    public void generateSuggestionsForWord(POS p, String word, String sentence, Long passageId, String ogText) throws JWNLException {

        if(suggestionMapping == null) suggestionMapping = new HashMap<Suggestion, String>();

        if (p != null && POS.isSignificant(p.name)) {
            if (c == null) c = new WordNetController();

            Word w = Word.byLemma(word);


            if (Suggestion.byWord(word).size() == 0) {

                HashSet<String> suggestions = c.synonymnLookup(word, p.name);


                for (String root : suggestions) {

                    Word r = Word.byLemma(root);
                    if (!word.toLowerCase().equals(root.toLowerCase())
                            && w != null
                            && r != null
                            && !isProperNoun(w)) {



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


                            //validator.fetchFrequencies(s, threeGram.toLowerCase());
                            tertiaryValidator.fetchFrequencies(s, threeGram);
                            suggestionMapping.put(s, threeGram);

                           // tertiaryValidator.checkOriginal(s, threeGram);
                        }

                    }

                }
            }
        }


    }


    private boolean isProperNoun(Word w) {

        for (POS p : w.partsOfSpeech) {
            if (p.name.indexOf("Proper Noun") != -1) return true;
        }
        return false;
    }

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
        return threeGram;
    }


    public void reviseSuggestions(){

       // while(tertiaryValidator.count.size() < suggestionMapping.keySet().size());

        if(suggestionMapping != null){
            for(Suggestion s: suggestionMapping.keySet()){
                tertiaryValidator.checkOriginal(s, suggestionMapping.get(s));
            }
        }

    }
}
