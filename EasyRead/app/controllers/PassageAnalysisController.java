package controllers;

import models.*;
import net.sf.extjwnl.JWNLException;

import java.util.ArrayList;
import java.util.HashSet;

public class PassageAnalysisController {
	
	
	private MicrosoftNGramsValidator validator = new MicrosoftNGramsValidator(); 
	
	
	
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
            for(Word w : s.words){
				if(!Word.isStopWord(w)){
					if(w.ageOfAcquisition > 6){
						average += w.ageOfAcquisition;
						count++;
					}
				}
            }
        }
        return average/count;
    }

	public void determineGradeLevel(SimplePassage p){

		int numAlgorithms; 

		double ARI = calculateARI(p);

		double FK  = calculateFKScore(p);

		double CL = gradeResolution(calculateCLScore(p.text, p.numWords));

		if(p.text.split(" ").length > 100){
			p.grade = (int) Math.round((ARI + FK + CL) / 3);
		} else{
			p.grade = (int) convertToGrade(averageAge(p));
		}

		p.save();
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

	public void generateSuggestionsForWord(POS p, String word, String sentence, Long passageId) throws JWNLException{

		if(p != null && !p.name.toLowerCase().contains("proper noun")){
			WordNetController c = new WordNetController();

			/*
			
			String threeGram = "";
			String[] splitS = sentence.split(" ");
			
			String iPOS = "";
			
			
			for(int i = 0; i < splitS.length; i++){
				if(splitS[i].equals(word)){
					if(i == splitS.length - 1){
						if(i - 2 > 0){
							threeGram += splitS[i - 2] + " ";
							threeGram += splitS[i - 1] + " ";
							threeGram += splitS[i]; 
							iPOS = "end";
						}
					} else if(i - 1 >= 0 && i + 1 < splitS.length){
						threeGram += splitS[i - 1] + " ";
						threeGram += splitS[i] + " ";
						threeGram += splitS[i + 1]; 
						iPOS = "middle"; 
					} else threeGram += splitS[i]; 
					break; 
				}
			}
			
			
			*/
			
			
			

			HashSet<String> suggestions = c.wordIDLookup(word, p.name);
			
			
			

			for(String root : suggestions){
				//System.out.println(root);
				Word w = Word.byLemma(word);
				Word r = Word.byLemma(root);
				if(!word.equals(root) 
						&& w!= null && w.ageOfAcquisition != 1 
						&& POS.byWord(w.lemma) != null 
						&& POS.byWord(w.lemma).name.indexOf("noun") == -1 
						&& Suggestion.byWord(word).size() == 0 
						&& (r != null && r.numSyllables < w.numSyllables) || r == null){

					/*int pos = threeGram.indexOf(w.lemma);
					
					if(pos != -1) {
						
						
						String ogGram = threeGram; 
						
						
						
						threeGram = threeGram.substring(0, pos) + root + threeGram.substring(pos + w.length); 
						
						System.out.println("comparing " + ogGram + " to " + threeGram);
						
						int ogFreq = validator.fetchFrequencies(ogGram); 
						
						int newFreq = validator.fetchFrequencies(threeGram);
						
						
						
					}*/
				
					
					
					Suggestion s = new Suggestion(); 
					s.word = word;
					s.suggestedWord = root; 
					s.simple_passage_id = passageId; 
					s.save();
				}
			}
		}	
	}
}
