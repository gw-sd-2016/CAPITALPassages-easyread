package models;

import com.avaje.ebean.annotation.Expose;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@Entity
public class POS extends Model {

// can probably change this all to an ENUM

	private static final long serialVersionUID = 1L;

	@Id
	@Expose
	public Long id;

	@Expose
	public String name;


	public POS(String name){
		this.name = getHumanReadableName(name);  
	}

	public static POS create(POS pos) {
		pos.save();
		return pos;
	}

	public static Finder<String,POS> find = new Finder<String,POS>(String.class, POS.class);

	public static POS byId(Long id) {
		return find.where().eq("id", id).findUnique();
	}
	public static List<POS> bySentence(Long id) {
		return find.where().eq("sentence_id", id).findList();
	}
	
	public static POS bySentenceAndWord(Long sID, String word){
		List<POS> thisSentence = bySentence(sID);
		
		
		int wordIndex = -1;
		
		String[] words = Sentence.byId(sID).text.split(" ");
		for(int i = 0; i < words.length; i++) if(words[i].equals(word)) wordIndex = i; 
		
		if(wordIndex != -1){
			return thisSentence.get(wordIndex);
		}
		
		return null;
	}
	
	public static POS byWord(String word){
		for(Sentence s : Sentence.all()){
			if(s.text.toLowerCase().contains(word.toLowerCase())){
				String[] words = s.text.split(" ");
				int index = 0; 
				for(int i = 0; i < words.length; i++){
					if(words[i].equals(word)){
						index = i;
						break;
					}
				}
				return POS.bySentence(s.id).get(index);
			}
			
			
			
			
			
			
		}
		return null; 
	}

	
	
	
	
	
	

	//http://stackoverflow.com/questions/1833252/java-stanford-nlp-part-of-speech-labels
	public String getHumanReadableName(String tag){
		switch(tag){
		case("WRB"):
			return "Whadverb";
		case("WP$"):
			return "Possessive whpronoun";
		case("WP"):
			return "Whpronoun";
		case("WDT"):
			return "Whdeterminer";
		case("VBZ"):
			return "Verb, 3rd person Singular Present";
		case("VBP"):
			return "Verb, non 3rd person singular present";
		case("VBN"):
			return "Verb, past participle";
		case("VBG"):
			return "Verb, gerund or present participle";
		case("VBD"):
			return "Verb, past tense";
		case("VB"):
			return "Verb, base form";
		case("UH"):
			return "Interjection";
		case("TO"):
			return "To";
		case("SYM"):
			return "Symbol";
		case("RP"):
			return "Particle";
		case("RBS"):
			return "Adverb, superlative";
		case("RBR"):
			return "Adverb, comparative";
		case("RB"):
			return "Adverb";
		case("PRP$"):
			return "Possesive Pronoun";
		case("PRP"):
			return "Personal Pronoun";
		case("POS"):
			return "Possesive Ending";
		case("PDT"):
			return "Predeterminer";
		case("NNPS"):
			return "Proper noun, plural";
		case("NNP"):
			return "Proper noun, singular";
		case("NNS"):
			return "Noun, plural";
		case("CC"):
			return "Coordinating Conjunction";
		case("DT"):
			return "Determiner";
		case("EX"):
			return "Existential there";
		case("FW"):
			return "Foreign Word";
		case("IN"):
			return "Preposition or subordinating conjunction";
		case("JJ"):
			return "Adjective";
		case("JJR"):
			return "Adjective, comparative";
		case("JJS"):
			return "Adjective, superlative";
		case("LS"):
			return "List item marker";
		case("MD"):
			return "Modal";
		case("Noun"):
			return "Noun, singular or mass";
		default:
			return "Unknown";

		} 
	}


}
