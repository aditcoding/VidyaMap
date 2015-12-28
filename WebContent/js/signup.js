var students = [];
var schools = [];
var classes = [];
var terms = [];
var teachers = [];
var retData = [];

$(document).ready(function(){
    $("#mySelect").change(function(){
        $( "select option:selected").each(function(){
        	
        	if($(this).attr("value")=="NoValue"){
             	$(".register").hide();
            }
        	
        	if($(this).attr("value")=="admin"){
                $(".register").hide();
                $(".admin").show();
            }
        	
        	if($(this).attr("value")=="guest"){
                $(".register").hide();
                $(".guest").show();
            }
        	
        	if($(this).attr("value")=="researcher"){
                $(".register").hide();
                $(".researcher").show();
            }
        	
            if($(this).attr("value")=="school"){
                $(".register").hide();
                $(".school").show();
            }
            
            if($(this).attr("value")=="teacher"){
            	$(".register").hide();
                $(".teacher").show();
                if(schools.length == 0){
                	$.ajax({
                        url : "/VidyaMap/r/getSchools",
                        type : 'GET',
                        async : false,
                        success : function(data) {
                           retData = data;
                           schools = retData.split(",");
                        },
                        failure : function(emsg) {
                            alert("Error:::" + emsg);
                        }
                    });
                }
                var options = $(".teacher #schoolSelect");
                if(options.children().length == 0) {
	                $.each(schools, function(item) {
	             	   var name = schools[item].split(":")[0];
	             	   var id = schools[item].split(":")[1];
	                    options.append($("<option />").val(id).text(name));
	                });
                }
            }
            
            if($(this).attr("value")=="student"){
            	$(".register").hide();
                $(".student").show();
                if(schools.length == 0){
                	$.ajax({
                        url : "/VidyaMap/r/getSchools",
                        type : 'GET',
                        async : false,
                        success : function(data) {
                           retData = data;
                           schools = retData.split(",");
                        },
                        failure : function(emsg) {
                            alert("Error:::" + emsg);
                        }
                    });
                }
                var options = $(".student #schoolSelect");
                if(options.children().length == 0) {
                    $.each(schools, function(item) {
                 	   var name = schools[item].split(":")[0];
                 	   var id = schools[item].split(":")[1];
                        options.append($("<option />").val(id).text(name));
                    });
                }
            }
            if($(this).attr("value")=="class"){
            	$(".register").hide();
                $(".class").show();
                if(teachers.length == 0){
	                $.ajax({
	                    url : "/VidyaMap/r/getTeachers",
	                    type : 'GET',
	                    success : function(data) {
	                       var options = $("#teacherSelect");
	                       teachers = data.split(",");
	                       $.each(teachers, function(item) {
	                    	   var name = teachers[item].split(":")[0];
	                    	   var id = teachers[item].split(":")[1];
	                           options.append($("<option />").val(id).text(name));
	                       });
	                    },
	                    failure : function(emsg) {
	                        alert("Error:::" + emsg);
	                    }
	                });
                }
            }
            if($(this).attr("value")=="group"){
            	$(".register").hide();
                $(".group").show();
                if(terms.length == 0){
	                $.ajax({
	                    url : "/VidyaMap/r/getTerms",
	                    type : 'GET',
	                    success : function(data) {
	                       var options = $("#termSelect");
	                       terms = data.split(",");
	                       $.each(terms, function(item) {
	                    	   var name = terms[item].split(":")[0];
	                    	   var id = terms[item].split(":")[1];
	                           options.append($("<option />").val(id).text(name));
	                       });
	                    },
	                    failure : function(emsg) {
	                        alert("Error:::" + emsg);
	                    }
	                });
                }
                if(classes.length == 0){
	                $.ajax({
	                    url : "/VidyaMap/r/getClasses",
	                    type : 'GET',
	                    success : function(data) {
	                       var options = $("#classSelect");
	                       classes = data.split(",");
	                       $.each(classes, function(item) {
	                    	   var name = classes[item].split(":")[0];
	                    	   var id = classes[item].split(":")[1];
	                           options.append($("<option />").val(id).text(name));
	                       });
	                    },
	                    failure : function(emsg) {
	                        alert("Error:::" + emsg);
	                    }
	                });
                }
                if(students.length == 0){
	                $.ajax({
	                    url : "/VidyaMap/r/getStudents",
	                    type : 'GET',
	                    success : function(data) {
	                       var options = $("#studentSelect");
	                       students = data.split(",");
	                       $.each(students, function(item) {
	                    	   var name = students[item].split(":")[0];
	                    	   var id = students[item].split(":")[1];
	                           options.append($("<option />").val(id).text(name));
	                       });
	                       $(".chosen").chosen();
	                    },
	                    failure : function(emsg) {
	                        alert("Error:::" + emsg);
	                    }
	                });
                }
            }
            
            if($(this).attr("value")=="term"){
            	$(".register").hide();
                $(".term").show();
            }
        });
    }).change();
});

$(document).ready(function(){
	
	var placeholder = null;
	$('input[type=text]').focus(function(){
	    placeholder = $(this).attr("placeholder");
	    $(this).attr("placeholder","");
	});
	
	$('input[type=text]').blur(function(){
		$(this).attr("placeholder", placeholder);
	});
	
});

function checkUserNameExists(object) {
    console.log("Here in username check");
   $.ajax({
      url: "/VidyaMap/r/username",
      type: 'POST',
      contentType : 'application/json; charset=utf-8',
      dataType : 'json',
      data: object.value,
      success: function(result){
                 if(result != 0){
                	 object.setCustomValidity('Username/Groupname already exists');
                 }
                 else{
                	 object.setCustomValidity('');
                 }
               }
      });
}

function match(password, who) {
	if (password.value != $("." + who + " #password").val()) {
	password.setCustomValidity('Password do not match');
	} else {
	// input is valid -- reset the error message
	password.setCustomValidity('');
	}
}