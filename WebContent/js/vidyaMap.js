var VizWidth = document.getElementById("background").offsetWidth,
    VizHeight = document.getElementById("background").offsetHeight;

var offset = VizWidth / 2,	
	offsetX = VizWidth / 2,
	offsetY = VizHeight / 2;// x,y offset of the center node from svg container 0,0
	
var color = d3.scale.category20();
var defaultNodeColor = "green"

var randDeviation = 0;
//scale the size of nodes and links based on window size and resolution
// Map looks fine on window width of 952, So taking that as the base
var WindowScaleFactor = document.getElementById("background").offsetWidth/952; 

var context; // sets current context. i.e. the type of center node (relationship or entity)
var center = 0; // center node index, randomly set to 4
var clickedNodePos = {
    x: 500,
    y: 500
};

// temporary offset set at the clicked node position. It moves towards the
// offset with each tick. This is just to make the center node movement
// smooth.
var tmpOffset = {
    x: 500,
    y: 500
};

var noOfLevelsToBeDisplayed = 4;
var first = 1;

var l1EdgeLength = 150*WindowScaleFactor;
var l1Tol1EdgeLength = 150*WindowScaleFactor;
var l1Tol2EdgeLength = 250*WindowScaleFactor;
var l1Tol2IndependentEdgeLength = 150*WindowScaleFactor;
var defaultEdgeLength = 90*WindowScaleFactor;

// size of nodes
var centerNodeWidth = 100;
var l1NodeWidth = 85;
var centerNodeHeight = 100*WindowScaleFactor;
var l1NodeHeight = 30*WindowScaleFactor;

// for circle nodes
var centerNodeSize = 100*WindowScaleFactor;
var l1NodeSize = 100*WindowScaleFactor;

// fisheye
var fishEyeScaleFactor = 22;
var fishEyeDistortionFactor = 2;
var fishEyeRadius = 200;

var restUrl = '/VidyaMap/r/search/'

//force directed layout: force parameter settings
var force =
    d3.layout.force().linkDistance(function(d) {
        if (d.source.level == 0 || d.target.level == 0) return l1EdgeLength; // root to level 1 connection
        else if (d.source.level <= 1 && d.target.level <= 1) return l1Tol1EdgeLength; // level 1 to level 1 connection
        else if ((d.source.level <= 1 || d.target.level <= 1) && (d.source.parents.length > 1 || d.target.parents.length > 1)) return l1Tol2EdgeLength;
        else if (d.source.level <= 1 || d.target.level <= 1) return l1Tol2IndependentEdgeLength; // level 1 to level 2 connection
        else return defaultEdgeLength;
    }).linkStrength(function(d) {
        if (d.invisible) return 0;
        else if (d.source.level == 0 || d.target.level == 0) return 0.3;
        else if ((d.source.level <= 1 || d.target.level <= 1) && (d.source.parents.length > 1 || d.target.parents.length > 1)) return 0.2;
        else if (d.source.level <= 1 && d.target.level <= 1) return 0.3;
        else if (d.source.level == 2 && d.target.level == 2) return 0.01;
        else if (d.source.level <= 1 || d.target.level <= 1) return 0.2;
        else return 0.3;
    }).charge(function(d) {
        if (d.invisible) return 0;
        else if (d.level == 0) return -500;
        else if (d.level == 1) return -80;
        else if (d.level == 2) return -200;
        else return 0;
    }).gravity(0).theta(1).friction(.95).size([VizWidth, VizHeight]);

// fisheye
var fisheye = d3.fisheye.circular().radius(fishEyeRadius).distortion(fishEyeDistortionFactor);

// Adi start

var node, link, original_link;

