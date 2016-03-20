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

import javax.json.JsonArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static play.data.Form.form;

public class SimplePassageController extends Controller {

    private ParsingController parsingController;
    private PassageAnalysisController analysisController;
    private ArrayList<Double> difficultiesCache;

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

        PassageText current = PassageText.bySimplePassageAndGrade(passageId, data.grade);
        current.html = data.passageText;


        passage.text = data.passageText;
        passage.grade = data.grade;
        passage.source = data.source;

        passage.title = data.passageTitle;


        passage.sentences = null;


        passage.save();

        flash("success", "Modified Passage details have been saved.");
        return redirect(routes.SimplePassageController.view(passageId, passage.grade));
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

            p.htmlRepresentations = new HashSet<PassageText>();

            this.difficultiesCache = getDifficulties(p);

            for(int i = 0 ; i < 14; i++){
                generatePassageTextAtGrade(p.id, i);
                beginSentenceBreakdown(p.id, i);
            }

            p.save();
        }

        flash("success", "Passage Analysis Completed.");
        return ok("true");
    }


    HashMap<String, String> sugMap = new HashMap<String, String>();
    String[] split;

    public void generatePassageTextAtGrade(Long passageId, int grade){
        System.out.println("gen text");
        SimplePassage p = SimplePassage.byId(passageId);
        PassageText current = new PassageText(grade, null);
        List<Suggestion> s = Suggestion.bySimplePassage(p.id);

        if(split == null) split = p.text.split(" ");
        if(sugMap.size() == 0){
            for(Suggestion sugg : s){
                sugMap.put(sugg.word, sugg.suggestedWord);
            }
        }

        current.html = "<div>" + p.text + "</div>";

        HashSet<String> alreadySeen = new HashSet<String>();

        for(String w : split){
            if(!alreadySeen.contains(w.toLowerCase())){
                alreadySeen.add(w.toLowerCase());

                Word raw = Word.byRawString(w);

                if(raw != null && raw.ageOfAcquisition > (grade + 6) && !w.contains("<u>")){
                    current.html = current.html.replace(" " + w + " ", " <u>" + w + "</u> ");
                    current.html = current.html.replace(" " + w.toLowerCase() + " ", " <u>" + w + "</u> ");
                }
            }
        }

        for(PassageText t : p.htmlRepresentations){
            t.delete(); 
        }

        current.html = current.html.replace("</u> <u>", "</u>&nbsp<u>");
        p.htmlRepresentations.add(current);
        p.save();
        System.out.println("GenText END");
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

    public HashSet<Suggestion> getSuggestionsList(SimplePassage p) {
        String[] split = p.text.split(" ");

        HashSet<Suggestion> ret = new HashSet<Suggestion>();

        for (String s : split) {
            List<Suggestion> a = Suggestion.byWord(s);
            if (a != null) {
                ret.addAll(a);
            }
        }
        return ret;
    }

    public Result getSuggestions(String word){

        word = word.replace("<span>","");
        word = word.replace("</span>","");

        System.out.println("--" + word );

        List<String> res = new ArrayList<String>();


        StringBuilder ret = new StringBuilder("[");


        List<Suggestion> sugg = Suggestion.byWord(word);

        for(int i = 0; i < sugg.size(); i++){
            ret.append("\"" + sugg.get(i).suggestedWord + "\"");
            if(i < sugg.size() - 1) ret.append(",");
            res.add(sugg.get(i).suggestedWord);
        }
        ret.append("]");

        return ok(ret.toString());
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

    public ArrayList<Double> getDifficulties(SimplePassage p){
        PassageAnalysisController analysisController = new PassageAnalysisController();
        ArrayList<Double> difficulties = new ArrayList<Double>();

        for (int i = 0; i < p.sentences.size(); i++) { 
            Double diff = analysisController.determineGradeLevelForString(p.sentences.get(i).text);
    

                difficulties.add(diff);

        }
        
        return difficulties;
    }


    public String rebuildHTML(String[] split){
        StringBuilder sb = new StringBuilder(); 

        for(int i = 0; i < split.length; i++){
            String s = split[i]; 

            if(!s.contains("<i")){
                 sb.append(s + " ");
             } else {
                sb.append(s); 
             }

           
        }

        return sb.toString(); 
    }






    public Result beginSentenceBreakdown(Long passageId, int grade){
        System.out.println("Begin");
        SimplePassage p = SimplePassage.byId(passageId);


      
        PassageText current = PassageText.bySimplePassageAndGrade(passageId, grade);

        if(current.html != null && current.html.length() > 0){
            System.out.println("in here");


            System.out.println("----" + current.html);

            String[] split = current.html.split(" ");

            int sentNumber = 0; 
            boolean placed = false; 
            for(int i = 0; i < split.length; i++){

                if(sentNumber < p.sentences.size() && this.difficultiesCache.get(sentNumber) > grade && !placed){
                    String curr = split[i];
                    int spaceIndex = curr.indexOf("&nbsp"); 

                    System.out.println("sp:" + spaceIndex);

                    if(spaceIndex != -1){
                        curr = "&nbsp<i class='glyphicon glyphicon-exclamation-sign'>" + curr.substring(0, spaceIndex) + "</i>&nbsp" + curr.substring(spaceIndex + 5) + "&nbsp";
                    } else {
                         curr = "&nbsp<i class='glyphicon glyphicon-exclamation-sign'>" + curr + "</i> ";
                    }

                    while(curr.indexOf(" <u>") != -1){
                        curr = curr.substring(0, curr.indexOf(" <u>")) + "&nbsp" + curr.substring(curr.indexOf(" <u>") + 1);
                    }

                  
                    split[i] = curr; 
                    System.out.println(split[i]); 
                    placed = true;
                }

                if(split[i].indexOf(".") != -1){
                    sentNumber++;
                    placed = false; 
                }
             
            
            }



            current.html = rebuildHTML(split);

            for(int c = 0; c < current.html.length() - 2; c++){
                if(current.html.charAt(c) == ' ' && current.html.substring(c + 1, c + 3).equals("<u")){
                     current.html = current.html.substring(0, c) + "&nbsp" + current.html.substring(c + 1);
                }
            }


            // do i actually need to do this?
            for(PassageText pt : p.htmlRepresentations){
                if(pt.grade == current.grade){
                    pt.html = current.html;
                    break;
                }
            }

            System.out.println(current.html);
            p.save();
        } else System.out.println("null"); 


        return ok();
    }



    public Result singularSentenceBreakdown(Long passageId, int grade, String sentence){
        System.out.println("Begin");
        SimplePassage p = SimplePassage.byId(passageId);



        PassageText current = PassageText.bySimplePassageAndGrade(passageId, grade);

        String ogSentence = sentence;

        if(current.html != null && current.html.length() > 0){
            System.out.println("in here");



            Double diff = analysisController.determineGradeLevelForString(sentence);



                if(diff > grade){
                    String curr = sentence;
                    int spaceIndex = curr.indexOf("&nbsp");

                    System.out.println("sp:" + spaceIndex);

                    if(spaceIndex != -1){
                        curr = "&nbsp<i class='glyphicon glyphicon-exclamation-sign'>" + curr.substring(0, spaceIndex) + "</i>&nbsp" + curr.substring(spaceIndex + 5) + "&nbsp";
                    } else {
                        curr = "&nbsp<i class='glyphicon glyphicon-exclamation-sign'>" + curr + "</i> ";
                    }

                    while(curr.indexOf(" <u>") != -1){
                        curr = curr.substring(0, curr.indexOf(" <u>")) + "&nbsp" + curr.substring(curr.indexOf(" <u>") + 1);
                    }


                    sentence = curr;
                    System.out.println(sentence);

                }
            }

            current.html = current.html.replace(ogSentence, sentence);



            for(int c = 0; c < current.html.length() - 2; c++){
                if(current.html.charAt(c) == ' ' && current.html.substring(c + 1, c + 3).equals("<u")){
                    current.html = current.html.substring(0, c) + "&nbsp" + current.html.substring(c + 1);
                }
            }


            // do i actually need to do this?
            for(PassageText pt : p.htmlRepresentations){
                if(pt.grade == current.grade){
                    pt.html = current.html;
                    break;
                }
            }

            System.out.println(current.html);
            p.save();

        return ok();
    }

    public Result savePassagePlainText(Long passageId, String text){
        try{
            SimplePassage p = SimplePassage.byId(passageId);
            p.text = text;
            p.save();
            return ok("true");
        } catch(Exception e){
            return badRequest();
        }
    }

    public Result savePassageHtml(Long passageId, String html, int grade){
        try{
            SimplePassage p = SimplePassage.byId(passageId);

            if(grade == -1) grade = p.grade;


            for(PassageText c : p.htmlRepresentations){
                if(c.grade == grade){
                    c.html = html;
                    break;
                }
            }

            p.save();
            return ok("true");
        } catch(Exception e){
            return badRequest();
        }
    }


    public Result checkSentence(String sentence, int grade, Long passageId){
        return singularSentenceBreakdown(passageId, grade, sentence);
    }

    public Result checkWord(String word, int grade, Long passageId){
        Word enteredWord = Word.byRawString(word);

       // (POS p, String word, String sentence, Long passageId, String ogText)
        if(enteredWord != null){
            if(enteredWord.ageOfAcquisition > grade){
                if(Suggestion.byWord(enteredWord.lemma) == null){
                    if(this.analysisController == null) analysisController = new PassageAnalysisController();
                    try{
                        analysisController.generateSuggestionsForWord(enteredWord.partsOfSpeech.get(0), enteredWord.lemma, word, passageId, enteredWord.lemma);
                        return(getSuggestions(enteredWord.lemma));
                    } catch(Exception e){
                        return badRequest();
                    }
                }
            }
        }
        return badRequest();
    }

}