package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.Expose;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class PassageQuestionRecord extends Model {
	
	private static final long serialVersionUID = 1L;

	// Fields
	@Id
	@Expose
	public Long id;
	
	@Required
	public boolean cleared;

	@CreatedTimestamp
	public Timestamp createdTime;
	
	@UpdatedTimestamp
	public Timestamp updatedTime;
	
	@Required
	public boolean disavowed = false;
	
	@ManyToOne
	public User user;
	
	@ManyToOne
	public PassageQuestion question;
	
	@OneToMany(cascade = CascadeType.ALL)
	public Set<PassageQuestionResponse> responses = new HashSet<PassageQuestionResponse>();
	
	// Constructors
	public PassageQuestionRecord(User user, PassageQuestion question, boolean cleared) {
		this.user = user;
		this.question = question;
		this.cleared = cleared;
	}

	// Getters / Setters
	public static Finder<Long, PassageQuestionRecord> find = new Finder<Long, PassageQuestionRecord>(Long.class, PassageQuestionRecord.class);

	public static List<PassageQuestionRecord> all() {
		return find.where().ne("disavowed", true).findList();
	}
	
	public static PassageQuestionRecord create(PassageQuestionRecord courseRecord) {
		courseRecord.save();
		return courseRecord;
	}

	public static void delete(Long id) {
//		find.ref(id).delete();
		PassageQuestionRecord questionRecord = find.ref(id);
		if (questionRecord == null) {
			return;
		}
		
		questionRecord.disavowed = true;
		questionRecord.save();
	}
	
	public static PassageQuestionRecord byId(Long id) {
		return find.where().ne("disavowed", true).eq("id", id).findUnique();
	}
	
	public static PassageQuestionRecord byMostRecentForUser(User user, PassageQuestion question) {
		List<PassageQuestionRecord> theList = find.where()
				.ne("disavowed", true)
				.eq("user_id", user.id)
				.eq("passage_question_id", question.id)
				.findList();

		if (theList.size() == 0) {
			return null;
		} else {
			return theList.get(theList.size()-1);
		}
	}
	
	
	
	public static PassageQuestionRecord byTimeTaken(User user, PassageQuestion passageQuestion, int timeTaken) {
		List<PassageQuestionRecord> theList = find.where()
				.ne("disavowed", true)
				.eq("user_id", user.id)
				.eq("passage_question_id", passageQuestion.id)
				.findList();
		if (theList.size() == 0) {
			return null;
		} else {
//			if (theList.size() <= timeTaken) {
//				timeTaken -= 1;
//			}
			return theList.get(timeTaken);
		}
	}
	
	
	public static List<PassageQuestionRecord> byPassageQuestionId(Long passageQuestionId) {
		List<PassageQuestionRecord> recs = PassageQuestionRecord.all();
		List<PassageQuestionRecord> ret = new ArrayList<PassageQuestionRecord>(); 
		
		for(PassageQuestionRecord r : recs){
			if(r.question.id == passageQuestionId) ret.add(r);
		}
		
		return ret; 
	}
	
	public static Set<PassageQuestionRecord> byUser(User user) {
		return find.where()
				.ne("disavowed", true)
				.eq("user_id", user.id)
				.findSet();
	}
	
	public static Set<PassageQuestionRecord> byUserAndPassage(User user, SimplePassage passage) {
		ArrayList<String> lessonQuestionIds = new ArrayList<String>(passage.questions.size());
		for (PassageQuestion question : passage.questions) {
			lessonQuestionIds.add(question.id.toString());
		}
		
		return find.where()
				.ne("disavowed", true)
				.eq("user_id", user.id)
				.in("passage_question_id", lessonQuestionIds)
				.findSet();
	}
	
	public static int getNumQuestionsCleared(User user, SimplePassage passage) {
		int numQuestionsCleared = 0;
		
		Set<PassageQuestionRecord> qrs = PassageQuestionRecord.byUserAndPassage(user, passage);
		for (PassageQuestionRecord qr : qrs) {
			if (qr.cleared) {
				numQuestionsCleared++;
			}
		}
		
		return numQuestionsCleared;
	}
	
	public static int getNumQuestionsCorrect(User user, SimplePassage passage, int timeTaken) {
		int numQuestionsCorrect = 0;
		int count = 0;
		
//		System.out.println("getting num questions correct for user " + user.id + "::: time = " + timeTaken);
		
		for (PassageQuestion q : passage.questions) {
			count += 1;
			PassageQuestionRecord qr = PassageQuestionRecord.byTimeTaken(user, q, timeTaken);
//			System.out.println(qr);
			
			if (qr == null) {
				continue;
			}
		
			if (qr.responses == null) {
				// question hasn't even been cleared yet
				continue;
			}
			
			if (qr.responses.size() <= 0) {
				// question hasn't even been cleared yet
				continue;
			}
			
			PassageQuestionResponse PassageQuestionResponse = qr.responses.iterator().next();	// TODO: support multiple PassageQuestionResponses?
			
			PassageQuestionChoice correctChoice = PassageQuestionChoice.getCorrectChoice(qr.question.choices);
			if (PassageQuestionResponse.entity_id.equals(correctChoice.entity_id)) {
				numQuestionsCorrect++;
			}
		}
		
		return numQuestionsCorrect;
	}

}