function createFDR(graph) {
	//delete and recreate the svg element on each new search
    d3.select("svg").remove();
    svg = d3.select("div.background").append("svg").attr("width", "100%").attr(
        "height", "100%");
	
	//arrow marker for links. Two markers: one for L1 links and other for others
    svg.append("svg:defs").selectAll("marker")
        .data(["arrow", "arrowLevel1"])
        .enter().append("svg:marker")
        .attr("id", String)
        .attr("viewBox", "0 0 10 10")
        .attr("refX", 0)
        .attr("refY", 5)
        .attr("markerWidth", function(d) {
			return d=="arrowLevel1" ? 6 : 3;})
        .attr("markerHeight", function(d) {
			return d=="arrowLevel1" ? 6 : 4;})
        .attr("orient", "auto")
        .append("svg:path")
        .attr("d", "M 0 0 L 10 5 L 0 10 z")
        .style("fill", "#666666");
	
	//read nodes and links and associate with force
    var nodeMap = {};
    graph.nodes.forEach(function(x) {
        nodeMap[x.id] = x;
    });
    graph.links = graph.links.map(function(x) {
        return {
            source: nodeMap[x.source],
            target: nodeMap[x.target],
            value: x.value,
            info: x.info
        };
    });

    force.nodes(graph.nodes).links(graph.links).start();

	//simulate first node click event after 2s
    setTimeout(simulateClick, 2000);

	addNodeElements(graph);
	addLinkElements(graph);
	
    createAdjacencyList();
	
    //findSectorAngles(center);

	//force layout simulation. Defines what happens with each simulation tick
    force.on("tick", function(e) {
        tickCount++;
		
		//place level1 (L1) nodes in circular fashion around the center node
        seedInitialPlacementofL1Nodes(center);

        center_node_x = (node[0][center].__data__.x ? node[0][center].__data__.x : 0);
        center_node_y = (node[0][center].__data__.y ? node[0][center].__data__.y : 0);

        // calculate tmpOffset position for center node. Makes the transition of center node to Center of the Visualization field
        if (tickCount < 200) {
            tmpOffset.x -= (clickedNodePos.x - offsetX) / 100;
            tmpOffset.y -= (clickedNodePos.y - offsetY) / 100;
        }
		//offset each node w.r.t center node which is at tmpOffset
        node.each(function(d) {
            d.x = d.x - center_node_x + tmpOffset.x;
            d.y = d.y - center_node_y + tmpOffset.y;
        });

        //if(tickCount<200)seedInitialPlacementofL2Nodes(true);
        //if(tickCount==200)seedInitialPlacementofL2Nodes(false);

		//Move the map to the left end if the nodes are going beyond the Visualization Pane boundary
        adjustMapHorizontally(tickCount);

        // update node size and nodes' fisheye parameter after fisheye distortion
        addFisheyeDistortion(center_node_x, center_node_y);
		
		//update the node and link positions
		updateNodePositions();
		updateLinksPositions();
		
    });
}

function simulateClick() {
    first = 0;
    click(node[0][center].__data__, 0);
    force.start();
}

function addNodeElements(graph){
    node = svg.selectAll(".node").data(graph.nodes);
    node.exit().remove();

    var nodeEnter = node.enter().append("g").attr("class", "node");
		//.call(force.drag);

	//append rect elementts. rect with rx and ry (curved corners) will look like ellipse
    nodeEnter.append("rect").attr("class", "node").attr("width", function(d) {
        return (d.level == 0) ? centerNodeWidth*WindowScaleFactor : l1NodeWidth*WindowScaleFactor;
    }).attr("height", function(d) {
        return (d.level == 0) ? centerNodeHeight : l1NodeHeight*WindowScaleFactor;
    }).attr("rx", function(d) {
        return (d.level == 0) ? centerNodeWidth*WindowScaleFactor * 0.5 : l1NodeWidth*WindowScaleFactor * 0.5;
    }).attr("ry", function(d) {
        return (d.level == 0) ? centerNodeHeight * 0.5 : l1NodeHeight*WindowScaleFactor * 0.5;
    }).style("fill", function(d) {
        return color(1);
    }).style("visibility", function(d) {
        return d.level > 2 ? "hidden" : "visible";
    });
	
    nodeEnter.append("text")
        .text(function(d) {
            return d.name;
        }).on("click", click);
}

function addLinkElements(graph){
    link = svg.selectAll(".link").data(graph.links);

    link.exit().remove();

    linkEnter = link.enter().append("g").attr("class", "link");

    link = linkEnter
        .append("line")
        .attr("class", "link")
        .call(force.drag)
        .style("stroke-width", function(d) {
            return (d.target.group == 12 || d.source.group == 12) ? 4 : 1;
        })
        .style("visibility", function(d) {
            return (d.source.level > 2 || d.target.level > 2) ? "hidden" : "visible";
        }).attr("marker-end", function(d) {
			return (d.target.level == 0 || d.source.level == 0) ? "url(#arrow)" : "url(#arrowLevel1)" 
		});

    linkEnter.append("text").attr("class", "linklabel").text(function(d) {
        return d.invisible ? "" : (d.info);
    }).attr("x", function(d) {
        return (d.source.y + d.target.y) / 2;
    }).attr("y", function(d) {
        return (d.source.x + d.target.x) / 2;
    }).attr("text-anchor", "end").attr("fill", "black");
}

var tickCount = 0;

