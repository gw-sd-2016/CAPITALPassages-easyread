package formdata;

import models.User;

/**
 * Created by mgoldiner on 11/9/15.
 */
public class LoginForm {

    public String usernameOrEmail;
    public String password;


    public LoginForm() {
    }

    public LoginForm(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    public String validate() {
        User user = User.byLogin(usernameOrEmail, password);
        if (user == null) {
            return "Invalid username/email and/or password! Please try again.";
        }

        return null;
    }

}
