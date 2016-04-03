package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Expose;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

/**
 * Created by malcolmgoldiner on 4/3/16.
 */

@Entity
public class InflectedWordForm extends Model {

    private static final long serialVersionUID = 1L;

    // Fields
    @Id
    @Expose
    public Long id;

    @Expose
    public String word;

    @Expose
    public String stem;

    public static Finder<String, InflectedWordForm> find = new Finder<String, InflectedWordForm>(String.class, InflectedWordForm.class);

    public static List<InflectedWordForm> all() {
        return find.where().findList();
    }

    public static InflectedWordForm byId(Long id) {
        return find.where().eq("id", id).findUnique();
    }

    public static Word byInflectedForm(String infl) {
        try{
            return Word.byRawString(find.where().eq("word", infl).findUnique().stem);
        } catch(Exception e) {
            return null;
        }
    }


}