function click(d, i) {
	
	var nodeName = node[0][i].__data__.name;
	var callingFunction = arguments.callee.caller.name;
	if(callingFunction === 'textClicked'){
		sendSearchRequest(nodeName);
		sendLogRequest(nodeName,'Text click');
		return;
	}
	if(callingFunction == ''){
		sendSearchRequest(nodeName);
		sendLogRequest(nodeName,'Map click');
		return;
	}
	
	tickCount = 0;
    center = i;
    context = node[0][i].__data__.type;
    clickedNodePos = {
        x: node[0][i].__data__.x,
        y: node[0][i].__data__.y
    };
    tmpOffset = clickedNodePos;

    updateNodeLevels(center);
    applyFiltering();
    findIndependentL2Nodes();

    //updateNodeLevels(center);
    groupL1Nodes(center);
    //randDeviation = Math.random()/20;
    randDeviation = 0;
    seedInitialPlacementofL1Nodes(center);
    //seedInitialPlacementofL2Nodes();
    //findSectorAngles(center);
    //enforceSectorConstraint(); // keep each element within its sector boundary
    changeNodeLinkDisplay();
    // display content
    jQuery("#textDivField").html(node[0][center].__data__.text);

    highLightNodesInText();
	
	//force.stop();
    force.start();

}

function adjustMapHorizontally(tickCount){	
	var minX = VizWidth,
		maxX = 0;
	// adjust horizontally 
	if (tickCount > 150) {
		node.each(function(d) {
			if (!d.invisible) {
				if (d.x + d.textWidth / 2 > maxX) maxX = d.x + d.textWidth;
				if (d.x - d.textWidth / 2 < minX) minX = d.x - d.textWidth;
			}
		});

		//console.log("max: " + maxX + ", " + document.getElementById("background").offsetWidth + ", " + minX);
		if (minX < 10 || maxX > VizWidth) {
			node.each(function(d) {
				d.x = d.x - minX + 10;
			});
		}
	}
}

function addFisheyeDistortion(center_node_x, center_node_y){	
	fisheye.focus([center_node_x, center_node_y]);
	node.each(function(d) {
		d.fisheye = fisheye(d);
	});
	
	//update node height and width based on levels and fisheye distortion
	node.selectAll("text").each(function(d) {
		d.textWidth = this.getBBox().width;
		d.textHeight = this.getBBox().height;
	});
	
	node.selectAll("rect").attr("width", function(d) {
		return Math.max(d.fisheye.z * l1NodeWidth*WindowScaleFactor, d.textWidth * 1.2);
	}).attr("height", function(d) {
		return d.fisheye.z * l1NodeHeight*WindowScaleFactor;
	}).attr("rx", function(d) {
		return Math.max(d.fisheye.z * l1NodeWidth*WindowScaleFactor, d.textWidth * 1.2) * 0.5;
	}).attr("ry", function(d) {
		return d.fisheye.z * l1NodeHeight*WindowScaleFactor * 0.5;
	});
}

function updateNodePositions(){
	node.attr("transform", function(d) {
		return "translate(" + (d.fisheye.x - Math.max(d.fisheye.z * l1NodeWidth*WindowScaleFactor, d.textWidth * 1.2) / 2) + "," + (d.fisheye.y - (d.fisheye.z * l1NodeHeight*WindowScaleFactor) / 2) + ")";
	});

	node.selectAll("text").attr("transform", function(d) {
		return "translate(" + Math.max(d.fisheye.z * l1NodeWidth*WindowScaleFactor, d.textWidth * 1.2) / 2 + "," + (d.fisheye.z * l1NodeHeight*WindowScaleFactor + d.textHeight / 2) / 2 + ")";
	}).style("font-family", function(d) {
		return "Arial";
	}).style("font-size", function(d) {
		return (d.level == 0) ? 25*WindowScaleFactor+"px" : 15*WindowScaleFactor+"px";
	}).style("font-style", function(d) {
		return (d.level == 0) ? "italic" : "normal";
	});
}

function performScaling() {	
	WindowScaleFactor = document.getElementById("background").offsetWidth/600;
	WindowScaleFactor = WindowScaleFactor >1 ? 1 : WindowScaleFactor;
	VizWidth = document.getElementById("background").offsetWidth;
	VizHeight = document.getElementById("background").offsetHeight;

	//console.log(VizHeight);
	offsetX = VizWidth / 2;
	offsetY = VizHeight / 2;// x,y offset of the center node from svg container 0,0
	
	 l1EdgeLength = 150*WindowScaleFactor;
	 l1Tol1EdgeLength = 150*WindowScaleFactor;
	 l1Tol2EdgeLength = 250*WindowScaleFactor;
	 l1Tol2IndependentEdgeLength = 150*WindowScaleFactor;
	 defaultEdgeLength = 90*WindowScaleFactor;

	// update size of nodes
	 centerNodeWidth = 100;
	 l1NodeWidth = 85;
	 centerNodeHeight = 100*WindowScaleFactor;
	 l1NodeHeight = 30*WindowScaleFactor;
	 centerNodeSize = 100*WindowScaleFactor;
	 l1NodeSize = 100*WindowScaleFactor;
	 click(node[0][center].__data__,center);
	 //force.start();
}

