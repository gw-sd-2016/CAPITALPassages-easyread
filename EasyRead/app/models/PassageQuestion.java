package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.Expose;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.sql.Timestamp;
import java.util.*;

@Entity
public class PassageQuestion extends Model implements Comparable<PassageQuestion> {

    private static final long serialVersionUID = 1L;

    // Fields
    @Id
    @Expose
    public Long id;

    @Required
    @Expose
    public Long basis_id; // simple passage id

    @Required
    public boolean active;

    @CreatedTimestamp
    public Timestamp createdTime;

    @UpdatedTimestamp
    public Timestamp updatedTime;

    @Required
    public boolean disavowed = false;

    @OneToMany(cascade = CascadeType.ALL)
    @Expose
    public Set<PassageQuestionChoice> choices = new HashSet<PassageQuestionChoice>();

    @Expose
    public PassageQuestionPrompt prompt;

    @Expose
    public int position;

    @Expose
    public String correctAnswer;


    // Constructors
    public PassageQuestion(Long basis_id, boolean active) {
        this.basis_id = basis_id;
        this.active = active;
    }

    // Getters / Setters
    public static Finder<Long, PassageQuestion> find = new Finder<Long, PassageQuestion>(Long.class, PassageQuestion.class);

    public static List<PassageQuestion> all() {
        /*
		Comparator<PassageQuestion> c = (p, o) ->
		p.compareTo(o);

		/*c = c.thenComparing((p, o) ->
		p.compareTo(o));*/

        List<PassageQuestion> ret =
                //ret.sort(c);
                find.where().ne("disavowed", true).findList();

        return ret;
    }


    public static PassageQuestion byPassageAndPosition(Long passageId, int pos){
        return find.where().eq("simple_passage_id", passageId).eq("position", pos).findUnique();
    }

    public static PassageQuestion create(PassageQuestion question) {
        question.save();
        return question;
    }

    public void addPrompt(PassageQuestionPrompt p) {
        p.questionId = this.id;
        this.save();
    }


    @Override
    public void delete() {
        for(PassageQuestionChoice c : this.choices){
            c.deepDelete();
        }

        PassageQuestionPrompt realPrompt = PassageQuestionPrompt.byPassageQuestion(id).get(0);
        realPrompt.delete();

        for(PassageQuestionRecord rec : PassageQuestionRecord.byPassageQuestionId(id)){
            for(PassageQuestionResponse res : rec.responses) res.delete();
            rec.delete();
        }

        super.delete();

    }


    public static List<String> promptsForPassageQuestion(Long passageId) {
        List<String> ret = new ArrayList<String>();

        for (PassageQuestion pq : byPassageAndBasis(passageId, passageId)) {
            ret.add(pq.prompt.text);
        }
        return ret;
    }

    public static PassageQuestion byId(Long id) {
        return find.where().ne("disavowed", true).eq("id", id).findUnique();
    }

    public static List<PassageQuestion> byPassageAndBasis(Long passageId, Long basisId) {
        return find.where()
                .ne("disavowed", true)
                .eq("passage_id", passageId)
                .eq("basis_id", basisId)
                .findList();
    }

    public static Set<PassageQuestionChoice> getActiveChoices(PassageQuestion question) {
        if (question == null) {
            return null;
        }

        Set<PassageQuestionChoice> activeChoices = new HashSet<PassageQuestionChoice>();
        Iterator<PassageQuestionChoice> itr = question.choices.iterator();
        while (itr.hasNext()) {
            PassageQuestionChoice choice = itr.next();
            if (choice == null) {
                continue;
            }

            if (choice.disavowed) {
                continue;
            }

            if (!choice.active) {
                continue;
            }

            activeChoices.add(choice);
        }

        return activeChoices;
    }

    public static Set<PassageQuestionChoice> getInactiveChoices(PassageQuestion question) {
        if (question == null) {
            return null;
        }

        Set<PassageQuestionChoice> inactiveChoices = new HashSet<PassageQuestionChoice>();
        Iterator<PassageQuestionChoice> itr = question.choices.iterator();
        while (itr.hasNext()) {
            PassageQuestionChoice choice = itr.next();
            if (choice == null) {
                continue;
            }

            if (choice.disavowed) {
                continue;
            }

            if (choice.active) {
                continue;
            }

            inactiveChoices.add(choice);
        }

        return inactiveChoices;
    }

    @Override
    // -1 --> goes before
    public int compareTo(PassageQuestion o) {
        if (o.position < this.position) return -1;
        else return 1;
    }

}