package controllers;

import models.Suggestion;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by malcolmgoldiner on 1/19/16.
 */
public class GoogleNGramsValidator implements PhraseValidator {

    private final String url = "https://books.google.com/ngrams/graph?content=FIRST&year_start=2000&year_end=2008&corpus=15&smoothing=3";




    private final HashMap<String, Double> diffs = new HashMap<String, Double>();



    public void checkOriginal(Suggestion s, String p){


        if(!diffs.containsKey(s.word)){

            /*
            String insert = "";

            String currentURL = url;

            final String oSentence = p;

            p = p.replace(s.suggestedWord,s.word);

            for (String substring : p.split(" ")) {
                insert += substring + "+";
            }

            insert = insert.substring(0, insert.length() - 1);

            currentURL = currentURL.replace("FIRST", insert);


            WSRequest holder = WS.url(currentURL);*/


            p = p.replace(s.suggestedWord,s.word);



            String currentURL = "https://books.google.com/ngrams/graph";


            WSRequest holder = WS.url(currentURL);


            holder.setQueryParameter("content", p);
            holder.setQueryParameter("year_start", "2000");
            holder.setQueryParameter("year_end", "2008");
            holder.setQueryParameter("corpus", "15");
            holder.setQueryParameter("smoothing", "3");

            holder.setFollowRedirects(true);



            holder.get().map(
                    new F.Function<WSResponse, Result>() {
                        public Result apply(WSResponse response) {
                            String body = response.getBody();


                            int startIndex = body.indexOf("  var data = [");



                            if(body.charAt(startIndex + "  var data = [".length()) != ']'){
                                body = body.substring(startIndex);

                                body = body.substring(body.indexOf("timeseries") + "timeseries".length() + 4);


                                body = body.substring(0, body.indexOf("]"));

                                char[] arr = body.toCharArray();


                                ArrayList<Double> freq = new ArrayList<Double>();

                                StringBuilder sb = new StringBuilder();

                                for (int i = 0; i < arr.length; i++) {
                                    if (arr[i] != ',') {
                                        sb.append(arr[i]);
                                    } else {
                                        double number = Double.valueOf(sb.toString());
                                        sb = new StringBuilder();
                                        int exponent = 1;
                                        int previousI = i + 1;

                                        String exp;
                                        if (arr.length - 4 <= i) {
                                            exp = body.substring(previousI, body.length());
                                            i = arr.length;
                                        } /* else {
                                    while (arr[i] != ',' && i < arr.length) i++;
                                    exp = body.substring(previousI, i);
                                } */

                                        // exponent = Integer.parseInt(exp);
                                        freq.add(number /* Math.pow(10, exponent)*/);

                                    }

                                }

                                double total = 0;
                                for (Double a : freq) total += a;

                                double oFrequency = total / freq.size();
                                diffs.put(s.word, oFrequency);

                                if(oFrequency > s.frequency) {
                                    System.out.println("\n\n----CAUGHT BAD SUGGESTION---\n\n");
                                    s.frequency = 0;
                                }
                            } else {
                                diffs.put(s.word, Double.valueOf(0));
                            }

                                s.save();

                            return null;
                        }
                    });
        } else {
            if(s.frequency < diffs.get(s.word)){
                s.frequency = 0;
                s.save();
            }
        }





    }


    public void fetchFrequencies(Suggestion s, String p) {

        String insert = "";

        String currentURL = url;

        final String oSentence = p;

        for (String substring : p.split(" ")) {
            insert += substring + "+";
        }

        insert = insert.substring(0, insert.length() - 1);

        currentURL = "https://books.google.com/ngrams/graph";


        WSRequest holder = WS.url(currentURL);


        holder.setQueryParameter("content", p);
        holder.setQueryParameter("year_start", "2000");
        holder.setQueryParameter("year_end", "2008");
        holder.setQueryParameter("corpus", "15");
        holder.setQueryParameter("smoothing", "3");

        holder.setFollowRedirects(true);


        holder.get().map(
                new F.Function<WSResponse, Result>() {
                    public Result apply(WSResponse response) {


                        String body = response.getBody();


                        //int startIndex = body.indexOf("  var data = [{\"ngram\"");

                        int startIndex = body.indexOf("  var data = [");




                        if(body.charAt(startIndex + "  var data = [".length()) != ']'){
                            body = body.substring(startIndex);

                            body = body.substring(body.indexOf("timeseries") + "timeseries".length() + 4);


                            body = body.substring(0, body.indexOf("]"));

                            char[] arr = body.toCharArray();


                            ArrayList<Double> freq = new ArrayList<Double>();

                            StringBuilder sb = new StringBuilder();

                            for (int i = 0; i < arr.length; i++) {
                                if (arr[i] != ',') {
                                    sb.append(arr[i]);
                                } else {
                                    double number = Double.valueOf(sb.toString());
                                    sb = new StringBuilder();
                                    int exponent = 1;
                                    int previousI = i + 1;

                                    String exp;
                                    if (arr.length - 4 <= i) {
                                        exp = body.substring(previousI, body.length());
                                        i = arr.length;
                                    } /* else {
                                    while (arr[i] != ',' && i < arr.length) i++;
                                    exp = body.substring(previousI, i);
                                } */

                                    // exponent = Integer.parseInt(exp);
                                    freq.add(number /* Math.pow(10, exponent)*/);

                                }

                            }

                            double total = 0;
                            for (Double a : freq) total += a;

                            s.frequency = total / freq.size();
                        } else {
                            s.frequency = 0;
                        }



                        s.save();

                        return null;
                    }
                });

    }
}
