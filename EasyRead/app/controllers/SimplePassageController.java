package controllers;


import com.avaje.ebeaninternal.server.lib.util.Str;
import com.fasterxml.jackson.databind.JsonNode;
import formdata.PassageQuestionAnswerData;
import formdata.SimplePassageData;
import formdata.SimplePassageNumQuestionsData;
import models.*;
import net.sf.extjwnl.JWNLException;
import play.api.libs.json.JsArray;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static play.data.Form.form;

public class SimplePassageController extends Controller {

    private ParsingController parsingController;
    private PassageAnalysisController analysisController;

    // New Passage Methods
    public Result createSimplePassage() throws JWNLException {
        return ok(createSimplePassage.render(form(SimplePassageData.class)));
    }

    // QUESTION CREATION
    public Result setNumQuestions(Long passageId) {
        return ok(setNumberOfQuestionsForPassage.render(form(SimplePassageNumQuestionsData.class), passageId));
    }

    public Result setNumQuestions_submit() {

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

            q.choices = new HashSet<PassageQuestionChoice>();

            for (int c = 0; c < createSPForm.get().choices.get(i).size(); c++) {
                PassageQuestionChoice w = new PassageQuestionChoice();


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

                q.choices.add(w);

            }


            passage.questions.add(q);


        }


        System.out.println("Size : " + passage.questions.size());


        passage.save();


        // resolve question ids
        // if there were 3 questions created then get the last 3 prompts



		/*PassageQuestion[] arr = new PassageQuestion[passage.questions.size()];

		int w = 0; 
		for(PassageQuestion q : passage.questions) {
			arr[w] = q;
			w++; 
		}


		for(int i = 0; i < arr.length; i++){
			int maxPromptId = 0; 
			for(PassageQuestionPrompt p : PassageQuestionPrompt.all()) maxPromptId = Math.max(maxPromptId, p.id.intValue());

			System.out.println("prompt " + maxPromptId);


			int thisId = maxPromptId - (passage.questions.size()) + arr[i].position;

			System.out.println("thisId " + thisId);

			PassageQuestionPrompt thisOne = PassageQuestionPrompt.byId(Long.valueOf(thisId));

			thisOne.questionId = arr[i].id; 

			thisOne.save();

			PassageQuestionChoice[] choices = new PassageQuestionChoice[arr[i].choices.size()]; 
			int e = 0; 
			for(PassageQuestionChoice c : arr[i].choices) {
				choices[e] = c;
				e++; 
			}




			for(int c = 0; c < choices.length; c++){
				int maxAnswerId = 0; 
				for(PassageQuestionAnswer a : PassageQuestionAnswer.all()) maxAnswerId = Math.max(maxAnswerId, a.id.intValue());

				int thisChoiceId = maxAnswerId - (choices.length - 1) + choices[i].position - 1;

				PassageQuestionAnswer thisAnswer = PassageQuestionAnswer.byId(Long.valueOf(thisChoiceId));

				thisAnswer.choiceId = choices[i].id; 

				thisAnswer.save();


			}



		}
		 */


