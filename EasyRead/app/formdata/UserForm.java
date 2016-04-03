package formdata;


import models.User;

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
    public boolean isStudent;
    public String instUsername;


    public UserForm() {
    }

    public UserForm(String firstName, String lastName, String email, String username, String password, String passwordRepeat, Long creatorId, boolean isStudent, String instUsername) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.passwordRepeat = passwordRepeat;
        //this.c = userId;
        this.instUsername = instUsername;

        if(isStudent){
            try {
                this.creatorId = User.byUsername(this.instUsername).id;
            } catch(Exception e) {
                this.email = null;
            }

        }
        else this.creatorId = creatorId;
        this.isStudent = isStudent;
    }

}