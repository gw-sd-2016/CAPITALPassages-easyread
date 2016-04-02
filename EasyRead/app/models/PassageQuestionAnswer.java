package models;

import com.avaje.ebean.annotation.Expose;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.List;


@Entity
public class PassageQuestionAnswer extends Model {

    private static final long serialVersionUID = 1L;

    // Fields
    @Id
    @Expose
    public Long id;


    @Required
    @Lob
    public String text;

    @Expose
    public Long choiceId;


    public static Finder<String, PassageQuestionAnswer> find = new Finder<String, PassageQuestionAnswer>(String.class, PassageQuestionAnswer.class);

    public static PassageQuestionAnswer byId(Long id) {
        return find.where().eq("id", id).findUnique();
    }

    public static List<PassageQuestionAnswer> byPassageQuestionChoice(Long id) {
        System.out.println("asked about choice : " + id);
        return find.where().eq("choice_id", id).findList();
    }


    public static List<PassageQuestionAnswer> all() {
        return find.where().findList();
    }

    public PassageQuestionAnswer(PassageQuestionChoice c, String text) {
        // answers id is choice id
        this.choiceId = c.id;
        this.text = text;

    }


    public static List<PassageQuestionAnswer> allAnswers() {
        return find.all();
    }


    public static PassageQuestionAnswer create(PassageQuestionAnswer answer) {
        answer.save();
        return answer;
    }

}