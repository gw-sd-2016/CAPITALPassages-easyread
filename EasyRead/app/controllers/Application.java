package controllers;

import formdata.LoginForm;
import formdata.UserForm;
import models.PassageTag;
import models.SimplePassage;
import models.User;
import net.sf.extjwnl.JWNLException;
import play.Routes;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.login;
import views.html.signup;
import views.html.viewAllPassageTags;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static play.data.Form.form;


public class Application extends Controller {

    public Result index() throws JWNLException {
        return ok(index.render(session("userFirstName")));
    }

    public Result signup_submit() throws NoSuchAlgorithmException, InvalidKeySpecException {

        Form<UserForm> newUserForm = form(UserForm.class).bindFromRequest();

        if (newUserForm.hasErrors()) {
            return badRequest(signup.render(newUserForm));
        } else {
            User newUser = new User(newUserForm);
            User.create(newUser);
        }
        return ok(login.render(form(LoginForm.class)));
    }

    public Result login() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return ok(login.render(form(LoginForm.class)));
    }

    public Result logout() {
        session().clear();
        return ok(index.render(null));
    }

    public Result login_submit() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Form<LoginForm> loginForm = form(LoginForm.class).bindFromRequest();

        LoginForm correctedForm = new LoginForm(loginForm.data().get("usernameOrEmail"), loginForm.data().get("password"));

        if (correctedForm.validate() != null) {
            return badRequest(login.render(loginForm));
        } else {
            User loggedInUser = User.byLogin(loginForm.data().get("usernameOrEmail"), loginForm.data().get("password"));
            session().clear();

            session("userId", loggedInUser.id.toString());
            session("userFirstName", loggedInUser.firstName);
            return ok(index.render(session("userFirstName")));
        }
    }

    public Result signup() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return ok(signup.render(form(UserForm.class)));
    }


    public Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("jsRoutes",
                        controllers.routes.javascript.SimplePassageController.analyzePassages(),
                        controllers.routes.javascript.SimplePassageController.deletePassage(),
                        controllers.routes.javascript.SimplePassageController.acceptWord(),
                        controllers.routes.javascript.SimplePassageController.replaceWord()/*,
                        controllers.routes.javascript.SimplePassageController.deletePassageQuestion()
                        controllers.routes.javascript.SimplePassageController.deletePassageQuestionChoice()*/

                )
        );
    }

    public static Result viewAllPassageTags() {
        return ok(viewAllPassageTags.render(PassageTag.all()));
    }

}