package controllers;

import models.Suggestion;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Result;

import java.util.ArrayList;

/**
 * Created by malcolmgoldiner on 1/19/16.
 */
public class GoogleNGramsValidator implements PhraseValidator {

    String url = "https://books.google.com/ngrams/graph?content=FIRST&year_start=2000&year_end=2004&corpus=15&smoothing=3&share=&direct_url=t1%3B%2CINSERT%3B%2Cc0";



    public void fetchFrequencies(Suggestion s, String p){

        String insert = "";

        for(String substring : p.split(" ")){
            insert += substring + "+";
        }

        insert = insert.substring(0, insert.length() - 1);

        url = url.replace("FIRST", insert);


        p = p.replace(" ", "%20");


        WSRequest holder = WS.url(url.replace("INSERT", p));

        holder.get().map(
                new F.Function<WSResponse, Result>() {
                    public Result apply(WSResponse response) {


                        String body = response.getBody();

                        int startIndex = body.indexOf("  var data = [{\"ngram\"");


                        body = body.substring(startIndex);

                        body = body.substring(body.indexOf("timeseries") + "timeseries".length() + 4);


                        body = body.substring(0, body.indexOf("]"));

                        char[] arr = body.toCharArray();


                        ArrayList<Double> freq = new ArrayList<Double>();

                        StringBuilder sb = new StringBuilder();

                        for(int i = 0; i < arr.length; i++){
                            if(arr[i] != 'e'){
                                sb.append(arr[i]);
                            } else {
                                double number = Double.valueOf(sb.toString());
                                sb = new StringBuilder();
                                int exponent = 1;
                                int previousI = i + 1;

                                String exp;
                                if(arr.length - 4 <= i){
                                    exp = body.substring(previousI, body.length());
                                    i = arr.length;
                                } else {
                                    while(arr[i] != ',' && i < arr.length) i++;
                                    exp = body.substring(previousI, i);
                                }

                                exponent = Integer.parseInt(exp);
                                freq.add(number * Math.pow(10, exponent));

                            }

                        }

                        double total = 0;
                        for(Double a : freq) total += a;

                        s.frequency = total / freq.size();
                        s.save();

                        return null;
                    }
                });

    }
}