function highLightNodesInText() {
	var nodesArray = [];
    node.each(function(d) {
        if (d.level == 1) nodesArray.push(d.name);
    });
	
    var theText = jQuery("#textDivField");
    var replaced = theText.html();
    for (var i = 0; i < nodesArray.length; i++) {
        var word = nodesArray[i];
        var spanTag = "<span onclick=\"textClicked(this)\" class=\"highlightSpan\">";
        var regex = new RegExp('\\b(' + word + ')\\b', 'gi');
        replaced = replaced.replace(regex, spanTag + "$1 </span>");
    }
    theText.html(replaced);
}

function textClicked(word) {
    var nodeName = $(word).text();
    var i = -1;
    //console.log(i);
    node.each(function(d) {
        if (d.name.toUpperCase() === nodeName.toUpperCase().substring(0, nodeName.length - 1)) {
			i = d.index;
		}
    });
	
    if (i >= 0) click(node[0][center].__data__, i);
}

function findIndependentL2Nodes() {
    node.each(function(d) {
        d.parents = []; //stores the node index of the parent
        d.numberOfL1Connections = 0;
    }); //keep the number of L1 connections for each L2 nodes

    link.each(function(d) {
        if (!d.invisible) {
            if (d.source.level == 2 && d.target.level == 1) {
                var parentExists = false;
                for (var j = 0; j < d.source.parents.length; j++) {
                    if (d.source.parents[j] == d.target.index) {
                        parentExists = true;
                    }
                }
                if (!parentExists) {
                    d.source.numberOfL1Connections++;
                    d.source.parents.push(d.target.index);
                }
            }

            if (d.target.level == 2 && d.source.level == 1) {
                var parentExists = false;
                for (var j = 0; j < d.target.parents.length; j++) {
                    if (d.target.parents[j] == d.source.index) {
                        parentExists = true;
                    }
                }
                if (!parentExists) {
                    d.target.numberOfL1Connections++;
                    d.target.parents.push(d.source.index);
                }
            }
        }
    });
    //printNodeInfo();
}

function printNodeInfo() {
    node.each(function(d) {
        console.log("Name: " + d.name + "Parent: " + d.parents + "No. of Parents" + d.parents.length + "Level" + d.level);
    });
}

function applyFiltering() {
    // make invisible all the links which are connected to first level incoming nodes
    node.each(function(d) {
        d.direction = "incoming";
    });

    // set outgoing first level nodes
    link.each(function(d) {
        d.invisible = false;
        if (d.source.level == 0 && d.target.level == 1) {
            d.target.direction = "outgoing";
        }
    });

    link.each(function(d) {
        if ((d.source.level == d.target.level) || (d.source.level > 2 || d.target.level > 2) || ((d.source.level == 2 || d.target.level == 2) && (d.source.type != context || d.target.type != context))) {
            d.invisible = true;
            //console.log("making invisible:" + d.source.name + ", -> " + d.target.name); 
        }
    });

    // take care of the second level nodes
    node.each(function(d) {
        if (d.level > 1) {
            d.invisible = true;
        } else d.invisible = false;
    });

    link.each(function(d) {
        //if(d.source.level==1 && d.target.level == 2 && d.source.direction == "outgoing") {
        if (!d.invisible) {
            d.target.invisible = false;
            d.source.invisible = false;
        }
    });

    /* link.each(function(d) {
		if(d.source.invisible || d.target.invisible) d.invisible = true;
	}); */

    var number = 0;

}

// return angle from origin in the range of 0 - 2PI
function getAngle(x, y) {
    var theta = Math.atan2(y, x);
    if (theta < 0)
        return 2 * Math.PI + theta;
    return theta;
}

// rotates a point at x,y to a new location with angle theta with origin and
// radial distance same as that of x,y
function rotate(x, y, theta) {
    var newTheta = getAngle(x, y) + theta;

    var radialDistance = Math.sqrt(x * x + y * y);
    var newX = radialDistance * Math.cos(newTheta);
    var newY = radialDistance * Math.sin(newTheta);
    return {
        x: newX,
        y: newY
    };
}

var L1GroupSectorRange = []; // contains the theta range for this group of nodes
L1GroupSectorRange[0] = 0;

function findSectorAngles(center) {
    var L1GroupFrequency = []; // contains the number of nodes in the
    // corresponding group number. Group number
    // starts with 1;
    L1GroupFrequency[0] = 0;
    // find the number of nodes in each group
    for (var i = 0; i < adjacencyList[center].size; i++) {
        curL1NodeIndex = adjacencyList[center].connections[i].node_index;
        groupNumber = node[0][curL1NodeIndex].__data__.L1Group;
        if (L1GroupFrequency[groupNumber])
            L1GroupFrequency[groupNumber]++;
        else
            L1GroupFrequency[groupNumber] = 1;
    }

    // calculate the angle range each group needs.
    var thetaPerNode = (2 * Math.PI / adjacencyList[center].size);
    for (var i = 1; i <= adjacencyList[center].size; i++) {
        L1GroupSectorRange[i] = L1GroupSectorRange[i - 1] + thetaPerNode * L1GroupFrequency[i];
    }

}

