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