package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.Expose;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

@Entity
public class PassageQuestionChoice extends Model {

    private static final long serialVersionUID = 1L;

    // Fields
    @Id
    @Expose
    public Long id;

    @Required
    @Expose
    public Long entity_id;    // e.g. Word.id

    @Required
    @Expose
    public boolean correct;

    @Required
    public boolean active;

    @CreatedTimestamp
    public Timestamp createdTime;

    @UpdatedTimestamp
    public Timestamp updatedTime;

    @Required
    public boolean disavowed = false;

    @Expose
    public PassageQuestionAnswer answer;

    @Expose
    public int position;

    // Constructors
    public PassageQuestionChoice(Long entity_id, boolean correct, boolean active) {
        this.entity_id = entity_id;
        this.correct = correct;
        this.active = active;
    }

    public PassageQuestionChoice() {
        // TODO Auto-generated constructor stub
    }

    // Getters / Setters
    public static Finder<Long, PassageQuestionChoice> find = new Finder<Long, PassageQuestionChoice>(Long.class, PassageQuestionChoice.class);

    public static List<PassageQuestionChoice> all() {
        return find.where().ne("disavowed", true).findList();
    }

    public void addAnswer(PassageQuestionAnswer a) {
        a.choiceId = this.id;
        a.save();
    }

    public static PassageQuestionChoice create(PassageQuestionChoice choice) {
        choice.save();
        return choice;
    }


    public static void delete(Long id) {
        PassageQuestionChoice choice = find.ref(id);
        if (choice == null) {
            return;
        }

        choice.disavowed = true;
        choice.save();
    }

    public static PassageQuestionChoice byId(Long id) {
        return find.where().ne("disavowed", true).eq("id", id).findUnique();
    }

    public static PassageQuestionChoice byId(String id) {
        if (id == null) {
            return null;
        }

        if (id.equals("")) {
            return null;
        }

        return byId(Long.parseLong(id));
    }

    public static PassageQuestionChoice getCorrectChoice(Collection<PassageQuestionChoice> choices) {
        if (choices == null) {
            return null;
        }

        PassageQuestionChoice correctChoice = null;
        for (PassageQuestionChoice choice : choices) {
            if (choice.correct) {
                correctChoice = choice;
                break;
            }
        }

        return correctChoice;
    }

}