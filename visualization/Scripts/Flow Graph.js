//"use strict";

/* Constants */
var DATA_FILE_PATH = "Flow Graph Data.json";
var CATEGORIES = ["SOURCES", "SINKS"];
var SOURCES = ["ANY", "IMEI", "PHONE_NUMBER", "CAMERA", "MICROPHONE",
		"LOCATION", "FILESYSTEM", "NONE"];
var SINKS = ["NONE", "NETWORK", "TEXTMESSAGE", "EMAIL", "FILESYSTEM", "ANY"];
var PADDING = 25;

/* Global Variables */
var svg;

/*
 * Initializes the flow graph.
 */
function initialize() {
	svg = d3.select("#viewport")
		.append("svg")
		.attr("xmlns", "http://www.w3.org/2000/svg")
		.style("height", window.innerHeight - d3.select("header").node().clientHeight - PADDING * 2)
		.style("width", window.innerWidth - PADDING * 2);
		
	var width = svg.node().clientWidth;
	var height = svg.node().clientHeight;

	var x = d3.scale.ordinal().domain(CATEGORIES).rangePoints([250, width]),
		y = {};

	var axis = d3.svg.axis().orient("left"),
		foreground;
		
		// Create a scale and brush for each category
	y["SOURCES"] = d3.scale.ordinal().domain(SOURCES).rangePoints([20, height - 20]);
	y["SINKS"] = d3.scale.ordinal().domain(SINKS).rangePoints([20, height - 20]);

	d3.json(DATA_FILE_PATH, function(json) {
		var flows = [];
		var filenames = {};
		// create a Flow object for each Source-Sink pair in the object
		json.forEach(function (obj) {
			var sources = obj.sources;
			var newSources = [];
			if (sources !== "NONE") {
				sources = sources.slice(1, -1).split(",");
				for (var i = 0; i < sources.length; i++) {
					var source = sources[i];
					var lastDotIndex = source.lastIndexOf(".");
					if (lastDotIndex >= 0) {
						newSources.push(source.slice(lastDotIndex + 1));
					}
				}
			} else {
				newSources.push("NONE");
			}
			
			var sinks = obj.sinks;
			var newSinks = [];
			if (sinks !== "NONE") {
				sinks = sinks.slice(1, -1).split(",");
				for (var i = 0; i < sinks.length; i++) {
					var sink = sinks[i];
					var lastDotIndex = sink.lastIndexOf(".");
					if (lastDotIndex >= 0) {
						newSinks.push(sink.slice(lastDotIndex + 1));
					}
				}
			} else {
				newSinks.push("NONE");
			}
			
			newSources.forEach(function (newSource) {
				newSinks.forEach(function (newSink) {
					flows.push(new Flow(newSource, newSink, obj.filename));
				});
			});
			
			filenames[obj.filename] = true;
		});

	
		// Add foreground lines.
		foreground = svg.append("g")
			.attr("class", "foreground");
		var lineContainer = foreground
			.selectAll("g")
			.data(flows)
			.enter()
			.append("g");
		lineContainer
			.append("path")
			.attr("d", getPath)
			.style("stroke", function(d) {
				return d3.scale.category20().domain(d3.keys(filenames))(d.filename);
			})
			.on("mouseover", highlight)
			.on("mouseout", unhighlight)
			.append("title")
			.text( function(d) { return d.filename;});

 var g = svg.selectAll(".category")
      .data(CATEGORIES)
    .enter().append("g")
      .attr("class", "category")
      .attr("transform", function(d) { return "translate(" + x(d) + ")"; });


	 // Add an axis and title.
	  g.append("g")
	      .attr("class", "axis")
	      .each(function(d) { d3.select(this).call(axis.scale(
	      	y[d])); })
	    .append("text");
	});
	
	// Returns the path for a given data point.
	function getPath(d) {
		return d3.svg.line()(CATEGORIES.map(function(p) {

			return [x(p), y[p](d[p === "SOURCES" ? "source" : "sink"])];
		}));
	}
}

function Flow(source, sink, filename) {
	this.source = source;
	this.sink = sink;
	this.filename = filename;
}

function highlight(d) {
	d3.select(this).attr("class", "highlighted");
}

function unhighlight(d) {
	d3.select(this).attr("class", "");
}

window.onload = initialize;