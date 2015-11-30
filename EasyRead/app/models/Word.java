package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.Expose;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Word extends Model {
	
	private static final long serialVersionUID = 1L;

	// Fields
	@Id
	@Expose
	public Long id;
	
	@Required
//	@Column(unique = true)
	@Expose
	public Long wordNetId;
	
	@Required
	@Expose
	public String lemma;

	@Required
	public int length;
	
	@Required
	public int numSyllables;
	
	@Required
	@OneToMany(cascade = CascadeType.ALL)
	public Set<Word> suggetions = new HashSet<Word>();
	
	@Required
	public double ageOfAcquisition;

	@CreatedTimestamp
	public Timestamp createdTime;
	
	@UpdatedTimestamp
	public Timestamp updatedTime;
	
	@Required
	public boolean disavowed = false;
	
	@Required
	@OneToOne(cascade = CascadeType.ALL)
	public List<POS> partsOfSpeech;

	
	// Constructors
	public Word() {}
	public Word(Long wordNetId, String lemma, int length, int numSyllables, double ageOfAcquisition, double familiarity, boolean hasPronunciation) {
		this.wordNetId = wordNetId;
		this.lemma = lemma;
		this.length = length;
		this.numSyllables = numSyllables;
		this.ageOfAcquisition = ageOfAcquisition;
	}

	// Getters / Setters
	public static Finder<Long, Word> find = new Finder<Long, Word>(Long.class, Word.class);

	public static List<Word> all() {
		return find.where().ne("disavowed", true).findList();
	}
	
	public static Word create(Word word) {
		word.save();
//		word.saveManyToManyAssociations("tags");
		return word;
	}

	public static void delete(Long id) {
//		find.ref(id).delete();
		Word word = find.ref(id);
		if (word == null) {
			return;
		}
		
		word.disavowed = true;
		word.save();
	}
	
	public static Word byId(Long id) {
		return find.where().ne("disavowed", true).eq("id", id).findUnique();
	}
	
	public static Word byId(int id) {
		return find.where().ne("disavowed", true).eq("id", id).findUnique();
	}
	
	public static Word byId(String id) {
		if (id == null) {
			return null;
		}
		
		if (id.equals("")) {
			return null;
		}
		
		return byId(Long.parseLong(id));
	}
	
//	public static Word byWordNetId(Long wordNetId) {
//		return find.where().ne("disavowed", true).eq("wordNetId", wordNetId).findUnique();
//	}
	
	public static List<Word> byPartialLemma(String partialLemma, int maxResults) {
		List<Word> results = find.where()
				.ne("disavowed", true)
				.like("lemma", partialLemma + "%")
				.findList();
		int max = maxResults;
		if (results.size() < max) {
			max = results.size();
		}
		return results.subList(0, max);
	}
	
	public static Word byLemma(String lemma) {
		return find.where().eq("lemma", lemma).findUnique();
	}
	
	
//	public  tagString(Word word) {
//		return find.where()
//				.in("phonemeTagAppearances.word.id", id)
//				.findSet();
//	}


	/********************************
	 ENUMERATOR: For each User type
	 ********************************/
	public static enum Role {
		SUPERADMIN,
		ADMIN,
		INSTRUCTOR,
		STUDENT;

		public static Role getRole(String roleString) {
			Role role = null;

			switch(roleString) {
				case "Super Administrator":
					role = SUPERADMIN;
					break;
				case "Administrator":
					role = ADMIN;
					break;
				case "Instructor":
					role = INSTRUCTOR;
					break;
				case "Student":
					role = STUDENT;
					break;
			}

			return role;
		}
	}





}