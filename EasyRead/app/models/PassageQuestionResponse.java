package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.Expose;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

@Entity
public class PassageQuestionResponse extends Model {

    private static final long serialVersionUID = 1L;

    // Fields
    @Id
    @Expose
    public Long id;

    @Required
    @Expose
    public Long entity_id;    // e.g. Choice.id or UserInput.id

    @CreatedTimestamp
    public Timestamp createdTime;

    @UpdatedTimestamp
    public Timestamp updatedTime;

    @Required
    public boolean disavowed = false;

    @ManyToOne
    public User submitter;

    // Constructors
    public PassageQuestionResponse(Long entity_id, User submitter) {
        this.entity_id = entity_id;
        this.submitter = submitter;
    }

    // Getters / Setters
    public static Finder<Long, PassageQuestionResponse> find = new Finder<Long, PassageQuestionResponse>(Long.class, PassageQuestionResponse.class);

    public static List<PassageQuestionResponse> all() {
        return find.where().ne("disavowed", true).findList();
    }

    public static PassageQuestionResponse create(PassageQuestionResponse response) {
        response.save();
        return response;
    }

    public static void delete(Long id) {
//		find.ref(id).delete();
        PassageQuestionResponse response = find.ref(id);
        response.disavowed = true;
        response.save();
    }

    public static PassageQuestionResponse byId(Long id) {
        return find.where().ne("disavowed", true).eq("id", id).findUnique();
    }

    public static PassageQuestionResponse byUserAndQuestion(Long userId, Long questionRecordId) {
        return find.where()
                .ne("disavowed", true)
                .eq("submitter_id", userId)
                .eq("question_record_id", questionRecordId)
                .findUnique();
    }


    public static List<PassageQuestionResponse> getAllQuestionsAnswered() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);

        Timestamp timestamp = new Timestamp(cal.getTime().getTime());

        return find.where()
                .ne("disavowed", true)
                .ge("created_time", timestamp)
                .findList();
    }


    public static List<PassageQuestionResponse> getAllQuestionsAnswered(User user) {
        return find.where()
                .ne("disavowed", true)
                .eq("submitter_id", user.id)
                .findList();
    }


    public static List<PassageQuestionResponse> getAllQuestionsAnsweredThisMonth(User user) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);

        Timestamp timestamp = new Timestamp(cal.getTime().getTime());

        return find.where()
                .ne("disavowed", true)
                .eq("submitter_id", user.id)
                .ge("created_time", timestamp)
                .findList();
    }

	/*
	public static int getAverageEngagement() {
		return (int)((double)getAllQuestionsAnswered().size() / (double)User.allStudents().size());
	}
	
	public static int getMaxEngagement() {
		return (int)(getAverageEngagement()*2);
	}
	
	
	public static int getStudentEngagement(User user) {
		if (getAllQuestionsAnswered(user).size() > 0) {
			return Math.min(getAllQuestionsAnswered(user).size(), getMaxEngagement());
		} else {
			return 1;
		}
	}
	
	
	public static String getStudentEngagementString(User user) {
		int numQuestionsAnswered = getAllQuestionsAnswered(user).size();
		int oneFifth = getMaxEngagement()/5;
		
		if (numQuestionsAnswered < oneFifth) {
			return "Well Below Average";
		} else if (numQuestionsAnswered < (oneFifth*2)) {
			return "Below Average";
		} else if (numQuestionsAnswered < (oneFifth*3)) {
			return "Average";
		} else if (numQuestionsAnswered < (oneFifth*4)) {
			return "Above Average";
		} else {
			return "Well Above Average";
		}
	}
	*/
}