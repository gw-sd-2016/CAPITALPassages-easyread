package controllers;

import formdata.PassageQuestionAnswerData;
import formdata.SimplePassageData;
import formdata.SimplePassageNumQuestionsData;
import models.*;
import net.sf.extjwnl.JWNLException;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import java.util.*;

import static play.data.Form.form;

public class SimplePassageController extends Controller {

    private ParsingController parsingController;
    private PassageAnalysisController analysisController;

    // avoid fetching difficulties per sentence more than once
    private ArrayList<Double> difficultiesCache;

    // determines whether or not spinner is active in UI
    public static boolean inProgress = false;

    // New Passage Methods

    /**
     * Renders Passage Creation Form
     * @return routed to form
     * @throws JWNLException
     */
    public Result createSimplePassage() throws JWNLException {
        if(session("userFirstName") == null) return ok(index.render(null));

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            return ok(createSimplePassage.render(form(SimplePassageData.class)));
        } else return badRequest();
    }

    /**
     * Method called on submission of Passage Creation form
     * @return Renders table view of all passages on finish
     */
    public Result createSimplePassage_submit() {
        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            if(session("userFirstName") == null) return ok(index.render(null));
            Form<SimplePassageData> createSPForm = form(SimplePassageData.class).bindFromRequest();

            if (createSPForm.hasErrors()) {
                return badRequest(createSimplePassage.render(createSPForm));
            }

            SimplePassage newPassage = new SimplePassage(createSPForm.get());
            newPassage.instructorID = Integer.valueOf(session("userId"));


            newPassage.save();
            return redirect(routes.SimplePassageController.viewAllPassages());
        } else return badRequest();
    }


    /**
     * Allows to be saved with HTML formatting from ajax call
     * @param text - String representing Passage Text
     * @param title - String representing Passage Title
     * @param source - String representing Passage Source
     * @return Routes user to Manage Passages page
     */
    public Result createPassageHTML(String text, String title, String source) {
        if(session("userFirstName") == null) return ok(index.render(null));


        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            SimplePassage p = new SimplePassage(text, title, source);
            p.original = true;
            p.visibleToStudents = true;

            Map<String, String[]> parameters = request().body().asFormUrlEncoded();


            String html = parameters.get("html")[0];

            String str = parameters.get("tags")[0];

            char[] arr = str.toCharArray();

            ArrayList<String> tags = new ArrayList<String>();

            int begin = str.indexOf("[") + 1;
            for(int i = begin ; i < arr.length; i++){
                if(arr[i] == ',' || i + 1 == arr.length){
                    tags.add(str.substring(begin, i).replace("\"", ""));
                    begin = i + 1;
                }
            }


            p.tags = new HashSet<PassageTag>();

            for(String s : tags){
                p.tags.add(new PassageTag(s));
            }

            PassageText pt = new PassageText(0, html);

            p.htmlRepresentations = new HashSet<PassageText>();
            p.htmlRepresentations.add(pt);

            p.instructorID = Long.valueOf(session("userId"));

            p.save();
            return viewAllPassages();
        } else {
            return ok(index.render(session("userFirstName")));
        }


    }

    /**
     * Allows an instructor to hide of show passages to their students
     * @param passageId - Id of the Passage Object you are operating on
     * @return Routes user back to manage passages page
     */
    public Result changeVisibility(Long passageId){

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            try{
                SimplePassage p = SimplePassage.byId(passageId);
                p.visibleToStudents = !p.visibleToStudents;
                p.save();
                return ok();
            } catch (Exception e){
                return badRequest();
            }
        } else {
            return badRequest();
        }
    }


    /**
     * This method is called on submission of the edit passage form
     * @param passageId Id of Passage being edited
     * @return Success or failure of edit
     */
    public Result edit_submit(Long passageId) {
        if(session("userFirstName") == null) return ok(index.render(null));


        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){

            SimplePassage passage = SimplePassage.byId(passageId);
            if (passage == null) {
                return redirect(routes.SimplePassageController.viewAllPassages());
            }

            Form<SimplePassageData> form = Form.form(SimplePassageData.class).bindFromRequest();

            if (form.hasErrors()) {
                return badRequest(editSimplePassageAtGrade.render(form, passageId, SimplePassage.byId(passageId).grade));
            }


            SimplePassageData data = form.get();

            PassageText current = PassageText.bySimplePassageAndGrade(passageId, data.grade);
            current.html = data.passageText;


            passage.text = data.passageText;
            passage.grade = data.grade;
            passage.source = data.source;

            passage.title = data.passageTitle;


            passage.sentences = null;


            passage.save();

            flash("success", "Modified Passage details have been saved.");
            return redirect(routes.SimplePassageController.viewS(passageId, passage.grade));
        } else {
            return ok(index.render(session("userFirstName")));
        }

    }


    // QUESTION CREATION

    /**
     * This method allows us to render the question form properly with the correct number of question fields
     * The actual layout is handled by Javascript
     * @param passageId Passage to add Questions too
     * @return Success or Failure of setting Questions
     */
    public Result setNumQuestions(Long passageId) {
        if(session("userFirstName") == null) return ok(index.render(null));

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            return ok(setNumberOfQuestionsForPassage.render(form(SimplePassageNumQuestionsData.class), passageId));
        } else {
            return ok(index.render(session("userFirstName")));
        }

    }

    /**
     * This method runs when number of questions is set
     * @return Success or failure of setting number of Questions
     */
    public Result setNumQuestions_submit() {

        if(session("userFirstName") == null) return ok(index.render(null));


        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            Form<SimplePassageNumQuestionsData> createSPForm = form(SimplePassageNumQuestionsData.class).bindFromRequest();

            if (createSPForm.hasErrors()) {
                return badRequest(setNumberOfQuestionsForPassage.render(createSPForm, createSPForm.get().passageId));
            }

            SimplePassage passage = SimplePassage.byId(createSPForm.get().passageId);

            if (passage.questions == null) passage.questions = new HashSet<PassageQuestion>();


            // getting what the next questionId will be
            Long qID = Long.valueOf("0");

            for (PassageQuestion pq : PassageQuestion.find.all()) {
                if (pq.id > qID) qID = pq.id;

            }




            // getting what the next choiceId Will be.
            Long cID = Long.valueOf("0");
            for (PassageQuestionChoice c : PassageQuestionChoice.find.all()) {
                if (c.id > cID) cID = c.id;
            }
            cID++;
            qID++;

            System.out.println("Question ID: " + qID);


            for (int i = 0; i < createSPForm.get().questions.size(); i++) {
                if (i != 0) cID++;

                String a = createSPForm.get().questions.get(i);

                PassageQuestion q = new PassageQuestion(createSPForm.get().passageId, true);

                q.position = passage.questions.size();

                PassageQuestionPrompt testPrompt = new PassageQuestionPrompt(q, a);
                testPrompt.questionId = qID + i;

                testPrompt.save();

                q.prompt = testPrompt;

                if(PassageQuestion.find.all().size() == 0){
                    passage.questions.add(q);
                    passage.save();

                    qID = Long.valueOf(-1);
                    for(PassageQuestion ques : passage.questions){
                        qID = Math.max(ques.id, qID);
                    }
                }

                testPrompt.questionId = qID + i;

                testPrompt.save();


                q.choices = new HashSet<PassageQuestionChoice>();

                for (int c = 0; c < createSPForm.get().choices.get(i).size(); c++) {
                    PassageQuestionChoice w = new PassageQuestionChoice();

                    if(PassageQuestionChoice.find.all().size() == 0){
                        q.choices.add(w);
                        q.save();

                        for(PassageQuestionChoice choice : q.choices){
                            cID = Math.max(choice.id, cID);
                        }
                    }


                    if (createSPForm.get().correctAnswers.size() > 0 && createSPForm.get().correctAnswers.get(i).equals(String.valueOf(c))) {
                        w.correct = true;
                        q.correctAnswer = createSPForm.get().choices.get(i).get(c);
                    } else {
                        w.correct = createSPForm.get().correctAnswers.size() == 0 && createSPForm.get().choices.get(i).size() == 1;
                    }
                    w.active = true;
                    w.position = c;

                    PassageQuestionAnswer answer = new PassageQuestionAnswer(w, createSPForm.get().choices.get(i).get(c));
                    if (c == 0) {
                        cID = cID + c;
                        answer.choiceId = cID;
                    } else answer.choiceId = ++cID;

                    answer.save();
                    w.answer = answer;


                    if(PassageQuestionChoice.find.all().size() != 0) {
                        q.choices.add(w);
                    }


                }


                if(PassageQuestion.find.all().size() != 0) {
                    passage.questions.add(q);
                }



            }



            passage.save();


            return ok(viewPassageQuestions.render(passage.id));

        } else {
            return ok(index.render(session("userFirstName")));
        }

    }

    /**
     * Renders the add questions page with existing questions already filled id
     * @param passageId Id of Passage you are editing questions in
     * @return rendered question form filled in
     */
    public Result editQuestions(Long passageId) {
        if(session("userFirstName") == null) return ok(index.render(null));


        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            return ok(editPassageQuestions.render(SimplePassage.byId(passageId), User.byId(session("userId"))));
        } else {
            return ok(index.render(session("userFirstName")));
        }



    }

    /**
     * Renders table view of an instructors students and the answers they've given to assigned questions
     * @return Rendered Page
     */
    public Result viewStudentAnswers(){
        if(session("userFirstName") == null) return ok(index.render(null));

        List<User> students = new ArrayList<User>();
        User currentUser = User.byId(Long.valueOf(session("userId")));
        if(currentUser.creatorId == 0){
            for(User u : User.getAll()){
                if(u.creatorId == currentUser.id){
                    students.add(u);
                }
            }
            return ok(viewStudentAnswers.render(students));
        } else return badRequest();
    }


    /**
     * This method is called when a change is made to the choices tied to a Question
     * @param questionId Id of the Question you are editing
     * @return Success or Failure of edit
     */
    public Result editQuestionChoices(Long questionId) {
        if(session("userFirstName") == null) return ok(index.render(null));


        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            return ok(editPassageQuestionsChoices.render(PassageQuestion.byId(questionId), User.byId(session("userId"))));
        } else {
            return ok(index.render(session("userFirstName")));
        }



    }

    /**
     * This method is called on submission of Questions form
     * values are populated in the javascript and mapped to form
     * @param passageId Id of Passage questions are being edited for
     * @return Success or Failure of submission
     */
    public Result editQuestions_submit(Long passageId) {
        if(session("userFirstName") == null) return ok(index.render(null));


        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            Form<SimplePassageNumQuestionsData> createSPForm = form(SimplePassageNumQuestionsData.class).bindFromRequest();

            if (createSPForm.hasErrors()) {
                return badRequest(setNumberOfQuestionsForPassage.render(createSPForm, createSPForm.get().passageId));
            }

            SimplePassage passage = SimplePassage.byId(createSPForm.get().passageId);


            // cleanup previous Qs
            for (PassageQuestion pq : passage.questions) {

                for (PassageQuestionChoice pqc : pq.choices) {
                    PassageQuestionAnswer.byPassageQuestionChoice(pqc.id).get(0).delete("");
                }

                PassageQuestionPrompt.byPassageQuestion(pq.id).get(0).delete("");


                pq.delete(""); // takes care of questions and choices

            }


            passage.questions = new HashSet<PassageQuestion>();


            // getting what the next questionId will be
            Long qID = Long.valueOf("0");

            for (PassageQuestion pq : PassageQuestion.find.all()) {
                if (pq.id > qID) qID = pq.id;

            }


            // getting what the next choiceId Will be.
            Long cID = Long.valueOf("0");
            for (PassageQuestionChoice c : PassageQuestionChoice.find.all()) {
                if (c.id > cID) cID = c.id;
            }
            cID++;
            qID++;

            System.out.println("Question ID: " + qID);

            for (int i = 0; i < createSPForm.get().questions.size(); i++) {
                if (i != 0) cID++;

                String a = createSPForm.get().questions.get(i);

                PassageQuestion q = new PassageQuestion(createSPForm.get().passageId, true);

                q.position = passage.questions.size();

                PassageQuestionPrompt testPrompt = new PassageQuestionPrompt(q, a);
                testPrompt.questionId = qID + i;

                testPrompt.save();

                q.prompt = testPrompt;

                q.choices = new HashSet<PassageQuestionChoice>();

                for (int c = 0; c < createSPForm.get().choices.get(i).size(); c++) {
                    PassageQuestionChoice w = new PassageQuestionChoice();


                    if (createSPForm.get().correctAnswers.size() > 0 && createSPForm.get().correctAnswers.get(i).equals(String.valueOf(c))) {
                        w.correct = true;
                    } else {
                        w.correct = createSPForm.get().correctAnswers.size() == 0 && createSPForm.get().choices.get(i).size() == 1;
                    }
                    w.active = true;
                    w.position = c;

                    PassageQuestionAnswer answer = new PassageQuestionAnswer(w, createSPForm.get().choices.get(i).get(c));
                    if (c == 0) {
                        cID = cID + c;
                        answer.choiceId = cID;
                    } else answer.choiceId = ++cID;

                    answer.save();
                    w.answer = answer;

                    q.choices.add(w);

                }


                passage.questions.add(q);


            }


            System.out.println("Size : " + passage.questions.size());


            passage.save();

            return ok(viewPassageQuestions.render(passage.id));
        } else {
            return ok(index.render(session("userFirstName")));
        }
    }

    /**
     * This method is called when questions are being reordered
     * @param questionId Question Being Edited
     * @param up Move Question forward or backward
     * @return Success or Failure of Move
     */
    public Result moveQuestion(Long questionId, boolean up){
        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            try{
                PassageQuestion q = PassageQuestion.byId(questionId);
                PassageQuestion other;
                if(up){
                    other = PassageQuestion.byPassageAndPosition(q.basis_id, q.position - 1);
                } else {
                    other = PassageQuestion.byPassageAndPosition(q.basis_id, q.position + 1);
                }
                int temp = q.position;
                q.position = other.position;
                other.position = temp;

                q.save();
                other.save();
                return ok();
            } catch(Exception e){
                return badRequest();
            }
        } else return badRequest();
    }

    /**
     * This method is called when the choices for a question are being reordered
     * @param choiceId Choice being edited
     * @param questionId Question within which choices are being edited
     * @param up Move Choice forward or backward
     * @return
     */
    public Result moveChoice(Long choiceId, Long questionId, boolean up){

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            try{
                PassageQuestionChoice c = PassageQuestionChoice.byId(choiceId);
                PassageQuestionChoice other;
                if(up){
                    other = PassageQuestionChoice.byQuestionAndPosition(questionId, c.position - 1);
                } else {
                    other = PassageQuestionChoice.byQuestionAndPosition(questionId, c.position + 1);
                }
                int temp = c.position;
                c.position = other.position;
                other.position = temp;

                c.save();
                other.save();
                return ok();
            } catch(Exception e){
                return badRequest();
            }
        } else return badRequest();
    }

    /**
     * Changes the Answer for a given question Choice
     * @param choiceId Id of Choice being edited
     * @param questionId Id of Question for this Choice
     * @param newText Text of the Answer being assigned
     * @return Success or Failure or Edit
     */
    public Result editChoiceAnswer(Long choiceId, Long questionId, String newText){
        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            try{
                PassageQuestion q = PassageQuestion.byId(questionId);

                for(PassageQuestionChoice c : q.choices){
                    if(c.id == Long.valueOf(choiceId)){
                        PassageQuestionAnswer answer = PassageQuestionAnswer.byPassageQuestionChoice(c.id).get(0);
                        answer.text = newText;
                        answer.save();

                        break;
                    }
                }


                return ok();
            } catch(Exception e){
                return badRequest();
            }
        } else return badRequest();
    }


    /**
     * Sets a Choice as the correct Answer for the Question
     * @param choiceId Id of Choice being set as correct
     * @param questionId Id of Question where this choice exists
     * @return Success or Failure of setting correct answer
     */
    public Result setAsCorrectAnswer(Long choiceId, Long questionId){
        try{
            PassageQuestionChoice c = PassageQuestionChoice.byId(choiceId);


            PassageQuestion ques = PassageQuestion.byId(questionId);

            for(PassageQuestionChoice choice : ques.choices){
                if(choice.correct){
                    choice.correct = false;
                    choice.save();
                    break;
                }
            }

            ques.correctAnswer = PassageQuestionAnswer.byPassageQuestionChoice(c.id).get(0).text;

            c.correct = true;

            ques.save();
            c.save();

            return ok();
        } catch(Exception e){
            return badRequest();
        }
    }


    /**
     * Change the Text that appears in the front end with a Question
     * @param questionId Id of Question being Edited
     * @param newText New Text to display with Question
     * @return Success or Failure of edit
     */
    public Result editPromptForQuestion(Long questionId, String newText){
        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            try{
                PassageQuestionPrompt prompt = PassageQuestionPrompt.byPassageQuestion(questionId).get(0);

                prompt.text = newText;

                prompt.save();

                return ok();
            } catch(Exception e){
                return badRequest();
            }
        } else return badRequest();
    }

    /**
     * Helper method for adding a choice to a question from front end form
     * @param questionId Question Being Updated
     * @param newChoice Text to associate as the Answer for this Choice
     * @param isCorrect boolean representing whether this is the correct answer or not
     * @return Succes or Failure of Add
     */
    public Result addChoiceToQuestion(Long questionId, String newChoice, boolean isCorrect){

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            try{
                PassageQuestion ques = PassageQuestion.byId(questionId);

                PassageQuestionChoice choice = new PassageQuestionChoice(ques.basis_id, isCorrect, true);

                ques.choices.add(choice);

                ques.save();

                PassageQuestionAnswer answer = new PassageQuestionAnswer(choice, newChoice);

                answer.save();

                choice.answer = answer;
                choice.position = ques.choices.size();


                choice.save();
                ques.save();

                if(isCorrect) return setAsCorrectAnswer(choice.id, questionId);

                return ok();
            } catch(Exception e){
                return badRequest();
            }
        } else return badRequest();

    }

    /**
     * This method deletes Questions + associated prompts, choices, answers, responses and records
     * @param questionId Id of Question to delete
     * @param passageId Id of Passage that contains this Question
     * @return Success or Failure of delete
     */
    public Result deleteQuestion(Long questionId, Long passageId){

        try{
            SimplePassage p = SimplePassage.byId(passageId);
            PassageQuestion q = PassageQuestion.byId(questionId);

            for(PassageQuestion ques : p.questions){
                if(ques.position > q.position){
                    ques.position--;
                    ques.save();
                }
            }

            q.delete();

            return ok();
        } catch(Exception e){
            return badRequest();
        }
    }

    /**
     * Helper method to delete all the choices associated with a question
     * @param questionId Id of Question being edited
     * @param passageId Id of passage that this question is assigned to
     * @param choiceId Id of choice being altered
     * @return Success or Failure of delete
     */
    public Result deleteChoiceForQuestion(Long questionId, Long passageId, Long choiceId){

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            try{
                SimplePassage p = SimplePassage.byId(passageId);
                PassageQuestion q = PassageQuestion.byId(questionId);

                PassageQuestionChoice choice = PassageQuestionChoice.byId(choiceId);

                if(choice.correct == true){
                    flash("warning", "Can't Delete Correct Answer.");
                    return badRequest(editPassageQuestionsChoices.render(PassageQuestion.byId(questionId), User.byId(Long.valueOf(session("userId")))));
                } else if(q.choices.size() == 1){
                    flash("warning", "Can't Delete Only Choice");
                    return badRequest(editPassageQuestionsChoices.render(PassageQuestion.byId(questionId), User.byId(Long.valueOf(session("userId")))));
                }

                for(PassageQuestionChoice c : q.choices){
                    if(c.position > choice.position){
                        c.position--;
                        c.save();
                    }
                }

                choice.deepDelete();
                q.save();

                return ok();
            } catch(Exception e){
                return badRequest();
            }
        } else return badRequest();
    }

    /**
     * This method submits a page for Students where they can give answers to assigned questions
     * @param passageId id of Passage for which Questions are being attempted
     * @return Redirects to page showing which answers were correct
     */
    public Result answerPassageQuestions_submit(Long passageId) {
        if(session("userFirstName") == null) return ok(index.render(null));


        Long userID = Long.valueOf(session("userId"));
        SimplePassage passage = SimplePassage.byId(passageId);
        if (passage == null) {
            return redirect(routes.SimplePassageController.answerPassageQuestions(passageId));
        }

        Form<PassageQuestionAnswerData> form = Form.form(PassageQuestionAnswerData.class).bindFromRequest();

        if (form.hasErrors()) {
            return badRequest(answerPassageQuestions.render(form(PassageQuestionAnswerData.class), passageId));
        }


        PassageQuestionAnswerData data = form.get();

        List<PassageQuestionRecord> records = PassageQuestionRecord.all();

        for (int i = 0; i < data.answers.size(); i++) {
            if (data.answers.get(i) != "") {
                PassageQuestionRecord thisQ = null;
                //thisQ.user = User.byId(userID);
                for (PassageQuestionRecord r : records) {
                    if (r.question.position == i && r.question.basis_id == passageId) thisQ = r;
                }

                if (thisQ != null) {
                    int id = -1;
                    for (PassageQuestionChoice c : thisQ.question.choices) {

                        if (c.position == Integer.valueOf(data.answers.get(i))) {
                            id = c.id.intValue();
                        }
                    }

                    PassageQuestionResponse res = new PassageQuestionResponse(Long.valueOf(id), null);
                    res.submitter = User.byId(userID);
                    thisQ.responses.add(res);
                    thisQ.save();
                } else {
                    PassageQuestion current = null;
                    for (PassageQuestion q : passage.questions) {
                        if (q.position == Long.valueOf(i)) {
                            current = q;
                        }
                    }
                    Long instId = Long.valueOf((session("userId")));
                    User inst = User.byId(instId);

                    thisQ = new PassageQuestionRecord(inst, current, false);

                    int id = -1;
                    for (PassageQuestionChoice c : thisQ.question.choices) {
                        if (c.position == Integer.valueOf(data.answers.get(i))) {
                            id = c.id.intValue();
                        }
                    }


                    PassageQuestionResponse newPQRes = new PassageQuestionResponse(Long.valueOf(id), null);
                    newPQRes.submitter = User.byId(session("userId"));
                    thisQ.responses.add(newPQRes);
                    thisQ.save();
                }
            }
        }

        Long instId = Long.valueOf((session("userId")));
        User inst = User.byId(instId);


        return ok(viewPassageQuestionAnswers.render(passageId, inst));
    }

    /**
     * Instructor page where you can view the answers to your assigned questions
     * @param passageId Id of Passage we are fetching responses for
     * @return
     */
    public Result viewPassageQuestionAnswers(Long passageId) {
        if(session("userFirstName") == null) return ok(index.render(null));

        Long instId = Long.valueOf((session("userId")));
        User inst = User.byId(instId);

        return ok(viewPassageQuestionAnswers.render(passageId, inst));
    }

    /**
     * Renders a page where students can answer assigned questions
     * @param passageId Id of Passage user is responding to
     * @return Rendered page of questions or redirect
     */
    public Result answerPassageQuestions(Long passageId) {
        if(session("userFirstName") == null) return ok(index.render(null));
        return ok(answerPassageQuestions.render(form(PassageQuestionAnswerData.class), passageId));
    }

    /**
     * Instructors can view all questions created for a specific passage
     * @param passageId Id of Passage we are fetching questions for
     * @return Rendered table view of questions
     */
    public Result viewPassageQuestions(Long passageId) {
        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            if(session("userFirstName") == null) return ok(index.render(null));
            return ok(viewPassageQuestions.render(passageId));
        } else return badRequest();
    }

    //-- end questions


    /**
     * Renders the create passage window with the fields and text editor populated with saved data
     * @param passageId Id of Passage being edited
     * @param grade - grade level at which we are editing this passage
     * Editing an ORIGINAL passage at a level below its determined grade creates a new passage
     * @return Rendered passage form with values filled in
     */
    public Result editPassage(Long passageId, int grade) {
        if(session("userFirstName") == null) return ok(index.render(null));

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            SimplePassage passage = SimplePassage.byId(passageId);
            if (passage == null) {
                return redirect(routes.SimplePassageController.viewAllPassages());
            }

            SimplePassageData data = new SimplePassageData(passage.text, passage.title, 1, "category", passage.source);
            Form<SimplePassageData> form = Form.form(SimplePassageData.class);
            form = form.fill(data);

            if(!passage.analyzed && passage.original ){
                grade = 0;
            }
            else if(grade > passage.grade && !passage.original || grade < 0){
                flash("warning","The grade you requested was unavailable so you were re-routed");
                grade = passage.grade;
            } else if(PassageText.bySimplePassageAndGrade(passageId, grade) == null){
                flash("danger", "This Passage isn't finished analyzing yet!");
                return viewAllPassages();
            }



            return ok(editSimplePassageAtGrade.render(form, passageId, grade));
        } else {
            return ok(index.render(session("userFirstName")));
        }
    }


    /**
     * Renders a view where Passage can be seen along with suggestions for making it readable at the input grade
     * @param passageId Id of Passage being viewed
     * @param grade Grade at which you are viewing passage
     * @return Rendered page with passage info
     */
    public Result viewS(Long passageId, int grade) {
        if(session("userFirstName") == null) return ok(index.render(null));


        User user = User.byId(session("userId"));


        if(user.type == User.Role.STUDENT) return viewAsStudent(passageId, grade);
        if (user == null) {
            return ok(index.render(session("userFirstName")));
        }

        SimplePassage passage = SimplePassage.byId(passageId);

        if (grade >= 0) {
            passage.grade = grade;
        } else passage.grade = 0;

        return ok(viewPassageWithSuggestions.render(passage, User.byId(passage.instructorID), true));


    }

    /**
     * This method permits student users to view a passage without suggestions editing with their instructors changes
     * Grade level may not be changed by the student
     * @param passageId Id of Passage being displayed
     * @param grade Grade level of student viewing this page
     * @return Rendered view of Passage and Passage Tags
     */
    public Result viewAsStudent(Long passageId, int grade) {
        if(session("userFirstName") == null) return ok(index.render(null));
        User user = User.byId(session("userId"));
        if (user == null) {
            return ok(index.render(session("userFirstName")));
        }

        SimplePassage passage = SimplePassage.byId(passageId);

        if (grade >= 0) {
            passage.grade = grade;
        } else passage.grade = 0;

        return ok(viewPassageAsStudent.render(passage, user));
    }