function groupL1Nodes(center) {
    var i;
    // initialization; Assign each L1 node to group 0
    node.each(function(d) {
        d.L1Group = 0;
    });

    var groupNumber = 0;
    // loop through each node and label same group number to the nodes which are connected to each other (howsoever)
    for (i = 0; i < adjacencyList[center].size; i++) {
        curL1NodeIndex = adjacencyList[center].connections[i].node_index;
        if (node[0][curL1NodeIndex].__data__.L1Group == 0) {
            groupNumber++;
            //run BFS and assign group number; BFS here is not implemented via stack, so is not optimal. 
            //Although since we have only two level of nodes, I believe it should not be an issue. 
            //In case of performance issues later, it should be changed
			
            //if(node[0][curL1NodeIndex].__data__.direction == "outgoing") findConnectivityUsingBFS(curL1NodeIndex, groupNumber); //finds all the nodes connected to node i (level 1 or beyond) and assign them the same group number
            findConnectivityUsingBFS(curL1NodeIndex, groupNumber); //finds all the nodes connected to node i (level 1 or beyond) and assign them the same group number
        }
    }
    return groupNumber;
}

function findConnectivityUsingBFS(nodeIndex, groupNumber) {
    var i;
    node[0][nodeIndex].__data__.L1Group = groupNumber;
    for (i = 0; i < adjacencyList[nodeIndex].size; i++) {
        curNodeIndex = adjacencyList[nodeIndex].connections[i].node_index;
        if (node[0][curNodeIndex].__data__.level != 0 && node[0][curNodeIndex].__data__.L1Group == 0 && (node[0][curNodeIndex].__data__.type == node[0][nodeIndex].__data__.type) && (node[0][curNodeIndex].__data__.level != node[0][nodeIndex].__data__.level)) {
            //node[0][curNodeIndex].__data__.L1Group = groupNumber;
            // console.log("Labeling node: " + node[0][curNodeIndex].__data__.name + "  with parent Node: " + node[0][nodeIndex].__data__.name);
            findConnectivityUsingBFS(curNodeIndex, groupNumber);
        }
    }
}

// checks if the current co-ordinate is in the specified quadrant; if not bring
// it to the specified co-ordinate;
function enforceSectorConstraint() {
    // enforce placement in correct sector for every node
    // run through the adjacency list and enforce its quadrant placement;
    var L1GroupRotateByTheta = [];
    L1GroupRotateByTheta[0] = 0;
    for (i = 0; i < adjacencyList[center].size; i++) {
        curL1NodeIndex = adjacencyList[center].connections[i].node_index;
        groupNumber = node[0][curL1NodeIndex].__data__.L1Group;

        // get angle w.r.t center
        L1GroupRotateByTheta[groupNumber] = getRequiredRotationTheta(
            curL1NodeIndex, groupNumber);
    }

    for (i = 0; i < node.size(); i++) {
        if (i != center) {
            groupNumber = node[0][i].__data__.L1Group;
            var newCoordinates = rotateWRTCenter(i,
                L1GroupRotateByTheta[groupNumber]);
            node[0][i].__data__.x = newCoordinates.x;
            node[0][i].__data__.y = newCoordinates.y;
        }
    }

}

function rotateWRTCenter(nodeIndex, theta) {
    var xWRTCenter = node[0][nodeIndex].__data__.x - node[0][center].__data__.x;
    var yWRTCenter = node[0][nodeIndex].__data__.y - node[0][center].__data__.y;

    var new_xyWRTCenter = rotate(xWRTCenter, yWRTCenter, theta);

    x_standard = node[0][center].__data__.x + new_xyWRTCenter.x;
    y_standard = node[0][center].__data__.y + new_xyWRTCenter.y;

    return {
        x: x_standard,
        y: y_standard
    };
}

function getRequiredRotationTheta(nodeIndex, groupNumber) {
    var xWRTCenter = node[0][nodeIndex].__data__.x - node[0][center].__data__.x;
    var yWRTCenter = node[0][nodeIndex].__data__.y - node[0][center].__data__.y;

    var currentAngle = getAngle(xWRTCenter, yWRTCenter);

    theta1 = L1GroupSectorRange[groupNumber - 1];
    theta2 = L1GroupSectorRange[groupNumber];

    if (currentAngle < theta1)
        return theta1 - currentAngle;
    else if (currentAngle > theta2)
        return 2 * Math.PI - (currentAngle - theta2);
    else
        return 0;

}

