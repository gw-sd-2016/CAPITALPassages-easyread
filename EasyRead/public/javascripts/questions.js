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