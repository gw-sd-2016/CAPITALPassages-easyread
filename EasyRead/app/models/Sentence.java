package models;

import com.google.gson.annotations.Expose;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Sentence extends Model {
    
    private static final long serialVersionUID = 1L;
    
    // Fields
    @Id
    @Expose
    public Long id;
    
    @Required
    @Expose
    public String text;

    
    @Required
    public List<Word> words = new ArrayList<Word>();
    
    // need to remove -- now with words
    @OneToMany(cascade = CascadeType.ALL)
    public List<POS> pos = new ArrayList<POS>();


    
   public static Finder<String,Sentence> find = new Finder<String,Sentence>(String.class, Sentence.class);
    
    public static Sentence byId(Long id) {
        return find.where().eq("id", id).findUnique();
    }
    
	public static List<Sentence> bySimplePassage(Long simplePassageId) {
		return find.where()
				.eq("simple_passage_id", simplePassageId)
				.findList(); 
	}
    
    public static List<Sentence> all() {
        return find.where().findList();
    }

    
    public Sentence(String text, int orderIndex){
        this.text = text;
        
        for(String w : this.text.split(" ")){
        	//this.words.add(w);
        }

    }
    
    
    
    
    public static Sentence create(Sentence s) {
        s.save();
        return s;
    
    }
}