        return ok(viewPassageQuestions.render(passage.id));
    }


    public Result edit(Long passageId) {
        SimplePassage passage = SimplePassage.byId(passageId);
        if (passage == null) {
            return redirect(routes.SimplePassageController.viewAllPassages());
        }

        SimplePassageData data = new SimplePassageData(passage.text, passage.title, 1, "category", passage.source);
        Form<SimplePassageData> form = Form.form(SimplePassageData.class);
        form = form.fill(data);
        return ok(editSimplePassage.render(form, passageId));
    }

    public Result editQuestions(Long passageId) {
        SimplePassage passage = SimplePassage.byId(passageId);
        if (passage == null) {
            return redirect(routes.SimplePassageController.viewAllPassages());
        }

        //questions, choices, corect answers
        ArrayList<String> q = new ArrayList<String>();
        ArrayList<ArrayList<String>> c = new ArrayList<ArrayList<String>>();
        ArrayList<String> ca = new ArrayList<String>();


        for (PassageQuestion pq : passage.questions) {
            q.add(PassageQuestionPrompt.byPassageQuestion(pq.id).get(0).text);
            ArrayList<String> thisQList = new ArrayList<String>();
            for (PassageQuestionChoice pqc : pq.choices) {

                thisQList.add(PassageQuestionAnswer.byPassageQuestionChoice(pqc.id).get(0).text);
                if (pqc.correct) ca.add(String.valueOf(pqc.position));
            }
        }

        SimplePassageNumQuestionsData data = new SimplePassageNumQuestionsData(passageId, q, c, ca);
        Form<SimplePassageNumQuestionsData> form = Form.form(SimplePassageNumQuestionsData.class);
        form = form.fill(data);

        return ok(editPassageQuestions.render(form, passageId));

    }

    public Result editQuestions_submit(Long passageId) {

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
    }


    public Result edit_submit(Long passageId) {
        SimplePassage passage = SimplePassage.byId(passageId);
        if (passage == null) {
            return redirect(routes.SimplePassageController.viewAllPassages());
        }

        Form<SimplePassageData> form = Form.form(SimplePassageData.class).bindFromRequest();

        if (form.hasErrors()) {
            return badRequest(editSimplePassage.render(form, passageId));
        }


        SimplePassageData data = form.get();
        passage.text = data.passageText;
        passage.grade = data.grade;
        passage.source = data.source;

        passage.title = data.passageTitle;


        passage.sentences = null;


        passage.save();

        flash("success", "Modified Passage details have been saved.");
        return redirect(routes.SimplePassageController.view(passageId, passage.grade));
    }


    public String getFolderPath() {
        return "public/passages";
    }


    public Result editPassage(Long passageId, int grade) {
        SimplePassage passage = SimplePassage.byId(passageId);
        if (passage == null) {
            return redirect(routes.SimplePassageController.viewAllPassages());
        }

        SimplePassageData data = new SimplePassageData(passage.text, passage.title, 1, "category", passage.source);
        Form<SimplePassageData> form = Form.form(SimplePassageData.class);
        form = form.fill(data);
        return ok(editSimplePassageAtGrade.render(form, passageId, grade));
    }


    public Result view(Long passageId, int grade) {
        User user = User.byId(session("userId"));
        if (user == null) {
            return ok(index.render(session("userFirstName")));
        }

        SimplePassage passage = SimplePassage.byId(passageId);

        if (grade >= 0) {
            passage.grade = grade;
        } else passage.grade = 0;

        return ok(viewPassage.render(passage, User.byId(passage.instructorID), false));
    }

    public Result viewS(Long passageId, int grade) {
        User user = User.byId(session("userId"));
        if (user == null) {
            return ok(index.render(session("userFirstName")));
        }

        SimplePassage passage = SimplePassage.byId(passageId);

        if (grade >= 0) {
            passage.grade = grade;
        } else passage.grade = 0;

        return ok(viewPassageWithSuggestions.render(passage, User.byId(passage.instructorID), true));
    }


