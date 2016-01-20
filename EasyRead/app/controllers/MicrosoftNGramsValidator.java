package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Suggestion;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Result;

public class MicrosoftNGramsValidator implements PhraseValidator {
	
	
	public final String TOKEN = "b31e797e-049f-4939-8e8c-af3af737ad2e";
	public final String catalog = "bing-body";
	public final String version = "2013-12";
	public final String order = "3";

	private final String model = "body";
	private final String key = "c992ac297e68484690b92c791a0c7ea7";
	private final String contentType = "application/JSON";

	private Suggestion currentSuggestion;
	
	public String generateURL(String p){
		return "https://api.projectoxford.ai/text/weblm/v1.0/calculateJointProbability/" + catalog + "/" + version + "/" + order + "/" + "cp?u=" + TOKEN + "&p=" + p + "&gen=" + p.split(" ").length + "&format=json";
	}




	@Override  //UNFINISHED
	public void fetchFrequencies(Suggestion s, String p) {
		// TODO Auto-generated method stub
		
	
		
		WSRequest holder = WS.url("https://api.projectoxford.ai/text/weblm/v1.0/calculateJointProbability");
		
		
		holder.setQueryParameter("model", model);
		holder.setQueryParameter("order", order);

        holder.setHeader("Content-Type", contentType);
        holder.setHeader("Ocp-Apim-Subscription-Key", key);


        String body = "{\n\"queries\"\n[\n\"this\",\n\"is\"\n]\n}";

        holder.setBody(body);
		
		holder.get().map(
				new F.Function<WSResponse, Result>() {
					public Result apply(WSResponse response) {
						
					//	System.out.println(response.getBody());
						JsonNode res = response.asJson();
						System.out.println(p + " " + res);
                        double result = Double.valueOf(res.asDouble());
						s.frequency = Math.pow(10,result);
						s.save();
                        return null;
					}
				});
	}
}