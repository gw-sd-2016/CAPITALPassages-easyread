package models;

import com.avaje.ebean.annotation.Expose;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;


@Entity
public class Suggestion extends Model {

    private static final long serialVersionUID = 1L;

    // Fields
    @Id
    @Expose
    public Long id;

    @Expose
    public String word;

    @Expose
    public String suggestedWord;

    @Expose
    public Long simple_passage_id;

    @Expose
    public double frequency;

    public static Finder<String, Suggestion> find = new Finder<String, Suggestion>(String.class, Suggestion.class);

    public static List<Suggestion> all() {
        return find.where().findList();
    }

    public static Suggestion byId(Long id) {
        return find.where().eq("id", id).findUnique();
    }

    public static List<Suggestion> byWord(String word) {
        return find.where().eq("word", word).findList();
    }

    public static List<Suggestion> bySimplePassage(Long simple_passage_id) {
        return find.where().eq("simple_passage_id", simple_passage_id).findList();
    }
}
    