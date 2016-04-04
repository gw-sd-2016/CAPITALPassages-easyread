package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import formdata.UserForm;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import utilities.PasswordHash;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import forms.UserForm;
// CODE FOR THIS CLASS TAKEN FROM ORIGINAL ReMind Repo

@Entity
public class User extends Model {


    /********************************
     * ENUMERATOR: For each User type
     ********************************/
    public enum Role {
        SUPERADMIN,
        ADMIN,
        INSTRUCTOR,
        STUDENT;

        public static Role getRole(String roleString) {
            Role role = null;

            switch (roleString) {
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


    /********************************
     * FIELDS
     ********************************/
    /* Universal */
	/*===========*/
    @Id
    public Long id;

    @Required
    public boolean retired = false;

    @CreatedTimestamp
    public Timestamp createdTime;

    @UpdatedTimestamp
    public Timestamp updatedTime;


	/* Specific */
	/*===========*/

    @Required
    public String firstName;

    @Required
    public String lastName;

    @Required
    @Column(unique = true)
    public String username;

    @Required
    public String password;    // hashed using utilities.PasswordHash

    @Email
    public String email;

    @Required
    public Role type;

    public Long creatorId;

 /*   @ManyToOne (cascade = CascadeType.ALL)
    public Institution institution;*/


    /********************************
     * CONSTRUCTORS
     ********************************/
    public User(String email, String username, String password, String firstName, String lastName, Role type, Long creatorId) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.email = email;
        this.username = username;
        this.password = PasswordHash.createHash(password);
        this.firstName = firstName;
        this.lastName = lastName;
        this.type = type;
        this.creatorId = creatorId;
    }

    public User(play.data.Form<UserForm> userForm) throws InvalidKeySpecException, NoSuchAlgorithmException {

        Map<String, String> formData = userForm.data();

        this.email = formData.get("email");
        this.username = formData.get("username");
        this.password = PasswordHash.createHash(formData.get("password"));
        this.firstName = formData.get("firstName");
        this.lastName = formData.get("lastName");
        this.type = Role.INSTRUCTOR;

        if(formData.get("isStudent").equals("true")){
            try {
                this.creatorId = User.byUsername(formData.get("instUsername")).id;
            } catch(Exception e) {
                this.creatorId = null;
            }

        } else this.creatorId = Long.valueOf(0);


        //   this.creatorId = Long.valueOf(formData.get("creatorId"));
        // this.institution = User.byId(userForm.creatorId).institution;
    }


    /********************************
     * FINDER
     ********************************/
    //Initialize Ebean Finder
    public static Finder<Long, User> find = new Finder<Long, User>(User.class);


    /********************************
     * CREATE / DELETE
     ********************************/
    public static User create(User user) {
        user.save();
        return user;
    }

    public static void delete(Long id) {
        User user = find.ref(id);
        if (user == null) {
            return;
        }

        user.retired = true;
        user.save();
    }


    /********************************
     * VALIDATION
     ********************************/

    //check if a user is in the database (i.e. the entity is not null)
    public static boolean exists(User user) {
        return user != null;
    }

    //check if a user has the proper permissions to view a page
    public static boolean hasPermission(String userId, List<Boolean> pflags) {
        User user = User.byId(Long.parseLong(userId));

        List<User.Role> roles = new ArrayList<>();
        roles.add(User.Role.values()[0]); //SA always has permission
        for (int i = 0; i < pflags.size(); i++) {
            if (pflags.get(i)) {
                roles.add(User.Role.values()[i + 1]);
            }
        }

        if (roles.contains(user.type)) {
            return true;
        } else {
            System.out.println("User type = " + user.type.toString() + " does NOT have permission!");
        }
        return false;
    }


    /********************************
     * GETTERS
     ********************************/

    //-----------Single-------------//

    //Get User by ID (Long)
    public static User byId(Long id) {
        return find.where()
                .ne("retired", true)
                .eq("id", id)
                .findUnique();
    }

    //Get User by ID (String)
    public static User byId(String idString) {
        Long id = Long.parseLong(idString);
        return find.where()
                .ne("retired", true)
                .eq("id", id)
                .findUnique();
    }

    //Get User by email address
    public static User byEmail(String email) {
        return find.where()
                .ne("retired", true)
                .eq("email", email)
                .findUnique();
    }

    //Get User by username/email and password
    public static User byLogin(String usernameOrEmail, String password) {
        User user = byUsername(usernameOrEmail);
        if (user == null) {
            user = byEmail(usernameOrEmail);
            if (user == null) {
                return null;
            }
        }

        try {
            if (!PasswordHash.validatePassword(password, user.password)) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        return user;
    }

    //Get User by username
    public static User byUsername(String username) {
        return find.where()
                .eq("username", username)
                .findUnique();
    }


    //Get User by username OR email address (used for logging in)
    public static User byUsernameOrEmail(String usernameOrEmail) {
        return find.where()
                .ne("retired", true)
                .or(Expr.eq("username", usernameOrEmail), Expr.eq("email", usernameOrEmail))
                .findUnique();
    }

    //-----------Group-------------//

    //Get all Users in the system
    public static List<User> getAll() {
        return find.where()
                .ne("retired", true)
                .findList();
    }

    // Get all Users in the system by role
    public static List<User> getAllByRole(String role) {
//		if (role.equals("Student")) {
        return find.where()
                .ne("retired", true)
                .eq("type", Role.getRole(role))
                .orderBy().asc("lastName")
                .findList();
//		} else {
//			//Because permissions cascade upwards, getting all Instructors
//			// should also get all Admins and SAs, etc.
//			return find.where()
//						.ne("retired", true)
//						.le("type", Role.getRole(role))
//						.orderBy().asc("lastName")
//					.findList();
//		}
    }


    // Get all instructor Users at a given institution
    public static List<User> getAllInstructorsForInstitution(Long institutionId) {
        return find.where()
                .eq("retired", false)
                .eq("type", Role.INSTRUCTOR)
                .eq("institution_id", institutionId)
                .eq("institution.retired", false)
                .findList();
    }


    // Get all student Users at a given institution
    public static List<User> getAllStudentsForInstitution(Long institutionId) {
        System.out.print(institutionId);
        String sql = "select * from user" +
                " where id in " +
                "(select student_id from student_in_institution" +
                " where institution_id = " + institutionId + "" +
                " and retired=0)" +
                " and retired=false";

        List<User> allStudents = new ArrayList<User>();
        for (SqlRow row : Ebean.createSqlQuery(sql).findList()) {
            allStudents.add(User.byId(row.getLong("id")));
        }

        return allStudents;
    }

    // Get all student Users in a given course
    public static List<User> getAllStudentsForCourse(Long courseId) {
        String sql = "select * from user" +
                " where retired=0" +
                " and type=" + Role.STUDENT.ordinal() +
                " and id in " +
                "(select user_id from course_user " +
                "where retired=0 " +
                "and course_id=" + courseId + ")";

        List<User> allStudents = new ArrayList<User>();
        for (SqlRow row : Ebean.createSqlQuery(sql).findList()) {
            allStudents.add(User.byId(row.getLong("id")));
        }

        return allStudents;
    }

    //Aidan and Dhruv
    // Returns all Students for a given Institution where the student is not in the specified course
    public static List<User> getAllStudentsForInstitutionNotInCourse(Long institutionId, Long courseId) {
        String sql = "select * from user " +
                "where id in " +
                "(select student_id from student_in_institution" +
                " where institution_id = " + institutionId +
                " and retired=false) " +
                "and id not in " +
                "(select user_id from course_user" +
                " where course_id = " + courseId + ") " +
                "and retired=false";

        List<User> students = new ArrayList<User>();
        for (SqlRow row : Ebean.createSqlQuery(sql).findList()) {
            students.add(User.byId(row.getLong("id")));
        }
        return students;
    }

    //Aidan
    // Returns all Students that belong to a given question list
    public static List<User> getAllStudentsForQuestionList(Long questionListId) {
        //when to put retired?
        String sql = "select * from user " +
                "where id in " +
                "(select user_id from question_list_user" +
                " where question_list_id = " + questionListId + ") " +
                "and retired=0";

        List<User> students = new ArrayList<User>();
        for (SqlRow row : Ebean.createSqlQuery(sql).findList()) {
            students.add(User.byId(row.getLong("id")));
        }
        return students;
    }


    ////////////////////////////////////////////
    //TEST
    ////////////////////////////////////////////
    public static class NameGenerator {

        private List<String> vocals = new ArrayList<String>();
        private List<String> startConsonants = new ArrayList<String>();
        private List<String> endConsonants = new ArrayList<String>();
        private List<String> nameInstructions = new ArrayList<String>();

        public NameGenerator() {
            String demoVocals[] = {"a", "e", "i", "o", "u", "ei", "ai", "ou", "j",
                    "ji", "y", "oi", "au", "oo"};

            String demoStartConsonants[] = {"b", "c", "d", "f", "g", "h", "k",
                    "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "z",
                    "ch", "bl", "br", "fl", "gl", "gr", "kl", "pr", "st", "sh",
                    "th"};

            String demoEndConsonants[] = {"b", "d", "f", "g", "h", "k", "l", "m",
                    "n", "p", "r", "s", "t", "v", "w", "z", "ch", "gh", "nn", "st",
                    "sh", "th", "tt", "ss", "pf", "nt"};

            String nameInstructions[] = {"vd", "cvdvd", "cvd", "vdvd"};

            this.vocals.addAll(Arrays.asList(demoVocals));
            this.startConsonants.addAll(Arrays.asList(demoStartConsonants));
            this.endConsonants.addAll(Arrays.asList(demoEndConsonants));
            this.nameInstructions.addAll(Arrays.asList(nameInstructions));
        }

        /**
         * The names will look like this
         * (v=vocal,c=startConsonsonant,d=endConsonants): vd, cvdvd, cvd, vdvd
         *
         * @param vocals          pass something like {"a","e","ou",..}
         * @param startConsonants pass something like {"s","f","kl",..}
         * @param endConsonants   pass something like {"th","sh","f",..}
         */
        public NameGenerator(String[] vocals, String[] startConsonants,
                             String[] endConsonants) {
            this.vocals.addAll(Arrays.asList(vocals));
            this.startConsonants.addAll(Arrays.asList(startConsonants));
            this.endConsonants.addAll(Arrays.asList(endConsonants));
        }

        /**
         * see {@link NameGenerator#NameGenerator(String[], String[], String[])}
         *
         * @param vocals
         * @param startConsonants
         * @param endConsonants
         * @param nameInstructions Use only the following letters:
         *                         (v=vocal,c=startConsonsonant,d=endConsonants)! Pass something
         *                         like {"vd", "cvdvd", "cvd", "vdvd"}
         */
        public NameGenerator(String[] vocals, String[] startConsonants,
                             String[] endConsonants, String[] nameInstructions) {
            this(vocals, startConsonants, endConsonants);
            this.nameInstructions.addAll(Arrays.asList(nameInstructions));
        }

        public String getName() {
            return firstCharUppercase(getNameByInstructions(getRandomElementFrom(nameInstructions)));
        }

        private int randomInt(int min, int max) {
            return (int) (min + (Math.random() * (max + 1 - min)));
        }

        private String getNameByInstructions(String nameInstructions) {
            String name = "";
            int l = nameInstructions.length();

            for (int i = 0; i < l; i++) {
                char x = nameInstructions.charAt(0);
                switch (x) {
                    case 'v':
                        name += getRandomElementFrom(vocals);
                        break;
                    case 'c':
                        name += getRandomElementFrom(startConsonants);
                        break;
                    case 'd':
                        name += getRandomElementFrom(endConsonants);
                        break;
                }
                nameInstructions = nameInstructions.substring(1);
            }
            return name;
        }

        private String firstCharUppercase(String name) {
            return Character.toString(name.charAt(0)).toUpperCase() + name.substring(1);
        }

        private String getRandomElementFrom(List<String> v) {
            return v.get(randomInt(0, v.size() - 1));
        }
    }

    public static double percentageCorrect(Long id){
        List<PassageQuestionResponse> res = PassageQuestionResponse.byUser(id);

        int size = res.size();

        int total = 0;

        for(PassageQuestionResponse response : res){
            if(PassageQuestionChoice.byId(response.entity_id).correct) total++;
        }

        if(size == 0) return 0;
        return total/size;
    }

/*
    public static void createSomeUsers(int numUsers, Role role) {
        for (int i = 0; i < numUsers; i++) {
            NameGenerator ng = new NameGenerator();
            String name = ng.getName();

            Random r = new Random();

            User user;
            if (User.byUsername(name) != null) {
                continue;
            }
            try {
                user = User.create(new User(name.toLowerCase() + "@email.com", name.toLowerCase(), name.toLowerCase(), name, ng.getName(), role, Long.valueOf(0)));
                int instId = r.nextInt((Institution.getAll().size() - 1) + 1) + 1;
                if (role != Role.STUDENT) {
                    user.institution = Institution.byId(Long.valueOf(instId));
                }
//				} else {
//					Institution inst = Institution.byId(Long.valueOf(instId));
//					StudentInInstitution.create(new StudentInInstitution(user.id, inst.id, true));
//				}
                user.save();
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
    }
*/
}