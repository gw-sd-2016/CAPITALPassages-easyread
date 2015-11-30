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
import java.util.ArrayList;
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
	public String name;

	@Expose
	public String description;
	
	@Expose
	public String type;

	@CreatedTimestamp
	Timestamp createdTime;
	
	@UpdatedTimestamp
	Timestamp updatedTime;
	
	@Required
	public boolean disavowed = false;
	
	// Constructors
	public PassageTag(String name, String description, String type) {
		this.name = name;
		this.description = description;
		this.type = type;
	}
	
	// Getters / Setters
	public static Finder<Long, PassageTag> find = new Finder<Long, PassageTag>(Long.class, PassageTag.class);
	
	public static List<PassageTag> all() {
		return find.where().ne("disavowed", true).findList();
	}
	
	public static List<String> allTypes(){
		List<String> types = new ArrayList<String>(); 
		for(PassageTag p : all()){
			if(!types.contains(p.type)) types.add(p.type);
		}
		return types; 
	}
	
	public static List<String> allNames(){
		List<String> names = new ArrayList<String>(); 
		for(PassageTag p : all()){
			if(!names.contains(p.name)) names.add(p.name);
		}
		return names; 
	}
	
	public static PassageTag create(PassageTag tag) {
		tag.save();
		return tag;
	}
	
	

	public static void delete(Long id) {
//		find.ref(id).delete();
		PassageTag phonemeTag = find.ref(id);
		phonemeTag.disavowed = true;
		phonemeTag.save();
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
	
	public static PassageTag byName(String name) {
		return find.where()
				.ne("disavowed", true)
				.eq("name", name)
				.findUnique();
	}
	
	public static List<PassageTag> byType(String type) {
		return find.where()
				.ne("disavowed", true)
				.eq("type", type)
				.orderBy("name")
				.findList();
	}
	
	public static List<PassageTag> byType_number(String type) {
		return find.where()
				.ne("disavowed", true)
				.eq("type", type)
				.orderBy("name+0")
				.findList();
	}
	
	public String getType(){
		return this.type;
	}

}
