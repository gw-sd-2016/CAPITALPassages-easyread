package formdata;


public class UserForm {
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public String firstName;
    public String lastName;
    public String email;
    public String username;
    public String password;
    public String passwordRepeat;
    public Long creatorId;


    public UserForm() {}
    public UserForm(String firstName, String lastName, String email, String username, String password, String passwordRepeat, Long creatorId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.passwordRepeat = passwordRepeat;
//		this.userId = userId;
        this.creatorId = creatorId;
    }

//	public String validate() {
////		User user = User.byUsername(username);
////		if ((user != null) && (user.id != userId)) {
////			return "The username you specified is already in use! Please choose another and try again.";
////		}
////
////		if (email.length() > 0) {
////			User userByEmail = User.byEmail(email);
////
////			if ((userByEmail != null) && (userByEmail.id != userId)) {
////				return "The email you specified is already in use! Please choose another and try again.";
////			}
////		}
//
//		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
//		Matcher matcher = pattern.matcher(email);
//		if (!matcher.matches()) {
//			if (email.length() > 0) {
//				return "Please provide a valid email address!";
//			}
//		}
//
//		if (firstName.length() <= 0) {
//			return "Please provide a first name!";
//		}
//
//		if(firstName.length() > 255){
//			return "Please provide a shorter first name.";
//		}
//		if (lastName.length() <= 0) {
//			return "Please provide a last name!";
//		}
//		if(lastName.length() > 255){
//			return "Please provide a shorter last name.";
//		}
//		if (email.length() <= 0) {
//			return "Please provide an email address!";
//		}
//		if(email.length() > 255){
//			return "Please provide a shorter email address.";
//		}
//
//		if (username.length() <= 0) {
//			return "Please provide a desired username!";
//		}
//		if(username.length() > 255){
//			return "Please shorten the username length!";
//		}
//		if (password.length() <= 0) {
//			return "Please provide a password!";
//		}
//
//		if (!passwordRepeat.equals(password)) {
//			return "The passwords you entered do not match! Please try again.";
//		}
//		return null;
//	}

}