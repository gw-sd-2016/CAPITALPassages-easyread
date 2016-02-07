//PassageQuestions
function setAnswer(e){
    //http://stackoverflow.com/questions/3001474/how-to-get-caller-element-using-javascript
    var sender = (e && e.target) || (window.event && window.event.srcElement);
    var name = sender.name;



    var questionNumber = name.substr(0,name.indexOf(","));



    var choiceNumber = name.substr(name.indexOf(",") + 1, name.length);




    var previousAnswers = document.getElementsByName("answers[" + questionNumber + "]");

    console.log(" correctAnswers[" + questionNumber + "] " + previousAnswers.length);


    var checkboxes = document.getElementsByTagName("input");
    if(previousAnswers != null && previousAnswers.length > 0){
        previousAnswers[0].value = choiceNumber;
    } else {
        var hiddenChoiceDiv = '<div style="display:none"><input type="text" name="answers[' + questionNumber + ']" value="' + choiceNumber + '" /> </div>';
        $(hiddenChoiceDiv).insertAfter(sender);
    }

    for(var i = 0; i < checkboxes.length; i++){
        if(checkboxes[i].type == "radio" && checkboxes[i].name != sender.name){
            if(checkboxes[i].name.indexOf(questionNumber) == 0) checkboxes[i].disabled = true;
        }
    }
}

function addChoices (e) {
//problem here
    //http://stackoverflow.com/questions/3001474/how-to-get-caller-element-using-javascript
    var sender = (e && e.target) || (window.event && window.event.srcElement);
    var name = sender.name;

    var numQuestions = name.substr(name.indexOf("b") + 1);

    var numChoices = document.getElementsByClassName('choice' + numQuestions).length;

    console.log('choice' + numQuestions);

    var newChoice = '<div class="choice' + numQuestions + '"><input name="choices[' + numQuestions + '][' + numChoices + ']" type="text" id="choice" value=choices[' + numQuestions + '][' + numChoices + ']" style="margin-left:25px;"/> <input type="checkbox" name="correctBox[' + numQuestions + '][' + numChoices + ']" onclick="setCorrectAnswer();" style="padding-left:5px;" /> </div>';

    var lastChoice = document.getElementsByName('correctBox[' + numQuestions + '][' + (numChoices - 1) + ']');

    console.log(lastChoice.length);

    var $this = $(lastChoice);
    $(newChoice).insertAfter(lastChoice[lastChoice.length - 1]);
}

function addQuestions () {
    var numQuestions = $('.question').length;


    var lastQuestion = document.getElementsByName('question[' + (numQuestions - 1) + ']');


    var newQ = '<br><h3>' + (numQuestions + 1) + '.</h3> <div class="question" name="question[' + numQuestions +']"><label>Question Prompt: <input type="text" name="questions[' + numQuestions +']" placeholder="Description" value="questions[' + numQuestions +']"> <button type="button" class="btn btn-primary" name="b' + numQuestions +'" onclick="addChoices();">Add Choice</button></label><strong>Check the box next to the correct answer</strong><div class="choice' + numQuestions + '"><input type="text" name="choices[' + numQuestions +'][0]" placeholder="Description" value="choices[' + numQuestions +'][0]" style="margin-left:25px;"> <input type="checkbox" name="correctBox[' + numQuestions +'][0]" onclick="setCorrectAnswer();" style="padding-left:5px;" checked="true"></div></div><br>';

    var $this = $(lastQuestion);
    $(newQ).insertAfter(lastQuestion[lastQuestion.length - 1]);
}

function setCorrectAnswer(e){
    //http://stackoverflow.com/questions/3001474/how-to-get-caller-element-using-javascript
    var sender = (e && e.target) || (window.event && window.event.srcElement);
    var name = sender.name;

    name = name.substr(name.indexOf("x") + 2);

    var questionNumber = name.substr(0,name.indexOf("]"));

    name = name.substr(name.indexOf("[") + 1);

    var choiceNumber = name.substr(0,name.indexOf("]"));


    var previousAnswers = document.getElementsByName("correctAnswers[" + questionNumber + "]");

    console.log(" correctAnswers[" + questionNumber + "] " + previousAnswers.length);


    var checkboxes = document.getElementsByTagName("input");
    if(previousAnswers != null && previousAnswers.length > 0){
        previousAnswers[0].value = choiceNumber
    } else {
        var hiddenChoiceDiv = '<div style="display:none"><input type="text" name="correctAnswers[' + questionNumber + ']" value="' + choiceNumber + '" /> </div>';
        $(hiddenChoiceDiv).insertAfter(sender);
    }

    for(var i = 0; i < checkboxes.length; i++){
        if(checkboxes[i].type == "checkbox" && checkboxes[i].name != sender.name && checkboxes[i].name.indexOf("correctBox[" + questionNumber) >= 0){
            checkboxes[i].checked = false;
        }
    }
}

//PassageCreation
function addTag() {
//problem here
//http://stackoverflow.com/questions/3001474/how-to-get-caller-element-using-javascript
var numTags = document.getElementsByClassName('tag').length;

console.log('tag' + numTags);


var newTag ='<div class="tag" name="tag' + numTags + '"> Tag' +
numTags + '<input type="text" name="names[' + numTags + ']" placeholder="Tag Name" value="names[' + numTags + ']"><label>Anything you want</label><input type="text" name="descriptions[' + numTags + ']" placeholder="Tag Description" value="descriptions[' + numTags + ']"><label>Ex: Grade, Source, Etc</label><input type="text" name="types[' + numTags + ']" placeholder="Tag Type" value="types[' + numTags + ']"></div>';


var lastTag = document.getElementsByClassName('tag');


var $this = $(lastTag);
$(newTag).insertAfter(lastTag[lastTag.length - 1]);
}

//Passage Edit

//http://www.creativejuiz.fr/blog/en/javascript-en/read-url-get-parameters-with-javascript
function $_GET(param) {
    var vars = {};
    window.location.href.replace(
    /[?&]+([^=&]+)=?([^&]*)?/gi, // regexp
    function( m, key, value ) { // callback
        vars[key] = value !== undefined ? value : '';
    }
    );

    if ( param ) {
     return vars[param] ? vars[param] : null;
    }
    return vars;
}

/*
function acceptWord(word){
    jsRoutes.controllers.SimplePassageController.acceptWord(word.id,$_GET('grade')).ajax({
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


function replaceWord(word, replacement){
    jsRoutes.controllers.SimplePassageController.replaceWord(@passageId, word, replacement).ajax({
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
}*/

