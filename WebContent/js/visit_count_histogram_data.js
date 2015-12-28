


var vc_hist = [["[0, 6]",1178],["[6, 12]",1582],["[12, 18]",1044],["[18, 24]",643],["[24, 30]",443],["[30, 36]",283],["[36, 42]",207],["[42, 48]",152],["[48, 54]",75],["[54, 60]",75],["[60, 66]",45],["[66, 72]",50],["[72, 78]",30],["[78, 84]",16],["[84, 90]",24],["[90, 96]",16],["[96, 102]",11],["[102, 108]",7],["[108, 114]",10],["[114, 120]",5],["[120, 126]",8],["[126, 132]",4],["[132, 138]",4],["[138, 144]",1],["[144, 150]",3],["[150, 156]",2],["[156, 162]",2],["[162, 168]",3],["[168, 174]",3],["[174, 180]",2],["[180, 186]",2],["[186, 192]",1],["[252, 258]",1]];
var hist = [["0000-00-00 00:00:00", "Test topic 1"], ["0000-00-00 00:00:00", "Test topic 2"], ["0000-00-00 00:00:00", "Test topic 3"]];
var logs = [];

var restUrl = '/VidyaMap/r/analysis/';
	
function getlogs(s) {

    jQuery.ajax({
        url : restUrl,
        type : 'POST',
        async : false,
        data : s,
        contentType : 'application/json; charset=utf-8',
        dataType : 'json',
        success : function(jsonData) {
            extractTimeline(jsonData);
        },
        failure : function(emsg) {
            alert("Error:::" + emsg);
        }
    });
}

function extractTimeline(jsonData) {
	//hist = jsonData;
	console.log("edited entry");
	jsonData.Logs.forEach(function(x){
		var row = [];
		row.push(x.time,x.question);
		logs.push(row);
	});
	console.log(logs);
	console.log(hist);
}