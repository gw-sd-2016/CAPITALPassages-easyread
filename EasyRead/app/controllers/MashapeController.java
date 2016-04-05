package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.SimplePassage;
import models.Word;
import net.davidashen.text.Hyphenator;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

public class MashapeController {

    private Hyphenator h = new Hyphenator();


    /**
     * This method either fetches syllables from Mashape Words (https://www.wordsapi.com/docs) for words that aren't in the database
     * or updates the syllable count of the passage with the stored syllable count from the exisitng word in the database.
     * @param word String representing for which number of syllables is requested
     * @param p Simple Passage object which contains the word
     */
    public void getNumSyllablesForWord(String word, SimplePassage p) {
        p.num_characters += word.length();
        if (Word.byLemma(word) == null) {
            getSyllableInformation(p, word);
        } else {
            p.numSyllables += Word.byLemma(word).numSyllables;
        }
    }


    /**
     * This method asynchronously fetches syllable counts from Mashape Word https://www.wordsapi.com/docs and updates
     * the syllable count of the Passage.
     * New words are not added to the database.
     * @param p
     * @param word
     * @return
     */
    public F.Promise<Integer> getSyllableInformation(SimplePassage p, String word) {
        if (word.length() > 2) {
            String url = "https://wordsapiv1.p.mashape.com/words/" + word + "/syllables";

            return WS.url(url).setHeader("X-Mashape-Key", "0qln9a1q5Hmsh7DpS3ttXRqx4nGCp1dmYowjsna9AhhZ2xMAbi").get().map(
                    new F.Function<WSResponse, Integer>() {
                        public Integer apply(WSResponse response) {
                            JsonNode res = response.asJson();

                            int result = res.findValue("syllables").findValue("count").asInt();

                            if(result == 0){
                                p.numSyllables += Math.max(1, h.hyphenate(word).split("-").length);
                            } else {
                                p.numSyllables += result;
                            }

                            p.save();
                            return 0;
                        }

                    });
        } else return null;
    }

}