package models;

import com.avaje.ebean.annotation.Expose;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.*;
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
    @Lob
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
        	if(Word.byLemma(w) != null){
                this.words.add(Word.byLemma(w));
            } else {
                Word n = new Word();
                n.lemma = w;
                n.disavowed = false;
                n.ageOfAcquisition = -1;
                n.numSyllables = 0;

                this.words.add(n);
            }
        }

    }
    
    
    
    
    public static Sentence create(Sentence s) {
        s.save();
        return s;
    
    }
}