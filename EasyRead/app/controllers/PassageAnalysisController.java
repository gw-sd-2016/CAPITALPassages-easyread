package controllers;

import models.*;
import net.sf.extjwnl.JWNLException;

import java.util.ArrayList;
import java.util.HashSet;

public class PassageAnalysisController {
	
	
	private PhraseValidator validator = new MicrosoftNGramsValidator();
	private WordNetController c;
	
	
	
	public double convertToGrade(double age){
		if(age <= 6) return 0;
		else if(age > 18) return 13; 
		return age - 6; 
	}

	public double gradeResolution(double grade){
		if(grade > 12) return 13; 
		else return grade;
	}

    public double averageAge(SimplePassage p){
        double average = 0;
		int count = 0;
        for(Sentence s : p.sentences){
            for(String x : s.text.split(" ")){
                Word w = Word.byLemma(x);
				if(w != null && !Word.isStopWord(w)){

						average += w.ageOfAcquisition;
						count++;

				}
            }
        }
        return average/count;
    }

	public void determineGradeLevel(SimplePassage p){

		int combined = 0;
        int numAlgorithms = 0;

		double ARI = calculateARI(p);
        if(ARI > 0){
            combined += ARI;
            numAlgorithms++;
        }

		double FK  = calculateFKScore(p);
        if(FK > 0){
            combined += FK;
            numAlgorithms++;
        }





		if(p.text.split(" ").length > 100){

            double CL = gradeResolution(calculateCLScore(p.text, p.numWords));
            if(CL > 0){
                combined += CL;
                numAlgorithms++;
            }

            p.grade = (int) Math.round(combined / numAlgorithms);
		} else{
            p.grade = (int) Math.round((combined + convertToGrade(averageAge(p))) / (numAlgorithms + 1));
		}

        try{
            p.save();
        } catch(Exception e){

        }

	}


	public double calculateCLScore(String text, int numWords){
		String[] words = text.split(" ");

		int letterCounter = 0; 
		int sentenceCounter = 0; 
		int counter = 0;

		ArrayList<Integer> letterAveragesPer100 = new ArrayList<Integer>(); 
		ArrayList<Integer> sentenceAveragesPer100 = new ArrayList<Integer>(); 

		for(String w : words){
			if(w.contains(".")) sentenceCounter++; 
			letterCounter += w.length();
			counter++; 
			if(counter == 100 && numWords > 99){
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
		for(int i : letterAveragesPer100){
			l += i; 
		}
		l = l / letterAveragesPer100.size();

		int s = 0; 
		for(int i : sentenceAveragesPer100){
			s += i; 
		}
		s = l / sentenceAveragesPer100.size();

		double result = (.0588 * Double.valueOf(l)) - (.296 * Double.valueOf(s)) - 15.8; 

		System.out.println("CLI = " + result );

		return result; 

	}

	public double calculateARI(SimplePassage p){
		double sentences = Double.valueOf(p.sentences.size());

		double ARI = 4.71 * (Double.valueOf(p.num_characters)/Double.valueOf(p.numWords)) + .5 * (Double.valueOf(p.numWords)/sentences) - 21.43;

		System.out.println("ARI = " + ARI);

		return ARI;
	}

	public double calculateFKScore(SimplePassage p){
		double sentences = Double.valueOf(p.sentences.size());

		double score = (.39 * (Double.valueOf(p.numWords)/sentences)) + (11.8 * (Double.valueOf(p.numSyllables)/Double.valueOf(p.numWords))) - 15.59;

		System.out.println("FK Score: " + score);

		return score;
	}

	public void generateSuggestionsForWord(POS p, String word, String sentence, Long passageId, String ogText) throws JWNLException{

		if(p != null && !p.name.toLowerCase().contains("proper noun")){
			if (c == null) c = new WordNetController();

			HashSet<String> suggestions = c.wordIDLookup(word, p.name);

			Word w = Word.byLemma(word);

            if(Suggestion.byWord(ogText).size() == 0){
                for(String root : suggestions){

                    Word r = Word.byLemma(root);
                    if(!word.toLowerCase().equals(root.toLowerCase())
                            && w != null
                            && r != null
							&& !isProperNoun(w)){

						// if it's capitalize let's assume it's a proper noun and ignore it as long as it is not the first word in a sentence
						if(!(w.lemma.substring(0, 1).toUpperCase() + w.lemma.substring(1, w.length)).equals(w.lemma)
                                && !w.lemma.equals(sentence.split(" ")[0])
                                ){
							Suggestion s = new Suggestion();
							s.word = ogText;

							String threeGram = "";
							String[] splitS = sentence.split(" ");

							String iPOS = "";
							s.suggestedWord = root;
							threeGram = buildThreeGram(splitS, sentence, ogText, root);
							s.simple_passage_id = passageId;
							s.save();



							validator.fetchFrequencies(s, threeGram.toLowerCase());
						}
                    }
                }
            }
        }


	}


	private boolean isProperNoun(Word w){

		for(POS p : w.partsOfSpeech){
			if(p.name.indexOf("Proper Noun") != -1) return true;
		}
		return false;
	}

    private String buildThreeGram(String[] splitS, String sentence, String ogText, String root){
        String threeGram = "";
        if(splitS.length <= 3) threeGram = sentence;
        else{
            for(int i = 0; i < splitS.length; i++){
                if(splitS[i].toLowerCase().equals(ogText.toLowerCase())){
                    if(i > 0 && (i + 1) < splitS.length){
                        threeGram = splitS[i - 1] + " " + root + " " + splitS[i + 1];
                        break;
                    } else if(i > 0){
                        if(i - 2 > 0){
                            threeGram = splitS[i - 2] + " ";
                        }
                        threeGram += splitS[i - 1] + " " + root;
                        break;
                    } else if((i + 1) < splitS.length) {
                        threeGram += root + " " + splitS[i + 1];
                        if((i + 2) < splitS.length){
                            threeGram += splitS[i + 2];
                        }
                        break;
                    }
                }
            }


        }
        return threeGram;
    }
}
