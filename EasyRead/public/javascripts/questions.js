// This method calls make an ajax call to the SimplePassageController that changes the position field of the PassageQuestionChoice object in the database
function moveChoiceUp(choiceId, questionId){

	    jsRoutes.controllers.SimplePassageController.moveChoice(choiceId, questionId, true).ajax({
                    success : function(data) {
                        //window.location.reload();
                    },
                    error : function(err) {
                        //window.location = "";
                    },
                    complete : function() {
                        window.location.reload();
                    }
                });
}

// This method calls make an ajax call to the SimplePassageController that changes the position field of the PassageQuestionChoice object in the database
function moveChoiceDown(choiceId, questionId){

	    jsRoutes.controllers.SimplePassageController.moveChoice(choiceId, questionId, false).ajax({
                    success : function(data) {
                        //window.location.reload();
                    },
                    error : function(err) {
                        //window.location = "";
                    },
                    complete : function() {
                        window.location.reload();
                    }
                });
}

// This method calls make an ajax call to the SimplePassageController that changes the position field of the PassageQuestion object in the database
function moveQuestion(questionId, up){

	    jsRoutes.controllers.SimplePassageController.moveQuestion(questionId, up).ajax({
                    success : function(data) {
                        //window.location.reload();
                    },
                    error : function(err) {
                        //window.location = "";
                    },
                    complete : function() {
                        window.location.reload();
                    }
                });
}


// This method calls make an ajax call to the SimplePassageController that deletes the PassageQuestionChoice from the database
function deleteChoice(questionId, passageId, choiceId){
	 jsRoutes.controllers.SimplePassageController.deleteChoiceForQuestion(questionId, passageId, choiceId).ajax({
                    success : function(data) {
                        //window.location.reload();
                    },
                    error : function(err) {
                        //window.location = "";
                    },
                    complete : function() {
                        window.location.reload();
                    }
                });
}

// This method calls make an ajax call to the SimplePassageController that deletes the PassageQuestion from the database
function deleteQuestion(questionId, passageId){
	 jsRoutes.controllers.SimplePassageController.deleteQuestion(questionId, passageId).ajax({
                    success : function(data) {
                        //window.location.reload();
                    },
                    error : function(err) {
                        //window.location = "";
                    },
                    complete : function() {
                        window.location.reload();
                    }
                });
}



// This method calls make an ajax call to the SimplePassageController that alters the PassageQuestionChoice and PassageQuestion objects in the database to reflect the new correct answer
function setAsCorrectAnswer(choiceId, questionId){
	 jsRoutes.controllers.SimplePassageController.setAsCorrectAnswer(choiceId, questionId).ajax({
                    success : function(data) {
                        //window.location.reload();
                    },
                    error : function(err) {
                        //window.location = "";
                    },
                    complete : function() {
                        window.location.reload();
                    }
                });
}


// This method calls make an ajax call to the SimplePassageController that alters the PassageQuestionAnswer Object in the database
function editAnswer(choiceId, questionId){
	//http://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_prompt
	var newAnswer = prompt("What should this answer be?", "Siri is the best");
    
     jsRoutes.controllers.SimplePassageController.editChoiceAnswer(choiceId, questionId, newAnswer).ajax({
                    success : function(data) {
                        //window.location.reload();
                    },
                    error : function(err) {
                        //window.location = "";
                    },
                    complete : function() {
                        window.location.reload();
                    }
                });

}

// This method calls make an ajax call to the SimplePassageController that changes the position field of the PassageQuestionPrompt object in the database
function editPrompt(questionId){
	//http://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_prompt
	var newAnswer = prompt("What's the question?", "Why do people use Android?");
    
     jsRoutes.controllers.SimplePassageController.editPromptForQuestion(questionId, newAnswer).ajax({
                    success : function(data) {
                        //window.location.reload();
                    },
                    error : function(err) {
                        //window.location = "";
                    },
                    complete : function() {
                        window.location.reload();
                    }
                });

}


// This method calls make an ajax call to the SimplePassageController that adds as PassageQuestionChoice object to the database & PassageQuestion
function addChoice(questionId){
	//http://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_prompt
	var newAnswer = prompt("What's the answer choice?", "OSX is the best");
    
     jsRoutes.controllers.SimplePassageController.addChoiceToQuestion(questionId, newAnswer, false).ajax({
                    success : function(data) {
                        //window.location.reload();
                    },
                    error : function(err) {
                        //window.location = "";
                    },
                    complete : function() {
                        window.location.reload();
                    }
                });

}