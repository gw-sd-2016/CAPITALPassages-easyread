package models;


import com.google.gson.annotations.Expose;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.List;


@Entity
public class PassageQuestionPrompt extends Model {

	private static final long serialVersionUID = 1L;

	// Fields
	@Id
	@Expose
	public Long id;


	@Required
	@Lob
	public String text;
	
	@Expose
	public Long questionId; 
	


	public static Finder<String,PassageQuestionPrompt> find = new Finder<String,PassageQuestionPrompt>(String.class, PassageQuestionPrompt.class);

	public static PassageQuestionPrompt byId(Long id) {
		return find.where().eq("id", id).findUnique();
	}
	public static List<PassageQuestionPrompt> byPassageQuestion(Long id) {
		return find.where().eq("question_id", id).findList();
	}
	
	
	
	public static List<PassageQuestionPrompt> all() {
		return find.where().findList();
	}

	public PassageQuestionPrompt(PassageQuestion q, String text){
		this.questionId = q.id;
		this.text = text;
		
	}

	
	/*
	public void delete(){
		this.delete();
		System.out.println("deleting");
	}
*/
	

	public static List<PassageQuestionPrompt> allPrompts() {
		return find.all();
	}

	public static PassageQuestionPrompt create(PassageQuestionPrompt prompt) {
		prompt.save();
		return prompt;
	}

}