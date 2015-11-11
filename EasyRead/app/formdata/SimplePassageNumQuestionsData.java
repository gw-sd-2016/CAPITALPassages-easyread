package formdata;

import java.util.ArrayList;



public class SimplePassageNumQuestionsData {
	
	public Long passageId; 
	public ArrayList<String> questions = new ArrayList<String>(); 
	public ArrayList<ArrayList<String>> choices = new ArrayList<ArrayList<String>>(); 
	public ArrayList<String> correctAnswers = new ArrayList<String>(); 
	
	public SimplePassageNumQuestionsData(Long passageId, ArrayList<String> questions, ArrayList<ArrayList<String>> choices, ArrayList<String> correctAnswers){
		this.passageId = passageId;
		this.questions = questions; 
		this.choices = choices; 
		this.correctAnswers = correctAnswers; 
	}
	
	public SimplePassageNumQuestionsData(){}
	

}
