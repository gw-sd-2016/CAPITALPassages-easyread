package controllers;

import formdata.LoginForm;
import formdata.UserForm;
import models.*;
import net.sf.extjwnl.JWNLException;
import play.Routes;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.login;
import views.html.signup;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static play.data.Form.form;


public class Application extends Controller {

    public User loggedInUser;

    public Result index() throws JWNLException {
        return ok(index.render(session("userFirstName")));
    }

    public Result signup_submit() throws NoSuchAlgorithmException, InvalidKeySpecException {



        Form<UserForm> newUserForm = form(UserForm.class).bindFromRequest();

        //http://www.tutorialspoint.com/java/java_regular_expressions.htm
        Pattern r = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");



        if (newUserForm.hasErrors()) {
            return badRequest(signup.render(newUserForm));
        } else {
            User newUser = new User(newUserForm);

            Matcher m = r.matcher(newUser.email);

            if(!m.find()){
                flash("failure", "That doesn't look like an email address.");
                return ok(signup.render(form(UserForm.class)));
            } else if(newUser.firstName.equals("")
                    || newUser.lastName.equals("")
                    || newUser.username.equals("")){
                flash("failure", "Sorry, you forgot to fill in some information.");
                return ok(signup.render(form(UserForm.class)));
            }
            if(newUser.creatorId != null){
                if(newUser.creatorId != 0) newUser.type = User.Role.STUDENT;
                User.create(newUser);
            } else {
                flash("failure", "Try signing up again, something went wrong.");
                return ok(signup.render(form(UserForm.class)));
            }

        }
        return ok(login.render(form(LoginForm.class)));
    }

    public Result login() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return ok(login.render(form(LoginForm.class)));
    }

    public Result logout() {
        this.loggedInUser = null;
        session().clear();
        return ok(index.render(null));
    }

    public void setAnalyzing(){
        session("analyzing", "true");

    }

    public Result login_submit() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Form<LoginForm> loginForm = form(LoginForm.class).bindFromRequest();

        LoginForm correctedForm = new LoginForm(loginForm.data().get("usernameOrEmail"), loginForm.data().get("password"));

        if (correctedForm.validate() != null) {
            flash("failure", "Incorrect Username or Password");
            return badRequest(login.render(loginForm));
        } else {
            User loggedInUser = User.byLogin(loginForm.data().get("usernameOrEmail"), loginForm.data().get("password"));
            session().clear();

            session("userId", loggedInUser.id.toString());
            session("userFirstName", loggedInUser.firstName);
            this.loggedInUser = loggedInUser;
            return ok(index.render(session("userFirstName")));
        }
    }

    public Result getLoggedInUserId(){
        return ok(String.valueOf(this.loggedInUser.id));
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
                        controllers.routes.javascript.SimplePassageController.replaceWord(),
                        controllers.routes.javascript.SimplePassageController.getSuggestions(),
                        controllers.routes.javascript.SimplePassageController.beginSentenceBreakdown(),
                        controllers.routes.javascript.SimplePassageController.savePassagePlainText(),
                        controllers.routes.javascript.SimplePassageController.savePassageHtml(),
                        controllers.routes.javascript.SimplePassageController.checkWord(),
                        controllers.routes.javascript.SimplePassageController.checkSentence(),
                        controllers.routes.javascript.SimplePassageController.createPassageHTML(),
                        controllers.routes.javascript.SimplePassageController.isAnalyzing(),
                        controllers.routes.javascript.SimplePassageController.analyzeSingularPassage(),
                        controllers.routes.javascript.SimplePassageController.moveQuestion(),
                        controllers.routes.javascript.SimplePassageController.moveChoice(),
                        controllers.routes.javascript.SimplePassageController.editChoiceAnswer(),
                        controllers.routes.javascript.SimplePassageController.setAsCorrectAnswer(),
                        controllers.routes.javascript.SimplePassageController.editPromptForQuestion(),
                        controllers.routes.javascript.SimplePassageController.addChoiceToQuestion(),
                        controllers.routes.javascript.SimplePassageController.deleteQuestion(),
                        controllers.routes.javascript.SimplePassageController.deleteChoiceForQuestion(),
                        controllers.routes.javascript.SimplePassageController.changeVisibility())
        );
    }


}