(function () {
    'use strict';

    angular
        .module('app')
        .controller('GraphController', GraphController);

  GraphController.$inject = ['NodeInfoService', 'CacheInfoService', '$templateCache'];

    function GraphController(NodeInfoService, CacheInfoService, $templateCache) {
        var vm = this;
        var refreshDelay = 5000;

        vm.update = () => {
            if (vm.selectedCache) {
                console.log("RESPONSE", vm.selectedCache);
            }
        }

        // Getting cache name list
        CacheInfoService.get()
            .then(
                function (response) {
                    vm.cacheInfo = response;
                    vm.selectedCache = vm.cacheInfo[0];
                    console.log("collectCacheInfo");
                    Promise.all([collectNodeInfo()]).then(buildGraph);
                },
                function (errResponse) {
                    vm.cacheInfo = ["Error for retrieving the cache names"]
                }
            );

        function collectNodeInfo() {
          return new Promise((resolve, reject) => {
            NodeInfoService.get(vm.selectedCache.name, "clustered"/*vm.selectedCache.container*/)
                .then(
                    function (response) {
                        vm.nodeInfo = response;
                        resolve(response);
                        console.log("collectNodeInfo")
                    },
                    function (errResponse) {
                        reject("ERROR");
                    }
                );
          })
        };

        function buildGraph(){
            var margin = {
                top: 100
                , right: 100
                , bottom: 100
                , left: 100
            };

            var width = 960,
                height = 500;

            var svg = d3.select('graph')
                .append('svg')
                .attr('height', height)
                .attr('width', width)
                .style('background-color', 'AliceBlue');

            var padding = 1.5, // separation between same-color circles
                clusterPadding = 20, // separation between different-color circles
                maxRadius = 20,
                entryRadius = 5,
                nodeRadius = 20,
                imageRadius = nodeRadius * 1.5;

            var color = d3.scaleOrdinal(d3.schemeCategory10)
                            .domain(d3.range(20));

            var nodes,
                ispnNodes = {};

            updateNodes();

        // Define the div for the tooltip
        let div = d3.select("graph").append("div")
            .attr("class", "tooltip")
            .style("opacity", 0);

        var forceCollide = d3.forceCollide()
            .radius(function(d) { return d.r + 1.5; })
            .iterations(1);

        // create the clustering/collision force simulation
        let simulation = d3.forceSimulation()
            .force('center', d3.forceCenter(width/2, height/2))
//            .velocityDecay(0.2)
//            .force("x", d3.forceX().strength(.005))
//            .force("y", d3.forceY().strength(.005))
//            .force("gravity", d3.forceManyBody(30))
            .force("collide", collide)
//            .force('collide', d3.forceCollide(d => d.r + padding)
//                .strength(0.7))
            .force("cluster", clustering)
//            .force('cluster', forceCluster)
            .on("tick", ticked)
            .nodes(nodes);

        var circle = svg.selectAll('g.circle')
            .data(nodes, function(d) { return d.id;})
            .enter()
            .append("g")
                .attr("class", "circle")
                .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
            .call(d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended))
            // add tooltips to each circle
            .on("mouseover", function(d) {
                div.transition()
                    .duration(200)
                    .style("opacity", .9);
                div .html( "Node " + d.node + "<br/>")
                    .style("left", (d3.event.pageX) + "px")
                    .style("top", (d3.event.pageY - 28) + "px");
                })
            .on("mouseout", function(d) {
                div.transition()
                    .duration(500)
                    .style("opacity", 0);
            });

        // Append circle
        circle.append('circle')
            .attr('r', (d) => d.r)
            .attr('fill', (d) => color(d.node));

        //Append text
        circle.append("text")
            .attr("class", "nodename")
            .attr("text-anchor", "middle")
            .style("font-family", "Comic Sans MS, cursive, sans-serif")
            .style("font-size", "12px")
            .attr("y", (d) => d.r + 14)
            .text(function(d) {return (d.type === "NODE") ? d.id : ""});

        circle.append("text")
            .attr("class", "numberEntries")
            .attr("text-anchor", "middle")
            .style("font-family", "Comic Sans MS, cursive, sans-serif")
            .style("font-size", "10px")
            .attr("y", (d) => d.r + 26)
            .text(function(d) {return (d.type === "NODE") ? "[" + d.numberEntries + "]" : ""});

        // Append images
        var images = circle.append("image")
            .attr("xlink:href",  (d) => { return (d.type === "NODE") ? "images/infinispan.png": ""})
            .attr("x", (d) => -(imageRadius / 2))
            .attr("y", (d) => -(imageRadius / 2) )
            .attr("height", (d) => imageRadius)
            .attr("width", (d) => imageRadius);

        d3.interval(function() {
            Promise.all([collectNodeInfo()]).then(redraw);
        }, refreshDelay, d3.now());

        function updateNodes() {
            var oldNodes = nodes;

            nodes = vm.nodeInfo.map((d, i) => {
                var nodeId = d.name;

                var mainEntry = _.find(oldNodes, function(n){ return n.id === nodeId; });

                if (mainEntry === undefined) {
                     mainEntry = {
                         id : nodeId,
                         type : "NODE",
                         node : d.name,
                         numberEntries : d.numberEntries,
                         r : nodeRadius,
                         x :  Math.cos(i / vm.nodeInfo.length * 2 * Math.PI) * 150 + width / 2 + Math.random(),
                         y : Math.sin(i / vm.nodeInfo.length * 2 * Math.PI) * 150 + height / 2 + Math.random()
                    };
                } else {
                    // Update just the number of entries
                    mainEntry.numberEntries = d.numberEntries;
                }

                // add cluster id and radius to array
                var entries = d3.range(d.numberEntries).map((number) => {
                    var entryId = d.name + "_" + number;

                    var entry = _.find(oldNodes, function(n){ return n.id === entryId; });

                    if (entry === undefined) {
                        entry = {
                           id : entryId,
                           type   : "ENTRY",
                           node     : d.name,
                           r   : entryRadius,
                           x :  Math.cos(i / vm.nodeInfo.length * 2 * Math.PI) * 150 + width / 2 + Math.random(),
                           y  : Math.sin(i / vm.nodeInfo.length * 2 * Math.PI) * 150 + height / 2 + Math.random()
                        };
                    }

                    return entry;
                });

                ispnNodes[mainEntry.id] = mainEntry;
                return [mainEntry].concat(entries);
              }).reduce((acc, val) => acc.concat(val), []);
        }

        function redraw() {
            var exitTransition = d3.transition()
                  .duration(2000);

            var enterTransition = d3.transition()
                  .duration(750);

            var updateTransition = d3.transition()
                  .duration(2000);

            updateNodes();

            // Apply the general update pattern to the nodes.
            circle = circle.data(nodes, function(d) { return d.id;});
            circle.exit()
                .selectAll("text")
                    .transition(exitTransition)
                        .attr("y", (d) => d.y + 100)
                        .attr("fill-opacity", 1e-6)
                .remove();

            circle.exit().selectAll("image")
                        .transition(exitTransition)
                            .attr("y", (d) => d.y + 100)
                            .attr("height", 1e-6)
                            .attr("width", 1e-6)
                    .remove();

            circle.exit()
                .selectAll("circle")
                    .transition(exitTransition)
                        .attr("cy", (d) => d.y + 100)
                        .attr("fill", 'red')
                        .attr("r", 1e-6)
                .remove();

            circle.exit().transition(exitTransition).remove();

            // Enter any new circle.
            var circleEnter = circle.enter().append("g")
                    .attr("class", "circle")
                    .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });

            circleEnter.append('circle')
                    .attr('r', 20)
                .transition(enterTransition)
                    .attr('r', (d) => d.r)
                    .attr('fill', (d) => color(d.node));

            circleEnter.append("text")
                        .attr("class", "nodename")
                        .attr("text-anchor", "middle")
                        .style("font-family", "Comic Sans MS, cursive, sans-serif")
                        .style("font-size", "12px")
                        .attr("y", (d) => d.r + 14)
                        .text(function(d) {return (d.type === "NODE") ? d.id : ""});

            circleEnter.append("text")
                        .attr("class", "numberEntries")
                        .attr("text-anchor", "middle")
                        .style("font-family", "Comic Sans MS, cursive, sans-serif")
                        .style("font-size", "10px")
                        .attr("y", (d) => d.r + 26)
                        .text(function(d) {return (d.type === "NODE") ? "[" + d.numberEntries + "]" : ""});

            // Append images
            var images = circleEnter.append("image")
                .attr("xlink:href",  (d) => { return (d.type === "NODE") ? "images/infinispan.png": ""})
                .attr("x", (d) => -(imageRadius / 2))
                .attr("y", (d) => -(imageRadius / 2) )
                .attr("height", (d) => imageRadius)
                .attr("width", (d) => imageRadius);

            // Update existing nodes
            circle.selectAll("text.numberEntries")
                .transition(updateTransition)
                .text(function(d) {return (d.type === "NODE") ? "[" + d.numberEntries + "]" : ""});

            // Enter and update
            circle = circleEnter.merge(circle);

          // Update and restart the simulation.
          simulation.nodes(nodes);

          simulation.alphaTarget(0.3).restart();
        }


        function ticked() {
            circle
                .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
//                  .attr("cx", function(d) { return d.x; })
//                  .attr("cy", function(d) { return d.y; });
        }

        // Drag functions used for interactivity
        function dragstarted(d) {
        if (!d3.event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
        }

        function dragged(d) {
        d.fx = d3.event.x;
        d.fy = d3.event.y;
        }

        function dragended(d) {
        if (!d3.event.active) simulation.alphaTarget(0);
        d.fx = null;
        d.fy = null;
        }

        // These are implementations of the custom forces.
        function clustering(alpha) {
          nodes.forEach(function(d) {
            var ispnNode = ispnNodes[d.node];
            if (ispnNode === d) return;
            var x = d.x - ispnNode.x,
                y = d.y - ispnNode.y,
                l = Math.sqrt(x * x + y * y),
                r = d.r + (ispnNode.r * 3);
            if (l !== r) {
              l = (l - r) / l * alpha;
              d.x -= x *= l;
              d.y -= y *= l;
              ispnNode.x += x;
              ispnNode.y += y;
            }
          });
        }

        function forceCluster(alpha) {
          var k = alpha * 1
          nodes.forEach(function(d) {
              var ispnNode = ispnNodes[d.node];
              var k = alpha * 1
              if (ispnNode === d) return;

              d.vx -= (d.x - ispnNode.x) * k;
              d.vy -= (d.y - ispnNode.y) * k;
            });
        }

        function collide(alpha) {
            var quadtree = d3.quadtree()
                .x((d) => d.x)
                .y((d) => d.y)
                .addAll(nodes);

            nodes.forEach(function(d) {
              var r = d.r + maxRadius + Math.max(padding, clusterPadding),
                  nx1 = d.x - r,
                  nx2 = d.x + r,
                  ny1 = d.y - r,
                  ny2 = d.y + r;
              quadtree.visit(function(quad, x1, y1, x2, y2) {

                if (quad.data && (quad.data !== d)) {
                  var x = d.x - quad.data.x,
                      y = d.y - quad.data.y,
                      l = Math.sqrt(x * x + y * y),
                      r = d.r + quad.data.r + (d.node === quad.data.node ? padding : clusterPadding);
                  if (l < r) {
                    l = (l - r) / l * alpha;
                    d.x -= x *= l;
                    d.y -= y *= l;
                    quad.data.x += x;
                    quad.data.y += y;
                  }
                }
                return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
              });
            });
        }
    } // BUILDGRAPH
  };
})();
