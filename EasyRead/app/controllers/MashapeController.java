package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.SimplePassage;
import models.Word;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Result;

public class MashapeController {
	
	

	public void getNumSyllablesForWord(String word, SimplePassage p) {
		p.num_characters += word.length();
		if(Word.byLemma(word) == null){
			System.out.println("requesting: " + word);
			if(word.length() > 1) getSyllableInformation(word, p);
		} else {
			p.numSyllables += Word.byLemma(word).numSyllables;
		}
	}
	
	public void getNumSyllablesForString(String word) {
		int numCharacters = 0;
		int numSyllables = 0; 
		String[] words = word.split(" ");
		
		for(String w : words){
			
			numCharacters += w.length();
			if(Word.byLemma(w) == null){
				System.out.println("requesting: " + word);
				if(w.length() > 1) numSyllables += getSyllableInformation(w).get(0);
			} else {
				numSyllables += Word.byLemma(word).numSyllables;
			}
		}
	}

	public void getSyllableInformation(String word, SimplePassage p) {


		if(word.length() > 2){

			String url = "https://wordsapiv1.p.mashape.com/words/" + word + "/syllables";
			//System.out.println(url);
			WS.url(url).setHeader("X-Mashape-Key", "0qln9a1q5Hmsh7DpS3ttXRqx4nGCp1dmYowjsna9AhhZ2xMAbi").get().map(
					new F.Function<WSResponse, Object>() {
						public Result apply(WSResponse response) {
							//System.out.println("response:" + response.asJson());
							JsonNode res = response.asJson();
							System.out.println("response: " + res);

							System.out.println("num:" + res.findValue("syllables").findValue("count"));

							Word nW = new Word(); 

							nW.lemma = word; 

							nW.numSyllables = res.findValue("syllables").findValue("count").asInt();

							//SimplePassage thisPassage = SimplePassage.byId(p.id);

							p.numSyllables += nW.numSyllables;

							p.save();

							nW.wordNetId = Long.valueOf(-1);
							nW.ageOfAcquisition = 6;
							nW.length = word.length();
							nW.save();
							return null;
						}
					});
		}
	}
    
    public void getFrequencyInformation(String word, SimplePassage p) {
        
        
       
        
            String url = "https://wordsapiv1.p.mashape.com/words/" + word + "/frequency";
            //System.out.println(url);
            WS.url(url).setHeader("X-Mashape-Key", "0qln9a1q5Hmsh7DpS3ttXRqx4nGCp1dmYowjsna9AhhZ2xMAbi").get().map(
                                                                                                                   new F.Function<WSResponse, Object>() {
                                                                                                                       public Result apply(WSResponse response) {
                                                                                                                           //System.out.println("response:" + response.asJson());
                                                                                                                           JsonNode res = response.asJson();
                                                                                                                           System.out.println("response: " + res);
                                                                                                                           
                                                                                                                           System.out.println("num:" + res.findValue("syllables").findValue("count"));
                                                                                                                           
                                                                                                                           Word nW = new Word(); 
                                                                                                                           
                                                                                                                           nW.lemma = word; 
                                                                                                                           
                                                                                                                           nW.numSyllables = res.findValue("syllables").findValue("count").asInt();
                                                                                                                           
                                                                                                                           //int frequency = res.findValue("frequency");
                                                                                                                           
                                                                                                                           //SimplePassage thisPassage = SimplePassage.byId(p.id);
                                                                                                                           
                                                                                                                           p.numSyllables += nW.numSyllables;
                                                                                                                           
                                                                                                                           p.save();
                                                                                                                           
                                                                                                                           nW.wordNetId = Long.valueOf(-1);
                                                                                                                           nW.ageOfAcquisition = 6;
                                                                                                                           nW.length = word.length();
                                                                                                                           nW.save();
                                                                                                                           return null;
                                                                                                                       }
                                                                                                                   });
        
    }

	
	
	public F.Promise<Integer> getSyllableInformation(String word) {


		if(word.length() > 2){

			String url = "https://wordsapiv1.p.mashape.com/words/" + word + "/syllables";
			//System.out.println(url);
			return WS.url(url).setHeader("X-Mashape-Key", "0qln9a1q5Hmsh7DpS3ttXRqx4nGCp1dmYowjsna9AhhZ2xMAbi").get().map(
                    new F.Function<WSResponse, Integer>() {
						public Integer apply(WSResponse response) {
							//System.out.println("response:" + response.asJson());
							JsonNode res = response.asJson();
							System.out.println("response: " + res);

							System.out.println("num:" + res.findValue("syllables").findValue("count"));

							Word nW = new Word(); 

							nW.lemma = word; 

							nW.numSyllables = res.findValue("syllables").findValue("count").asInt();

							//SimplePassage thisPassage = SimplePassage.byId(p.id);

							return  nW.numSyllables;

					
						}
					});
		} else return null;
		
	}

}
