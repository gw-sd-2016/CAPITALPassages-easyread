package models;


import com.google.gson.annotations.Expose;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class Phrase extends Model {
    
    private static final long serialVersionUID = 1L;
    
    // Fields
    @Id
    @Expose
    public Long id;
    
    @Required
    @Expose
    public String text;

    

    
    public Phrase(String text){
        this.text = text;
    }
    
    
    
    
    public static Phrase create(Phrase s) {
        s.save();
        return s;
    }
    
    
}
