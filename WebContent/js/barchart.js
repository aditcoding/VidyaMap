
function BarChart(params) {
  var input = params.data;
  var margin, width, height, barWidth,
      x, y, xAxis, yAxis,
      data;

  var init = function() {
	  
	  
    data = [];
    var maxTextLength = 0;
    var firstTextLength = 0;
    for (var i in input) {
      var str = JSON.stringify(input[i][0]).replace(new RegExp('"', 'g'), '');
      data.push({ "label": str, "value": input[i][1] });
      maxTextLength = Math.max(maxTextLength, str.length);
      if (i == 0) {
        firstTextLength = maxTextLength;
      }
    }
    

    margin = { top: 50, right: 50, bottom: maxTextLength * 5 + 20, 
               left: 100 + firstTextLength * 5 };
    barWidth = Math.max(35, 1280 / data.length);
    width = data.length * barWidth;
    height = 300;
    x = d3.scale.ordinal()
          .rangeRoundBands([0, width], .1, 1);
    y = d3.scale.linear()
          .range([height, 0]);

    xAxis = d3.svg.axis()
              .scale(x)
              .orient("bottom");
    yAxis = d3.svg.axis()
              .scale(y)
              .orient("left");
  }

  var draw = function(location) {
    var svg = d3.select("#"+location).append("svg")
                .attr("width", width + margin.left + margin.right)
                .attr("height", height + margin.top + margin.bottom)
                .append("g")
                .attr("transform", "translate(" + margin.left + "," +
                      margin.top + ")");

    x.domain(data.map(function(d) { return d.label; }));
    y.domain([0, d3.max(data, function(d) { return d.value; })]);

    svg.append("g")
       .attr("class", "x axis")
       .attr("transform", "translate(0," + height + ")")
       .call(xAxis)
       .selectAll("text")  
       .style("text-anchor", "end")
       .attr("dx", "-.8em")
       .attr("dy", ".15em")
       .attr("transform", function(d) {
          return "translate(3, -1) rotate(-30)"; 
        });

    svg.append("g")
       .attr("class", "y axis")
       .call(yAxis)
       .append("text")
       .attr("transform", "rotate(-90)")
       .attr("y", 6)
       .attr("dy", ".71em")
       .style("text-anchor", "end")
       .text("Count");

    svg.selectAll(".bar")
       .data(data)
       .enter().append("rect")
       .attr("class", "bar")
       .attr("x", function(d) { return x(d.label); })
       .attr("width", x.rangeBand())
       .attr("y", function(d) { return y(d.value); })
       .attr("height", function(d) { return height - y(d.value); });
  }

  this.draw = draw;
  init();
}
