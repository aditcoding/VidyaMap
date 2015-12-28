var restUrlAutoComplete = '/VidyaMap/r/autocomplete/';
var restUrlSearch = '/VidyaMap/r/search/';
var nodes = [];
var sessionAttrs = {};

$(function() {
	getSessionAttrs();
	var subject = sessionAttrs.subject;
	getAutoComplete(subject);
})

function getSessionAttrs(){
	console.log("Getting session attrs");
	$.ajax({
		url : '/VidyaMap/r/getSessionAttrs',
		type : 'GET',
		async : false,
		success : function(data) {
			sessionAttrs = $.parseJSON(data);
		},
		failure : function(emsg) {
			alert("Error:::" + emsg);
		}
	});
}

function getAutoComplete(subject) {
	console.log("Subject: " + subject);
	nodes.length = 0;
	$.ajax({
		url : restUrlAutoComplete,
		type : 'POST',
		data: subject,
		contentType : 'application/json; charset=utf-8',
		async : false,
		success : function(data) {
			nodes = data.split(",");
		},
		failure : function(emsg) {
			alert("Error:::" + emsg);
		}
	});

	$("#box")
	// don't navigate away from the field on tab when selecting an item
	.bind(
			"keydown",
			function(event) {
				if (event.keyCode === $.ui.keyCode.TAB
						&& $(this).autocomplete("instance").menu.active) {
					event.preventDefault();
				}
				if (event.keyCode === $.ui.keyCode.ENTER
						&& !$(this).autocomplete("instance").menu.active) {
					var closest = spellCheck(this.value);
					this.value = closest.trim();
					sendSearchRequest(this.value);
				}
			}).autocomplete(
			{
				minLength : 2,
				autoFocus : true,
				focus : true,
				source : function(request, response) {
					// delegate back to autocomplete, but extract the last term
					response($.ui.autocomplete.filter(nodes,
							extractLast(request.term)));
				},
				select : function(event, ui) {
					var terms = split(this.value);
					// remove the current input
					terms.pop();
					// add the selected item
					var selected = ui.item.value.trim();
					terms.push(selected);
					//this.value = terms.join(" ");
					this.value = selected;
					sendSearchRequest(selected);
					return false;
				}
			});

}

function sendLogRequest(text,type) {
	var logConsent = sessionAttrs.logConsent;
	console.log("here yes " + logConsent + "type of click : " + type);
	if(logConsent === 'true'){
		$.ajax({
			url : '/VidyaMap/r/log',
			type : 'POST',
			data : text + "querytype:" + type,
			contentType : 'application/json',
			async : true,
			success : function(retData) {
				console.log('Logged');
			},
			failure : function(emsg) {
				alert("Error:::" + emsg);
			}
		});
	}
}

function split(val) {
	return val.split(/ \s*/);
}

function extractLast(term) {
	return split(term).pop();
}

function sendSearchRequest(text) {
	var subject = sessionAttrs.subject;
	var callingFunction = arguments.callee.caller.name;
	if(callingFunction != 'click'){
		sendLogRequest(text,'Search box');
	}
	jQuery.ajax({
		url : restUrlSearch,
		type : 'POST',
		data : text + ",:-selectionFromAutoCompleteMenu" + "Subject:" + subject,
		contentType : 'application/json; charset=utf-8',
		dataType : 'json',
		async : false,
		success : function(jsonData) {
			createFDR(jsonData);
		},
		failure : function(emsg) {
			alert("Error:::" + emsg);
		}
	});
}

function levenshtein(a, b) {
	var i,j,cost;
	var d = new Array();
	if (a.length == 0) {
		return b.length;
	}
	if (b.length == 0) {
		return a.length;
	}
	for (i = 0; i <= a.length; i++) {
		d[i] = new Array();
		d[i][0] = i;
	}
	for (j = 0; j <= b.length; j++) {
		d[0][j] = j;
	}
	for (i = 1; i <= a.length; i++) {
		for (j = 1; j <= b.length; j++) {
			if (a.charAt(i - 1) == b.charAt(j - 1)) {
				cost = 0;
			} else {
				cost = 1;
			}
			d[i][j] = Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1,
					d[i - 1][j - 1] + cost);
			if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2)
					&& a.charAt(i - 2) == b.charAt(j - 1)) {
				d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + cost)

			}
		}
	}
	return d[a.length][b.length];
}

function spellCheck(input) {
	var shortest = -1;
	var closest = "";
	for (i = 0; i < nodes.length; i++) {
		if (input == nodes[i]) {
			closest = nodes[i];
			shortest = 0;
			break;
		}
		var lev = levenshtein(input, nodes[i]);
		if (lev <= shortest || shortest < 0) {
			closest = nodes[i];
			shortest = lev;
		}
	}
	return closest;
}