function seedInitialPlacementofL1Nodes(center) {
    // find the number of groups
    var i;
    noOfGroups = 0;
    for (i = 0; i < adjacencyList[center].size; i++) {
        curL1NodeIndex = adjacencyList[center].connections[i].node_index;
        if (node[0][curL1NodeIndex].__data__.L1Group > noOfGroups) {
            noOfGroups = node[0][curL1NodeIndex].__data__.L1Group;
        }
    }

    // place each group elements together;
    nodeNumber = 0;
    theta = 2 * Math.PI / adjacencyList[center].size;
    
    for (i = 1; i <= noOfGroups; i++) {
        for (var j = 0; j < adjacencyList[center].size; j++) {
            curL1NodeIndex = adjacencyList[center].connections[j].node_index;
            groupNumber = node[0][curL1NodeIndex].__data__.L1Group;
            if (groupNumber == i) {
				//var edgeAngle = getAngle(node[0][curL1NodeIndex].__data__.x - node[0][center].__data__.x, node[0][curL1NodeIndex].__data__.y - node[0][center].__data__.y);
                //var r = ( (edgeAngle < Math.PI/8 || edgeAngle > 15*Math.PI/8) || (edgeAngle > 7*Math.PI/8 && edgeAngle < 9*Math.PI/8) ) ? (node[0][center].__data__.textWidth/2 + l1EdgeLength) : l1EdgeLength;
				node[0][curL1NodeIndex].__data__.x = node[0][center].__data__.x + (l1EdgeLength) * Math.cos(3 * Math.PI / 2 + nodeNumber * (theta + randDeviation));
                node[0][curL1NodeIndex].__data__.y = node[0][center].__data__.y + (l1EdgeLength) * Math.sin(3 * Math.PI / 2 + nodeNumber * (theta + randDeviation));
                //console.log(node[0][curL1NodeIndex].__data__.name + ", "+ nodeNumber + ", i:" + i + "groupNumeber: " + groupNumber);
                nodeNumber++;
                //d3.select(node[0][curL1NodeIndex]).classed("fixed", node[0][curL1NodeIndex].__data__.fixed = true);
            }
        }
    }
    //console.log(nodeNumber);
}

function seedInitialPlacementofL2Nodes(makeNodefixed) {
    node.each(function(d) {
        var avgX = 0,
            avgY = 0;
        if (d.level == 2 && d.parents.length > 1) {
            for (var j = 0; j < d.parents.length; j++) {
                avgX = avgX + (node[0][d.parents[j]].__data__.x - node[0][center].__data__.x);
                avgY = avgY + (node[0][d.parents[j]].__data__.y - node[0][center].__data__.y);
            }
            avgX = avgX / d.parents.length;
            avgY = avgX / d.parents.length;
            d.x = node[0][center].__data__.x + 8 * avgX;
            d.y = node[0][center].__data__.y + 8 * avgY;
            //console.log("Making: " + d.name + ", x:" + d.x + ", y:" + d.y);

            if (makeNodefixed) d.fixed = true;
            else d.fixed = false;
        }
    });
}

function changeNodeLinkDisplay() {
    node.selectAll("text").each(function(d) {
        d.textWidth = this.getBBox().width;
    })

    node.selectAll("rect").attr("class", "node").attr("width", function(d) {
        return (d.level == 0) ? centerNodeWidth*WindowScaleFactor : d.textWidth;
    }).attr("height", function(d) {
        return (d.level == 0) ? centerNodeWidth*WindowScaleFactor : l1NodeHeight*WindowScaleFactor;
    }).style("fill", function(d) {
        if (d.type == 'Process') {
            if (d.level == 0) return "#44cc99";
            else if (d.level == 1) return "#77ccaa";
            else return "#88ddcc";
        } else if (d.type == 'Entity') {
            if (d.level == 0) return "#77aaff";
            else if (d.level == 1) return "#99ccff";
            else return "#bbeeff";
        } else return defaultNodeColor;
    }).style("stroke", function(d) {
        if (d.level == 0) return "#bc0202";
    });

    node.selectAll("text")
        .attr("text-anchor", "middle").attr("text-align", "center").style(
            "visibility",
            function(d) {
                return d.level > 2 ? "hidden" : "visible";
            }).text(function(d) {
            return d.invisible ? "" : (d.name);
        });
	
	node.selectAll("rect").style("visibility", function(d) {
		return (d.invisible == true || d.level > 2) ? "hidden" : "visible";
	});
	
    link = svg.selectAll(".link")
        .attr("class", "link")
        .style("stroke-width", function(d) {
            if (d.target.level == 0 || d.source.level == 0) return 4;
            else if (d.target.level < 2 && d.source.level < 2) return 2;
            else return 1;
        }).style("stroke", function(d) {
            //return "#444444";
            return "#666666";
        }).style("visibility", function(d) {
            return (d.source.level > 2 || d.target.level > 2) ? "hidden" : "visible";
        }).style("fill", function(d) {
            return (d.target.level == 0 || d.source.level == 0) ? "black" : "#000";
        }).attr("marker-end", function(d) {
			return (d.target.level == 0 || d.source.level == 0) ? "url(#arrow)" : "url(#arrowLevel1)" 
		});

    link.selectAll("text").style("stroke", "none").style("-webkit-transform", "rotate(90deg)");

	// remove all the not required links (b/w first level incoming nodes and second level nodes only connected to first level incoming nodes
	link.selectAll("line").style("visibility", function(d) {
		return (d.invisible || d.source.level > 2 || d.target.level > 2) ? "hidden" : "visible";
	});

}