// Core Processes

    /**
     * Helper method that allows us to begin Stanford CoreNLP Parsing and other
     * necessary tasks to get Suggestions
     * @param p Passage to update with Suggestions
     * @throws JWNLException -- WordNet Library is used and could thrown Exception
     */
    public void parseAndAddSuggestions(SimplePassage p) throws JWNLException{


        if(p.sentences.size() > 0){
            for(Sentence s : p.sentences){
                s.delete();
            }
            p.sentences = null;
            p.save();
        }

        String[] sentences = p.text.split(" ");

        String portion = "";

        int sCounter = 0;

        for (int i = 0; i < sentences.length; i++) {
            portion += sentences[i] + " ";
            if (sentences[i].indexOf(". ") >= 0) {
                sCounter++;

            }
            if (sCounter == 10 || (i + 1) == sentences.length) {
                if (parsingController == null) parsingController = new ParsingController();
                parsingController.parse(p, portion);
                portion = "";
                sCounter = 0;
            }
        }
    }


    // Helper method for telling whether or not analysis is in progress to assist with front end spinner display
    public static boolean isAnalyzing(){
        return inProgress;
    }

    /**
     * Parses, and gets difficulty for all Passages in the system
     * @return Success or Failure of Analysis
     * @throws JWNLException
     */
    public Result analyzePassages() throws JWNLException {
        if(session("userFirstName") == null) return ok(index.render(null));

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            this.inProgress = true;
            for (SimplePassage p : SimplePassage.all()) {


                parseAndAddSuggestions(p);

                if(p.htmlRepresentations != null){
                    for(PassageText pt : p.htmlRepresentations) pt.delete();
                }



                p.htmlRepresentations = new HashSet<PassageText>();

                this.difficultiesCache = getDifficulties(p);


                int stopPoint = 13;
                if(!p.original) stopPoint = p.grade;

                for (int i = 0; i <= stopPoint; i++) {
                    generatePassageTextAtGrade(p.id, i);
                    beginSentenceBreakdown(p.id, i);
                }



                p.analyzed = true;


                parsingController.reviseSuggestions();

                p.save();
            }


            this.inProgress = false;
            flash("success", "Passage Analysis Completed.");
            return ok("true");
        } else {
            return badRequest();
        }
    }


    /**
     * Preforms parsing, difficulty analysis, and generates HTML for the input Passage
     * @param passageId Id of Passage to analyze
     * @return Success or Failure of Analysis
     * @throws JWNLException
     */
    public Result analyzeSingularPassage(Long passageId) throws JWNLException {
        if(session("userFirstName") == null) return ok(index.render(null));

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            this.inProgress = true;

            SimplePassage p = SimplePassage.byId(passageId);

            if(p != null) {

                parseAndAddSuggestions(p);

                if(p.htmlRepresentations != null){
                    for(PassageText pt : p.htmlRepresentations) pt.delete();
                }



                p.htmlRepresentations = new HashSet<PassageText>();

                this.difficultiesCache = getDifficulties(p);

                int stopPoint = 13;
                if(p.analyzed) stopPoint = p.grade;

                for (int i = 0; i <= stopPoint; i++) {
                    generatePassageTextAtGrade(p.id, i);
                    beginSentenceBreakdown(p.id, i);
                }


                p.analyzed = true;


                parsingController.reviseSuggestions();

                p.save();


                this.inProgress = false;
                flash("success", "Passage Analysis Completed.");
                return ok("true");
            } else {
                this.inProgress = false;
                return badRequest();
            }
        } else {
            return badRequest();
        }
    }

    // Keep track of Suggestions that have already been set and no need to split the passage text over and over again
    private HashMap<String, String> sugMap = new HashMap<String, String>();
    private String[] split;

    /**
     * This method generates the HTML from the passage text inserting the proper ! and underlines for sentence
     * and word difficulty. Whenever this method is called, wholly new HTML is generated for the input grade level
     * @param passageId Passage to update HTML for
     * @param grade Grade Level to updated HTML for
     */
    public void generatePassageTextAtGrade(Long passageId, int grade) {
        SimplePassage p = SimplePassage.byId(passageId);

        boolean isOverwriting = false;

        PassageText current;

        if(PassageText.bySimplePassageAndGrade(p.id, grade) != null){
            current =  PassageText.bySimplePassageAndGrade(p.id, grade);
            isOverwriting = true;
        } else {
            current = new PassageText(grade, null);
        }


        List<Suggestion> s = Suggestion.bySimplePassage(p.id);

        if (split == null) split = p.text.split(" ");
        if (sugMap.size() == 0) {
            for (Suggestion sugg : s) {
                sugMap.put(sugg.word, sugg.suggestedWord);
            }
        }

        current.html =  p.text;


        HashSet<String> alreadySeen = new HashSet<String>();

        for (String w : split) {
            if (!alreadySeen.contains(w.toLowerCase())) {
                alreadySeen.add(w.toLowerCase());

                if(w.split(" ").length > 1){
                    w = w.split(" ")[0];
                }

                // The word is also too difficulted if it is an inflected form or a root word that is too difficult
                Word raw = Word.byRawString(w);
                Word inflected = InflectedWordForm.byInflectedForm(w);

                // maybe need to deal with compound words here

                if (raw != null && raw.ageOfAcquisition > (grade + 6) && !w.contains("<u>") && !Word.isStopWord(raw)) {
                    current.html = current.html.replace(" " + w + " ", " <u>" + w + "</u> ");
                    current.html = current.html.replace(" " + w.toLowerCase() + " ", " <u>" + w + "</u> ");
                } else if(inflected != null && inflected.ageOfAcquisition > (grade + 6) && !w.contains("<u>") && !Word.isStopWord(raw)){
                    current.html = current.html.replace(" " + w + " ", " <u>" + w + "</u> ");
                    current.html = current.html.replace(" " + w.toLowerCase() + " ", " <u>" + w + "</u> ");
                }
            }
        }

        current.html = current.html.replace("</u> <u>", "</u>&nbsp<u>");


        if(!isOverwriting){
            p.htmlRepresentations.add(current);
            p.save();
        } else {
            current.save();
        }
    }

    /**
     * This method is called when the "This is ok" button is pressed in the Suggestions Bar. Calling this will set
     * the difficulty of the word to be the input grade.
     * @param word Word to update
     * @param grade Grade to set as the corresponding age of acquisition for the word
     * @param passageId Passage this word is contained in
     * @return
     */
    public Result acceptWord(String word, int grade, Long passageId) {
        if(session("userFirstName") == null) return ok(index.render(null));

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            Word thisWord = Word.byRawString(word);
            if (thisWord != null) {
                thisWord.ageOfAcquisition = grade + 6;
                thisWord.save();
                generatePassageTextAtGrade(passageId, grade);
                flash("success", "We'll remember that word is okay!");
                return ok();
            }
        }

        return badRequest();
    }

    /**
     * This method is called when a Suggestion in the Suggestions bar is clicked by the user and replaces all instances
     * of the original word with the suggestion within this Passage
     * @param passageId Id of Passage to be altered
     * @param word Word being replaced
     * @param replacement Word to use for replacement
     * @return Renders updated passage or Error
     */
    public Result replaceWord(Long passageId, String word, String replacement) {
        if(session("userFirstName") == null) return ok(index.render(null));

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){

            SimplePassage p = SimplePassage.byId(passageId);

            try {
                p.text = p.text.replace(word, replacement);
                p.save();
                flash("success", "We replaced that word for you!");
                return ok();
            } catch (Exception e) {
                return badRequest();
            }
        } else {
            return ok(index.render(session("userFirstName")));
        }

    }


  // Holder set for Suggestions already found
    private HashSet<Suggestion> suggestionCache;


    /**
     * Helper method that generates lists of Suggestions and caches them so that we don't need to look them up more than once
     * @param p Simple Passage Object we are getting the Suggestions for
     * @return
     */
    public HashSet<Suggestion> getSuggestionsList(SimplePassage p) {
        String[] split = p.text.split(" ");

        HashSet<Suggestion> ret = new HashSet<Suggestion>();

        for (String s : split) {
            List<Suggestion> a = Suggestion.byWord(s);
            if (a != null) {
                ret.addAll(a);
            }
        }

        suggestionCache = ret;
        return ret;
    }

    /**
     * Helper Class to allow sorting/avoid duplication of Suggestions
     */
    private class SuggestionComp implements Comparator<Suggestion>{
        @Override
        public int compare(Suggestion o1, Suggestion o2) {
            if(o1.frequency > o2.frequency) return -1;
            else if (o1.frequency == o2.frequency) return 0;
            else return 1;
        }
    }

    /**
     * Returns a JSON array representing the Suggestions for a given Word represented by the input String
     * @param word - Word to check database for suggestions on.
     * @return JSON String of suggestions
     */
    public Result getSuggestions(String word) {
        if(session("userFirstName") == null) return ok(index.render(null));

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){

            word = word.replace("<span>", "");
            word = word.replace("</span>", "");

            System.out.println("--" + word);

            List<String> res = new ArrayList<String>();


            StringBuilder ret = new StringBuilder("[");


            List<Suggestion> sugg = Suggestion.byWord(word);

            sugg.sort(new SuggestionComp());

            for (int i = 0; i < sugg.size(); i++) {

                if(sugg.get(i).frequency != 0){
                    ret.append("\"" + sugg.get(i).suggestedWord + "\"");
                    if (i < sugg.size() - 1) ret.append(",");
                    res.add(sugg.get(i).suggestedWord);
                }
            }

            if(ret.charAt(ret.length() - 1) == ',') ret.deleteCharAt(ret.length() - 1);

            ret.append("]");

            return ok(ret.toString());
        } else {
            return ok(index.render(session("userFirstName")));
        }

    }

    /**
     * Renders table view of all passages in the system visible to the logged in User
     * @return Success or failure of rendering page
     */
    public Result viewAllPassages() {
        if(session("userFirstName") == null) return ok(index.render(null));
        Long instId = Long.valueOf((session("userId")));
        User inst = User.byId(instId);

        if(inst.type == User.Role.STUDENT){
            inst = User.byId(inst.creatorId);
        }
        List<SimplePassage> pList = SimplePassage.byInstructorId(inst.id);

        return ok(viewAllPassages.render(pList, inst));
    }

    /**
     * Method renders table view of all passages with a tag matching the input String
     * @param name
     * @return Success or Failure of Rendering view
     */
    public Result viewAllPassagesWithTag(String name) {
        if(session("userFirstName") == null) return ok(index.render(null));
        Long instId = Long.valueOf((session("userId")));
        User inst = User.byId(instId);
        List<SimplePassage> pList = SimplePassage.byPassageTagKeyword(name);

        return ok(viewAllPassagesWithTag.render(pList, inst, name));
    }

    /**
     * Completely Deletes a Passage and all corresponding table data from the database
     * called when delete button is pressed
     * @param passageId Id Of Passage to be deleted
     * @return Success or Failure of deleting
     */
    public Result deletePassage(Long passageId) {
        if(session("userFirstName") == null) return ok(index.render(null));

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            SimplePassage a = SimplePassage.byId(passageId);

            if (a == null) {
                flash("error", "Couldn't find a passage matching this id to delete");
                return ok("false");
            }

            a.delete();
            return ok("true");
        } else {
            return ok(index.render(session("userFirstName")));
        }
    }


    /**
     * Gets the Difficulties of all sentences in a Passage
     * @param p Passage of sentences to Analyze
     * @return List of all of the sentence difficulties
     */
    public ArrayList<Double> getDifficulties(SimplePassage p) {
        PassageAnalysisController analysisController = new PassageAnalysisController();
        ArrayList<Double> difficulties = new ArrayList<Double>();

        for (int i = 0; i < p.sentences.size(); i++) {
            Double diff = analysisController.determineGradeLevelForString(p.sentences.get(i).text);


            difficulties.add(diff);

        }

        return difficulties;
    }


    /**
     * Helper method to reconstruct HTML string broken down for difficulty Check
     * @param split - Array of Strings representing space separated values from original HTML
     * @return - Fully formed HTML String for use in Quill Text Editor
     */
    public String rebuildHTML(String[] split) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < split.length; i++) {
            String s = split[i];

            if (!s.contains("<i") || s.indexOf(".") != 0) {
                sb.append(s + " ");
            } else {
                sb.append(s);
            }


        }

        return sb.toString();
    }


    /**
     * Gets the difficutly of all the sentences in the input Passage and checks them against the input grade
     * @param passageId Id of Passage where these sentences come from
     * @param grade Grade to check the passages against
     * @return
     */
    public Result beginSentenceBreakdown(Long passageId, int grade) {

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            if(session("userFirstName") == null) return ok(index.render(null));
            System.out.println("Begin");
            SimplePassage p = SimplePassage.byId(passageId);


            PassageText current = PassageText.bySimplePassageAndGrade(passageId, grade);

            if (current.html != null && current.html.length() > 0) {
                System.out.println("in here");


                System.out.println("----" + current.html);

                String[] split = new String[p.sentences.size()];


                boolean placed = false;
                for (int i = 0; i < p.sentences.size(); i++) {
                    if (this.difficultiesCache.get(i) > grade && !placed) {
                        String curr = p.sentences.get(i).text;
                        int spaceIndex = curr.indexOf("&nbsp");

                        int whiteSpaceIndex = curr.indexOf(" ");

                        int padding = 5;

                        if(spaceIndex == -1){
                            spaceIndex = whiteSpaceIndex;
                            padding = 0;
                        }

                        System.out.println("sp:" + spaceIndex);


                        double diffValue = this.difficultiesCache.get(i);

                        String diffString = "" + (int) Math.round(diffValue);

                        if (spaceIndex != -1) {
                            curr = "&nbsp<i class='glyphicon glyphicon-exclamation-sign' onclick='showJustification(" + diffString + ");'>" + curr.substring(0, spaceIndex) + "</i>&nbsp" + curr.substring(spaceIndex + padding) + "&nbsp";

                        } else {
                            curr = "&nbsp<i class='glyphicon glyphicon-exclamation-sign' onclick='showJustification(" + diffString + ");'>" + curr + "</i> ";
                        }

                        while (curr.indexOf(" <u>") != -1) {
                            curr = curr.substring(0, curr.indexOf(" <u>")) + "&nbsp" + curr.substring(curr.indexOf(" <u>") + 1);
                        }


                        split[i] = curr;
                        System.out.println(split[i]);
                        placed = true;
                    }else {
                        split[i] = p.sentences.get(i).text;
                        placed = false;
                    }


                }


                current.html = rebuildHTML(split);

                for (int c = 0; c < current.html.length() - 2; c++) {
                    if (current.html.charAt(c) == ' ' && current.html.substring(c + 1, c + 3).equals("<u")) {
                        current.html = current.html.substring(0, c) + "&nbsp" + current.html.substring(c + 1);
                    }
                }


                // do i actually need to do this?
                for (PassageText pt : p.htmlRepresentations) {
                    if (pt.grade == current.grade) {
                        pt.html = current.html;
                        break;
                    }
                }

                System.out.println(current.html);
                p.save();
            } else System.out.println("null");


            return ok();
        } else return badRequest();
    }


    // Avoid fetching difficulties more than necessary
    private ArrayList<Double> diffCache;

    /**
     * Gets the difficulty of a singular sentence and returns whether or not it is more difficult than
     * the grade level passaged in.
     * @param passageId - Id of Passage this sentence is in
     * @param grade - Grade to check this sentence against
     * @param sentence - String with text of sentence
     * @return
     */
    public Result singularSentenceBreakdown(Long passageId, int grade, String sentence) {
        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            if(session("userFirstName") == null) return ok(index.render(null));
            SimplePassage p = SimplePassage.byId(passageId);
            if(diffCache == null) diffCache = getDifficulties(p);


            PassageText current = PassageText.bySimplePassageAndGrade(passageId, grade);

            String ogSentence = sentence;

            sentence = trimHTMLChars(ogSentence);
            if (current.html != null && current.html.length() > 0) {


                if(analysisController == null) analysisController = new PassageAnalysisController();
                Double diff = analysisController.determineGradeLevelForString(sentence);


                 if (diff > grade && !ogSentence.contains("exclamation") && !current.html.contains(sentence)) {
                    String curr = sentence;
                    int spaceIndex = curr.indexOf("&nbsp");

                    System.out.println("sp:" + spaceIndex);

                    double diffValue = diff;

                    String diffString = "" + (int) Math.round(diffValue);


                    if (spaceIndex != -1) {
                        curr = "&nbsp<i class='glyphicon glyphicon-exclamation-sign' onclick='showJustification(" + diffString + ");'>" + curr.substring(0, spaceIndex) + "</i>&nbsp" + curr.substring(spaceIndex + 5) + "&nbsp";
                    } else {
                        curr = "&nbsp<i class='glyphicon glyphicon-exclamation-sign' onclick='showJustification(" + diffString + ");'>" + curr + "</i> ";
                    }


                    while (curr.indexOf(" <u>") != -1) {
                        curr = curr.substring(0, curr.indexOf(" <u>")) + "&nbsp" + curr.substring(curr.indexOf(" <u>") + 1);
                    }

                    sentence = curr;
                    System.out.println(sentence);

                    current.html = current.html.replace(ogSentence, sentence);


                    for (int c = 0; c < current.html.length() - 2; c++) {
                        if (current.html.charAt(c) == ' ' && current.html.substring(c + 1, c + 3).equals("<u")) {
                            current.html = current.html.substring(0, c) + "&nbsp" + current.html.substring(c + 1);
                        }
                    }
                    if(ogSentence.indexOf("<i") != -1){
                        return ok();
                    } else {
                        return ok("!" + diffString);
                    }


                } else if(ogSentence.contains("exclamation") && !current.html.contains(sentence)){
                    return ok("REM " + sentence);
                }

                return ok();
            }


            // do i actually need to do this?
            for (PassageText pt : p.htmlRepresentations) {
                if (pt.grade == current.grade) {
                    pt.html = current.html;
                    break;
                }
            }

            p.save();

            // since this is saving, in theory this will reload and come up with the corrections
            return ok();
        } else return badRequest();
    }

    /**
     * Method to allow saving of updated passage plain text from Quill Text Editor in front end
     * @param passageId Id of Passage being saved
     * @param text - String representing plain text of passage
     * @return Success or failure of saving
     */
    public Result savePassagePlainText(Long passageId, String text) {
        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            try {
                SimplePassage p = SimplePassage.byId(passageId);
                p.text = text;
                p.save();
                return ok("true");
            } catch (Exception e) {
                return badRequest();
            }
        } else return badRequest();
    }


    /**
     * Helper method for Saving the Passage. If a passage is an original and you are editing it at a lower
     * level than the original passage then a new Passage is created and editable up to the newly saved grade.
     * Ex: edit a 9th grade passage down to 5th grade, new passage is created editable up to 5th grade level
     * @param grade - Grade at which you are editing
     * @param p - Passage being edited
     */
    public void copySimplePassage(int grade, SimplePassage p){

        SimplePassage atGrade = new SimplePassage(p.text, p.title + "-(" + PassageAnalysisController.displayGrade(grade) + " edit)", p.source);
        atGrade.instructorID = p.instructorID;
        atGrade.htmlRepresentations = new HashSet<PassageText>();
        atGrade.sentences = p.sentences;
        atGrade.tags = p.tags;

        atGrade.num_characters = p.num_characters;
        atGrade.numWords = p.numWords;
        atGrade.numSyllables = p.numSyllables;

        atGrade.analyzed = true;
        atGrade.grade = grade;




        String html = "";

        for (PassageText c : p.htmlRepresentations) {
            if (c.grade == grade) {
                PassageText copyText = new PassageText(c.grade, c.html);
                html = c.html;
                atGrade.htmlRepresentations.add(copyText);
                break;
            }
        }

        for(int i = 0; i < grade; i++){
            PassageText copyText = new PassageText(grade, html);
            atGrade.htmlRepresentations.add(copyText);
        }

        atGrade.save();


        atGrade.questions = new HashSet<PassageQuestion>();
        for(PassageQuestion q : p.questions){
            PassageQuestion copyQuestion = new PassageQuestion(q.basis_id, q.active);
            copyQuestion.correctAnswer = q.correctAnswer;
            copyQuestion.position = q.position;
            atGrade.questions.add(copyQuestion);
            atGrade.save();

            PassageQuestionPrompt copyPrompt = new PassageQuestionPrompt(copyQuestion, PassageQuestionPrompt.byPassageQuestion(q.id).get(0).text);
            copyQuestion.prompt = copyPrompt;
            copyPrompt.save();
            copyQuestion.save();

            for(PassageQuestionChoice c : q.choices){
                PassageQuestionChoice copyChoice = new PassageQuestionChoice(c.entity_id, c.correct, c.active);
                copyChoice.position = c.position;
                copyQuestion.choices.add(copyChoice);
                copyQuestion.save();

                PassageQuestionAnswer copyAnswer = new PassageQuestionAnswer(copyChoice, PassageQuestionAnswer.byPassageQuestionChoice(c.id).get(0).text);
                copyChoice.answer = copyAnswer;
                copyAnswer.save();
                copyChoice.save();
            }
            copyQuestion.save();
        }


        atGrade.save();
    }


    /**
     * Allows saving updated Passage HTML throw ajax call from the front end
     * @param passageId - Id of Passage to be saved
     * @param grade - Grade level of the passage currently being saved
     * @return Returns String message on success or failure of save
     */
    public Result savePassageHtml(Long passageId, int grade) {
        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            try {
                SimplePassage p = SimplePassage.byId(passageId);

                if (grade == -1) grade = p.grade;

                Map<String, String[]> parameters = request().body().asFormUrlEncoded();


                String html = parameters.get("html")[0];
                String plainText = parameters.get("plain")[0];

                //https://dzone.com/articles/jquery-ajax-play-2
                //http://stackoverflow.com/questions/16408867/send-post-json-with-ajax-and-play-framework-2
                for (PassageText c : p.htmlRepresentations) {
                    if (c.grade == grade) {
                        c.html = html;
                        if(grade == p.grade) {
                            c.save();
                        }
                        break;
                    }
                }

                p.text = plainText;

                if(grade != p.grade) copySimplePassage(grade,p);
                else {
                    p.save();

                }

                //p.save();
                return ok("true");
            } catch (Exception e) {
                return badRequest();
            }
        } else return badRequest();
    }

    /**
     * Allows Sentence difficulty to be checked through ajax call from Front End
     * @param sentence String representation of new sentence
     * @param grade - Grade level at which passage is currently being displayed in front end
     * @param passageId - Id of passage being shown in front end
     * @return String message that's either empty, has the difficulty if the sentence is too hard or REM to remove the !
     */
    public Result checkSentence(String sentence, int grade, Long passageId) {
        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            if(session("userFirstName") == null) return ok(index.render(null));
            return singularSentenceBreakdown(passageId, grade, sentence);
        } else return badRequest();
    }

    /**
     * Returns String stripped of any HTML tags
     * @param a String to be flattened
     * @return Original string without tags
     */
    public String trimHTMLChars(String a){
       StringBuilder sb = new StringBuilder();

        for(int i = 0; i < a.length(); i++){
            if(a.charAt(i) == '<'){
                while(a.charAt(i) != '>' && i < a.length()) i++;
            } else {
                sb.append(a.charAt(i));
            }
        }
        return sb.toString();
    }


    /**
     * Allows Word difficulty to be checked through Ajax call from Front End
     * @param word - String representation of Word Object to be checked
     * @param grade - Grade currently shown in the UI to check this word against
     * @param passageId - Id of the passage that this word appears in
     * @return Sends a String to front end on whether to underline a word or not
     */
    public Result checkWord(String word, int grade, Long passageId) {
        if(session("userFirstName") == null) return ok(index.render(null));

        if(User.byId(Long.valueOf(session("userId"))).creatorId == 0){
            word = trimHTMLChars(word);

            System.out.println(word);

            Word enteredWord = Word.byRawString(word);

            // (POS p, String word, String sentence, Long passageId, String ogText)
            if (enteredWord != null) {
                if (enteredWord.ageOfAcquisition - 6 > grade) {
                    if (Suggestion.byWord(enteredWord.lemma) == null) {
                        if (this.analysisController == null) analysisController = new PassageAnalysisController();
                        try {
                            analysisController.generateSuggestionsForWord(enteredWord.partsOfSpeech.get(0), enteredWord.lemma, word, passageId, enteredWord.lemma);

                        } catch (Exception e) {
                            return badRequest();
                        }
                    }
                    return ok("UNDERLINE");
                } else {
                    return ok("REM");
                }
            }
            // instead of returning a bad request...shouldn't this word be underlined and it's suggestions added
            return ok("REM");
        } else return badRequest();
    }

}