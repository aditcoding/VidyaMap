function HeatMap(params) {
  var input = params.data;
  var rowNumber, colNumber,
      rowLabel, colLabel,
      hcrow, hccol,
      rowIndex, colIndex,
      data,
      cellSize,
      margin, width, height,
      colorBuckets, colors;
  var minValue, maxValue;

  var init = function() {
    rowNumber = 0;
    colNumber = 0;
    rowLabel = [];
    colLabel = [];
    hcrow = [];
    hccol = [];
    rowIndex = {};
    colIndex = {};
    data = [];
    var maxTextLength = 0;

    for (var i in input) {
      var rowName = JSON2STRING(input[i][1]);
      var colName = JSON2STRING(input[i][0]);
      var value = input[i][2];

      if (i == 0) {
        minValue = value;
        maxValue = value;
      } else {
        if (value < minValue) {
          minValue = value;
        }
        if (value > maxValue) {
          maxValue = value;
        }
      }
      
      if (!(rowName in rowIndex)) {
        rowNumber++;
        hcrow.push(rowNumber);
        rowLabel.push(rowName);
        rowIndex[rowName] = rowNumber;
      }
      if (!(colName in colIndex)) {
        colNumber++;
        hccol.push(colNumber);
        colLabel.push(colName);
        colIndex[colName] = colNumber;
      }
      data.push({"row": rowIndex[rowName], 
                 "col": colIndex[colName],
                 "value": value});

      maxTextLength = Math.max(maxTextLength,
         JSON.stringify(input[i][0]).replace(new RegExp('"', 'g'), '').length);
    }
    console.log(maxTextLength);

    cellSize = Math.min(Math.max(600 / colNumber, 15), 30);
    margin = { top: 40 + maxTextLength * 5, right: 30 + maxTextLength * 5, bottom: 100, left: 200 };
    width = cellSize * colNumber;
    height = cellSize * rowNumber;
/*    colors1 = ['#00E53A', '#00E18E', '#00DBDD', '#0086D9',
              '#0033D5', '#1C00D2', '#6900CE', '#B300CA',
              '#C60092', '#C20047', '#BF0100'];
*/
    colorBuckets = 21;
    colorScale = [];
    for (var i = 1; i < colorBuckets; i++) {
      colorScale.push(i);
    }
    colors = colorScale.map(function(level) {
      var s = (colorBuckets - level) / (colorBuckets - 1.0) * 255;
      return d3.rgb(s, s, s);
    });
  }

  var draw = function(location) {
    var sqrt2 = Math.sqrt(2);
    var colorScale = d3.scale.quantile()
        .domain([minValue, maxValue])
        .range(colors);
  
    var svg = d3.select("#"+location).append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var rowSortOrder = false;
    var colSortOrder = false;
    var rowLabels = svg.append("g")
        .selectAll(".rowLabelg")
        .data(rowLabel)
        .enter()
        .append("text")
        .text(function (d) { return d; })
        .attr("x", 0)
        .attr("y", function (d, i) { return hcrow.indexOf(i+1) * cellSize; })
        .style("text-anchor", "end")
        .attr("transform", "translate(-6," + cellSize / 1.5 + ")")
        .attr("class", function (d,i) { return "rowLabel mono r"+i;} ) 
        .on("mouseover", function(d) {
           d3.select(this).classed("text-hover",true);
         })
        .on("mouseout" , function(d) {
           d3.select(this).classed("text-hover",false);
         })
        .on("click", function(d,i) {
           rowSortOrder = !rowSortOrder;
           sortbylabel("r", i, rowSortOrder);
           d3.select("#order")
             .property("selectedIndex", 4)
             .node()
             .focus();
         });

    var colLabels = svg.append("g")
        .selectAll(".colLabelg")
        .data(colLabel)
        .enter()
        .append("text")
        .text(function (d) { return d; })
        .attr("x", function (d, i) {
           return Math.round(hccol.indexOf(i+1) * cellSize / sqrt2);
         })
        .attr("y", function (d, i) {
           return Math.round(hccol.indexOf(i+1) * cellSize / sqrt2);
         })
        .style("text-anchor", "left")
        .attr("transform", "translate("+cellSize/2 + ",-6) rotate(-45)")
        .attr("class",  function (d, i) { return "colLabel mono c"+i;} )
        .on("mouseover", function(d) {
           d3.select(this).classed("text-hover",true);
         })
        .on("mouseout" , function(d) {
           d3.select(this).classed("text-hover",false);
         })
        .on("click", function(d, i) {
           colSortOrder = !colSortOrder;
           sortbylabel("c", i, colSortOrder);
           d3.select("#order")
             .property("selectedIndex", 4)
             .node()
             .focus();
         });
   
    var heatMap = svg.append("g").attr("class","g3")
        .selectAll(".cellg")
        .data(data,function(d){ return d.row+":"+d.col;})
        .enter()
        .append("rect")
        .attr("x", function(d) { return hccol.indexOf(d.col) * cellSize; })
        .attr("y", function(d) { return hcrow.indexOf(d.row) * cellSize; })
        .attr("class", function(d){
           return "cell cell-border cr" + (d.row-1) + " cc" + (d.col-1);
         })
        .attr("width", cellSize)
        .attr("height", cellSize)
        .style("fill", function(d) { return colorScale(d.value); })
        /* .on("click", function(d) {
               var rowtext=d3.select(".r"+(d.row-1));
               if(rowtext.classed("text-selected")==false){
                   rowtext.classed("text-selected",true);
               }else{
                   rowtext.classed("text-selected",false);
               }
        })*/
        .on("mouseover", function(d){
           //highlight text
           d3.select(this).classed("cell-hover",true);
           d3.selectAll(".rowLabel")
             .classed("text-highlight", function(r, ri) {
                return ri == (d.row-1);
              });
           d3.selectAll(".colLabel")
             .classed("text-highlight", function(c, ci) {
                return ci == (d.col-1);
              });
           //Update the tooltip position and value
           d3.select("#tooltip")
             .style("left", (d3.event.pageX+10) + "px")
             .style("top", (d3.event.pageY-10) + "px")
             .select("#value")
             .text(rowLabel[d.row-1] + "," + colLabel[d.col-1] +
                   "\ndata:" + d.value);
         })
        .on("mouseout", function(){
               d3.select(this).classed("cell-hover",false);
               d3.selectAll(".rowLabel").classed("text-highlight",false);
               d3.selectAll(".colLabel").classed("text-highlight",false);
         });


    var sortLabels = function() {
      var t = svg.transition().duration(0);
      var rowValues = [];
      var colValues = []
      var rowSorted;
      var colSorted;

      d3.selectAll(".rowLabel") 
        .filter(function(v) { rowValues.push(v); });
      rowSorted = d3.range(rowNumber)
                    .sort(function(a, b) {
                       return JSON.stringify(rowValues[a]).localeCompare(
                                JSON.stringify(rowValues[b]));
                     });
      d3.selectAll(".colLabel") 
        .filter(function(v) { colValues.push(v); });
      colSorted = d3.range(colNumber)
                    .sort(function(a, b) {
                       return JSON.stringify(colValues[a]).localeCompare(
                                JSON.stringify(colValues[b]));
                     });
      t.selectAll(".rowLabel")
       .attr("y", function (d, i) {
          return rowSorted.indexOf(i) * cellSize;
        });
      t.selectAll(".colLabel")
       .attr("x", function (d, i) {
          return Math.round(colSorted.indexOf(i) * cellSize / sqrt2);
        })
       .attr("y", function (d, i) {
          return Math.round(colSorted.indexOf(i) * cellSize / sqrt2);
        });
      t.selectAll(".cell")
       .attr("x", function(d) {
          return colSorted.indexOf(d.col-1) * cellSize;
        })
       .attr("y", function(d) {
          return rowSorted.indexOf(d.row-1) * cellSize;
        });
    }

    sortLabels();
  }

  this.draw = draw;
  init();
}