/*
    public Result viewPassageQuestions(Long passageId) {
		User user = User.byId(session("userId"));
		if (user == null) {
			return redirect(routes.UserController.index(request().uri()));
		}



		return ok(viewPassageQuestions.render(passageId));
	}
*/

    public Result viewAllPassageTags(List<PassageTag> tags) {
        return ok(viewAllPassageTags.render(PassageTag.all()));
    }


    public Result createSimplePassage_submit() {
        Form<SimplePassageData> createSPForm = form(SimplePassageData.class).bindFromRequest();

        if (createSPForm.hasErrors()) {
            return badRequest(createSimplePassage.render(createSPForm));
        }

        SimplePassage newPassage = new SimplePassage(createSPForm.get());
        newPassage.instructorID = Integer.valueOf(session("userId"));

        if (createSPForm.get().descriptions.size() > 0 && createSPForm.get().types.size() > 0 && createSPForm.get().names.size() > 0) {

            PassageTag t = new PassageTag(createSPForm.get().names.get(0), createSPForm.get().descriptions.get(0), createSPForm.get().types.get(0));

            if (PassageTag.byName(createSPForm.get().names.get(0)) == null) {
                t.save();
                newPassage.tagId = t.id;


            } else {
                newPassage.tagId = PassageTag.byName(createSPForm.get().names.get(0)).id;
                t = PassageTag.byName(createSPForm.get().names.get(0));

            }

            newPassage.tags.add(t);

        }

        newPassage.save();
        return redirect(routes.SimplePassageController.viewAllPassages());
    }


    public Result analyzePassages() throws JWNLException {
        for (SimplePassage p : SimplePassage.all()) {
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

            p.save();
        }

        flash("success", "Passage Analysis Completed.");
        return ok("true");
    }


    public Result acceptWord(String word, int grade) {
        Word thisWord = Word.byLemma(word);
        if (thisWord != null) {
            thisWord.ageOfAcquisition = grade + 6;
            thisWord.save();
            flash("success", "We'll remember that word is okay!");
            return ok();
        }
        return badRequest();
    }

    public Result replaceWord(Long passageId, String word, String replacement) {
        SimplePassage p = SimplePassage.byId(passageId);

        try {
            p.text = p.text.replace(word, replacement);
            p.save();
            flash("success", "We replaced that word for you!");
            return ok();
        } catch (Exception e) {
            return badRequest();
        }
    }

    public Result getSuggestions(String word){

        List<String> res = new ArrayList<String>();

        for(Suggestion s : Suggestion.byWord(word)) res.add(s.suggestedWord);

        return ok(Json.toJson(res));
    }

    public Result viewAllPassages() {

        Long instId = Long.valueOf((session("userId")));
        User inst = User.byId(instId);
        List<SimplePassage> pList = SimplePassage.byInstructorId(instId);

        return ok(viewAllPassages.render(pList, inst));
    }

    public Result viewAllPassagesWithTag(String name) {
        Long instId = Long.valueOf((session("userId")));
        User inst = User.byId(instId);
        List<SimplePassage> pList = SimplePassage.byInstructorId(instId);

        return ok(viewAllPassagesWithTag.render(pList, inst, name));
    }

    public Result deletePassage(Long passageId) {
        SimplePassage a = SimplePassage.byId(passageId);

        System.out.println("PASSAGEID: " + passageId);

        if (a == null) {
            flash("error", "Couldn't find a passage matching this id to delete");
            return ok("false");
        }

		/*

		for(PassageQuestion p : a.questions){
			for(PassageQuestionPrompt prompt : PassageQuestionPrompt.byPassageQuestion(p.question.id)){
        		prompt.delete("");
        	}

        	for(PassageQuestionChoice c : p.choices){
        		 PassageQuestionAnswer.byId(c.id)).dele
        	}

		}

        for(PassageQuestionRecord p : PassageQuestionRecord.all()){
            if(a.questions.contains(p.question)){  

            	p.delete("");
            }
        }


    	System.out.println("Records should be deleted");
		 */
        a.delete("");


        return ok("true");
    }

    // do we possible need to clean up response here?? are they unreachable
    public Result deletePassageQuestion(Long questionId) {
        PassageQuestion a = PassageQuestion.byId(questionId);

        if (a == null) {
            flash("error", "Couldn't find a question matching this id to delete");
            return ok("false");
        }

        a.delete("");

        System.out.println("Should be deleted");


        return ok("true");
    }

    public Result deletePassageQuestionChoice(Long choiceId) {
        PassageQuestionChoice a = PassageQuestionChoice.byId(choiceId);

        if (a == null) {
            flash("error", "Couldn't find a choice matching this id to delete");
            return ok("false");
        }

        a.delete("");

        System.out.println("Should be deleted");


        return ok("true");
    }

    public Result answerPassageQuestions(Long passageId) {
        return ok(answerPassageQuestions.render(form(PassageQuestionAnswerData.class), passageId));
    }


    public Result answerPassageQuestions_submit(Long passageId) {
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

                    thisQ.responses.add(new PassageQuestionResponse(Long.valueOf(id), null));
                    thisQ.save();
                }
            }
        }

        Long instId = Long.valueOf((session("userId")));
        User inst = User.byId(instId);


        return ok(viewPassageQuestionAnswers.render(passageId, inst));
    }

    public Result viewPassageQuestionAnswers(Long passageId) {
        Long instId = Long.valueOf((session("userId")));
        User inst = User.byId(instId);

        return ok(viewPassageQuestionAnswers.render(passageId, inst));

    }

    public Result viewPassageDeepAnalysis(Long passageId) {
        SimplePassage p = SimplePassage.byId(passageId);

        if (p != null) {
            return ok(viewPassageDeepAnalysis.render(p));
        }

        return badRequest();
    }

    public Result viewPassageSentenceDrilldown(Long passageId, int startingSentence) {
        SimplePassage p = SimplePassage.byId(passageId);

        if (p != null) {

            StringBuilder b = new StringBuilder();

            if(startingSentence < p.sentences.size()) b.append(p.sentences.get(startingSentence).text);
            if(startingSentence + 1 < p.sentences.size()) b.append(p.sentences.get(startingSentence + 1).text);

            return ok(viewPassageSentenceDrillDown.render(p.id, p.grade, b.toString()));
        }

        return badRequest();
    }


}