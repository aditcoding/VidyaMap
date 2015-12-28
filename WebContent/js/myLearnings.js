
function init() {
  console.log("loading the initial data for MyLearning");
  
  console.log("fetching logs from database");
  getlogs("11");
  
  new VisitHistory(["Test time", "Test question"],
          "visit_table", logs);
  
 //new DataTableModule(["Click count interval", "Student count"],
  //                     "hist_table", vc_hist);
  new VisualizationModule("barchart", "hist_vis", vc_hist);
  
  new DataTableModule(["Concept from", "Concept to", "Jump count"],
                       "heatmap_table", cc_heatmap);
  new VisualizationModule("heatmap", "heatmap_vis", cc_heatmap);

}

function VisitHistory(h, loc, input) {
    var headers = [];
    for (var i in h) {
      headers.push({"title":h[i]}); 
    }
    document.getElementById(loc).innerHTML =
      '<table id=t' + loc + ' class="display" style=\'width: 100%; ' +
      'table-layout: fixed; word-wrap:break-word;\'>' +
      '</table>';
    
    //input = hist;
    var data = [];
    for (var i in input) {
      var row = [];
      for (var j in input[i]) {
        row.push(Column2String(input[i][j]));
      }
      data.push(row);
    }

    $('#t' + loc).dataTable( {
      "data": data,
      "columns": headers,
      "scrollY": "250px",
      "scrollCollapse": true,
      "paging": false,
      "sDom": '<"top"i>rt<"bottom"flp><"clear">'
    });
}

function DataTableModule(h, loc, input) {
      var headers = [];
      for (var i in h) {
        headers.push({"title":h[i]}); 
      }
      document.getElementById(loc).innerHTML =
        '<table id=t' + loc + ' class="display" style=\'width: 100%; ' +
        'table-layout: fixed; word-wrap:break-word;\'>' +
        '</table>';
      
      var data = [];
      for (var i in input) {
        var row = [];
        for (var j in input[i]) {
          row.push(Column2String(input[i][j]));
        }
        data.push(row);
      }

      $('#t' + loc).dataTable( {
        "data": data,
        "columns": headers,
        "scrollY": "250px",
        "scrollCollapse": true,
        "paging": false,
        "sDom": '<"top"i>rt<"bottom"flp><"clear">'
      });
}

function VisualizationModule(kind, loc, input) {
    // update graph information
    var params = { "data" : input };
    var chart;
    document.getElementById(loc).innerHTML = "";
    switch(kind) {
      case "heatmap":
        chart = new HeatMap(params);
        chart.draw(loc);
        break;
      case "barchart":
        chart = new BarChart(params);
        chart.draw(loc);
      default:
        break;
    }
}


function JSON2STRING(o) {
  var ret;
  if (Array.isArray(o)) {
    // array
    ret = "[";
    for (var idx in o) {
      if (idx != 0) {
        ret += ", ";
      }
      ret += JSON2STRING(o[idx]);
    }
    ret += "]";
  } else {
    ret = o.toString();
  }

  return ret;
}

function Column2String(o) {
  var ret = "";
  
  //Json parsing
  //var str = JSON.parse(o);
  //console.log(str);
  
  if (Array.isArray(o)) {
    // array
    for (var idx in o) {
      if (idx != 0) {
        ret += "; ";
      }
      ret += o[idx];
    }
  } else {
    ret = o.toString();
  }

  return ret;
}

