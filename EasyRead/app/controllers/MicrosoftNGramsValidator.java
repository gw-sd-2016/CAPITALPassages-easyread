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

	private Suggestion currentSuggestion;
	
	public String generateURL(String p){
		return "http://weblm.research.microsoft.com/rest.svc/" + catalog + "/" + version + "/" + order + "/" + "cp?u=" + TOKEN + "&p=" + p + "&gen=" + p.split(" ").length + "&format=json";
	}




	@Override
	public void fetchFrequencies(Suggestion s, String p) {
		// TODO Auto-generated method stub
		
	
		
		WSRequest holder = WS.url("http://weblm.research.microsoft.com/rest.svc/" + catalog + "/" + version + "/" + order + "/" + "cp");
		
		
		holder.setQueryParameter("u", TOKEN);
		holder.setQueryParameter("p", p);
		holder.setQueryParameter("gen", String.valueOf(p.split(" ").length));
		holder.setQueryParameter("format", "json");
		
		
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