var adjacencyList = [];
// create adjacency list of the current graph
function createAdjacencyList() {
    for (i = 0; i < node.size(); i++) {
        node_name = node[0][i].__data__.name;
        adjacencyList[i] = {
            name: node_name,
            size: 0,
            connections: []
        };
    }

    // loop through all the edges and fill out adjacency list.
    for (i = 0; i < link.size(); i++) {
        source_index = link[0][i].__data__.source.index;
        target_index = link[0][i].__data__.target.index;

        // check if source target already there in adjacency list. This might happen in case of bidirectional links
        var j = 0,
            linkExists = 0;
        for (j = 0; j < adjacencyList[source_index].size; j++) {
            if (adjacencyList[source_index].connections[j].node_index == target_index) {
                linkExists = 1;
            }
        }
        if (linkExists == 1) {
            continue;
        }

        var size = adjacencyList[source_index].size;
        adjacencyList[source_index].connections[size] = {
            node_index: target_index,
            type: "out"
        };
        adjacencyList[source_index].size = adjacencyList[source_index].size + 1;

        var size = adjacencyList[target_index].size;
        adjacencyList[target_index].connections[size] = {
            node_index: source_index,
            type: "in"
        };
        adjacencyList[target_index].size = adjacencyList[target_index].size + 1;
    }
    //console.log(adjacencyList[center]);
}

function updateNodeLevels(center) {
    var i, j;
    var initialLevel = 25000;
    // initialize
    for (i = 0; i < node.size(); i++) {
        node[0][i].__data__.level = initialLevel;
        //console.log(node[0][i].__data__);
    }

    // set the centre node;
    node[0][center].__data__.level = 0;

    // set levels of nodes; Naive; Could implement BFS if data structure changes.
    var curLevel;
    for (curLevel = 0; curLevel < noOfLevelsToBeDisplayed; curLevel++) {
        for (j = 0; j < link.size(); j++) {
            if (link[0][j].__data__.source.level == curLevel && link[0][j].__data__.target.level == initialLevel) {
                link[0][j].__data__.target.level = curLevel + 1;
            } else if (link[0][j].__data__.target.level == curLevel && link[0][j].__data__.source.level == initialLevel) {
                link[0][j].__data__.source.level = curLevel + 1;
            }
        }
    }

    //print adjacencyList
    groupL1Nodes(center);
}

