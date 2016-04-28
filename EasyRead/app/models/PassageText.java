package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Expose;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class PassageText extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    @Expose
    public Long id;

    @Expose
    public int grade;

    @Expose
    @Lob
    public String html;


    public PassageText(int grade, String html) {
        this.grade = grade;
        this.html = html;
    }

    public static PassageText create(PassageText pt) {
        pt.save();
        return pt;
    }

    public static Finder<String, PassageText> find = new Finder<String, PassageText>(String.class, PassageText.class);

    public static PassageText bySimplePassageAndGrade(Long simple_passage_id, int grade) {

        try {
            return find.where().eq("simple_passage_id", simple_passage_id).eq("grade",grade).findUnique();
        } catch(Exception e){
            return null;
        }
    }


}