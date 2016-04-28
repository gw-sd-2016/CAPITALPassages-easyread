package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.Expose;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.List;

@Entity
public class PassageTag extends Model {

    private static final long serialVersionUID = 1L;

    // Fields
    @Id
    public Long id;

    @Required
    @Column(unique = true)
    @Expose
    public String keyword;


    @CreatedTimestamp
    Timestamp createdTime;

    @UpdatedTimestamp
    Timestamp updatedTime;

    @Required
    public boolean disavowed = false;

    // Constructors
    public PassageTag(String keyword) {
        this.keyword = keyword;
    }

    // Getters / Setters
    public static Finder<Long, PassageTag> find = new Finder<Long, PassageTag>(Long.class, PassageTag.class);

    public static List<PassageTag> all() {
        return find.where().ne("disavowed", true).findList();
    }


    public static PassageTag create(PassageTag tag) {
        tag.save();
        return tag;
    }


    public static PassageTag byId(Long id) {
        return find.where().ne("disavowed", true).eq("id", id).findUnique();
    }

    public static PassageTag byId(String id) {
        if (id == null) {
            return null;
        }

        if (id.equals("")) {
            return null;
        }

        return byId(Long.parseLong(id));
    }

    public static PassageTag byKeyword(String keyword) {
        return find.where()
                .ne("disavowed", true)
                .eq("keyword", keyword)
                .findUnique();
    }

}