function updateLinksPositions(){
	// find the intersection of the Links with the Rectangle and make x1,y1 and x2,y2 so that it is from boundary of the source to that of the target. 
	link.attr("x1", function(d) {
		var width = Math.max(d.source.fisheye.z * l1NodeWidth*WindowScaleFactor, d.source.textWidth * 1.2);
		var height = d.source.fisheye.z * l1NodeHeight*WindowScaleFactor;
		var boxAngle = getAngle(width, height);
		var edgeAngle = getAngle(d.target.fisheye.x - d.source.fisheye.x,
			d.source.fisheye.y - d.target.fisheye.y);

		var x1, y1, x2, y2;
		var signFactorY = (edgeAngle < Math.PI) ? 1 : -1;
		var signFactorX = (edgeAngle < Math.PI / 2 || edgeAngle > 3 * Math.PI / 2) ? 1 : -1;
		if ((edgeAngle > boxAngle && edgeAngle < Math.PI - boxAngle) || (edgeAngle > Math.PI + boxAngle && edgeAngle < 2 * Math.PI - boxAngle)) {
			y1 = height / 2;
			x1 = Math.abs(y1 / Math.tan(edgeAngle));
		} else {
			x1 = width / 2;
			y1 = Math.abs(x1 * Math.tan(edgeAngle));
		}
		
		return d.source.fisheye.x + signFactorX * x1;

	}).attr("y1", function(d) {
		var width = Math.max(d.source.fisheye.z * l1NodeWidth*WindowScaleFactor, d.source.textWidth * 1.2);
		var height = d.source.fisheye.z * l1NodeHeight*WindowScaleFactor;
		var boxAngle = getAngle(width,
			height);
		var edgeAngle = getAngle(
			d.target.fisheye.x - d.source.fisheye.x,
			d.source.fisheye.y - d.target.fisheye.y);
		var x1, y1, x2, y2;
		var signFactorY = (edgeAngle < Math.PI) ? 1 : -1;
		var signFactorX = (edgeAngle < Math.PI / 2 || edgeAngle > 3 * Math.PI / 2) ? 1 : -1;
		if ((edgeAngle > boxAngle && edgeAngle < Math.PI - boxAngle) || (edgeAngle > Math.PI + boxAngle && edgeAngle < 2 * Math.PI - boxAngle)) {
			y1 = height / 2;
			x1 = Math.abs(y1 / Math.tan(edgeAngle));
		} else {
			x1 = width / 2;
			y1 = Math.abs(x1 * Math.tan(edgeAngle));
		}
		return d.source.fisheye.y - signFactorY * y1;

	}).attr("x2", function(d) {
		var width = Math.max(d.target.fisheye.z * l1NodeWidth*WindowScaleFactor, d.target.textWidth * 1.2);
		var height = d.target.fisheye.z * l1NodeHeight*WindowScaleFactor;
		var boxAngle = getAngle(width, height);
		var edgeAngle = getAngle(d.target.fisheye.x - d.source.fisheye.x, d.source.fisheye.y - d.target.fisheye.y);
		var x1, y1, x2, y2;
		var signFactorY = (edgeAngle < Math.PI) ? 1 : -1;
		var signFactorX = (edgeAngle < Math.PI / 2 || edgeAngle > 3 * Math.PI / 2) ? 1 : -1;
		if ((edgeAngle > boxAngle && edgeAngle < Math.PI - boxAngle) || (edgeAngle > Math.PI + boxAngle && edgeAngle < 2 * Math.PI - boxAngle)) {
			y1 = height / 2;
			x1 = Math.abs(y1 / Math.tan(edgeAngle));
		} else {
			x1 = width / 2;
			y1 = Math.abs(x1 * Math.tan(edgeAngle));
		}
		return d.target.fisheye.x - signFactorX * x1;

	}).attr("y2", function(d) {
		var width = Math.max(d.target.fisheye.z * l1NodeWidth*WindowScaleFactor, d.target.textWidth * 1.2);
		var height = d.target.fisheye.z * l1NodeHeight*WindowScaleFactor;
		var boxAngle = getAngle(width, height);
		var edgeAngle = getAngle(d.target.fisheye.x - d.source.fisheye.x, d.source.fisheye.y - d.target.fisheye.y);
		var x1, y1, x2, y2;
		var signFactorY = (edgeAngle < Math.PI) ? 1 : -1;
		var signFactorX = (edgeAngle < Math.PI / 2 || edgeAngle > 3 * Math.PI / 2) ? 1 : -1;
		if ((edgeAngle > boxAngle && edgeAngle < Math.PI - boxAngle) || (edgeAngle > Math.PI + boxAngle && edgeAngle < 2 * Math.PI - boxAngle)) {
			y1 = height / 2;
			x1 = Math.abs(y1 / Math.tan(edgeAngle));
		} else {
			x1 = width / 2;
			y1 = Math.abs(x1 * Math.tan(edgeAngle));
		}
		return d.target.fisheye.y + signFactorY * y1;

	});

	link.selectAll("text").text(function(d) {
			return d.invisible ? "" : (d.info);
		}).attr("x", function(d) {
			var edgeAngle = getAngle(d.target.fisheye.x - d.source.fisheye.x, d.source.fisheye.y - d.target.fisheye.y);
			var length = Math.sqrt( Math.pow(d.target.fisheye.x - d.source.fisheye.x,2) + Math.pow(d.source.fisheye.y - d.target.fisheye.y,2));
			return d.source.fisheye.x + 0.6*length*Math.cos(edgeAngle);
			//d.source.fisheye.x + d.target.fisheye.x / 2;
		}).attr("y", function(d) {
			var edgeAngle = getAngle(d.target.fisheye.x - d.source.fisheye.x, d.source.fisheye.y - d.target.fisheye.y);
			var length = Math.sqrt( Math.pow(d.target.fisheye.x - d.source.fisheye.x,2) + Math.pow(d.source.fisheye.y - d.target.fisheye.y,2));
			return d.source.fisheye.y - 0.6*length*Math.sin(edgeAngle);
			//return (d.source.fisheye.y + d.target.fisheye.y) / 2;
		}).attr("text-anchor", "start")
		.attr("fill", function(d) {
			return "black";
		});
}

$(window).resize(function() {
  performScaling();
});
