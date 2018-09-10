package ru.hse.spb

import java.util.*

/**
 * This class represents simple bidirected graph.
 */
class Graph {
    /**
     * Node state is
     * - ISOLATED if it has no adjacent nodes.
     * - HANGING if it has exactly one adjacent node.
     * - INNER if its degree is at least two.
     */
    enum class NodeState {
        ISOLATED, HANGING, INNER
    }

    /**
     * Utils and algorithms that can be applied to graphs.
     */
    companion object Utils {
        /**
         * Builds graph from the list of edges. The resulting graph will contain only the nodes which were mentioned
         * in the list.
         *
         * @return the graph that was built.
         */
        fun buildGraph(listOfEdges: List<Pair<Int, Int>>): Graph {
            return listOfEdges.fold(Graph()) { graph, edge -> graph.addEdge(edge.first, edge.second); graph}
        }

        /**
         * Makes breadth-first-serch throught the graph, starting from the given set of nodes.
         * NOTICE: all the nodes from the starting set must present in the graph.
         *
         * @return map of <nodes, distances (in edges)> from the starting set.
         */
        fun bfs(graph: Graph, startSet: Collection<Int>): Map<Int, Int> {
            val results: MutableMap<Int, Int> = mutableMapOf()
            val queue: Queue<Node> = ArrayDeque()
            startSet.forEach { queue.add(graph.nodes[it]); results[it] = 0 }
            while (queue.isNotEmpty()) {
                val node = queue.remove()
                val nodeResult = results[node.id]
                        ?: throw IllegalStateException("Graph doesn't contain node ${node.id}.")
                node.getAdjacentNodesSet()
                        .filter { !results.contains(it.id) }
                        .forEach { results[it.id] = nodeResult + 1; queue.add(it) }
            }
            return results
        }

        /**
         * Finds the core of the graph.
         *
         * @return set of nodes in the core.
         */
        fun findCore(graph: Graph): Set<Int> {
            val graphCopy = graph.deepCopy()
            val hangingNodes: Queue<Graph.Node> = ArrayDeque()
            graphCopy.getNodes()
                    .filter { it.getState() != NodeState.INNER }
                    .forEach { hangingNodes.add(it) }
            while (hangingNodes.isNotEmpty()) {
                val node = hangingNodes.remove()
                val adjacentNodes = node.getAdjacentNodesSet()
                graphCopy.removeNode(node.id)
                adjacentNodes
                        .filter { it.getState() != NodeState.INNER }
                        .forEach { hangingNodes.add(it) }
            }
            return graphCopy.nodes.keys.toSet()
        }
    }

    private val nodes: MutableMap<Int, Node> = mutableMapOf()

    /**
     * This class represents a simple graph node.
     */
    inner class Node(val id: Int) {
        private val adjacentNodes: MutableSet<Node> = mutableSetOf()
        private var state: NodeState = NodeState.ISOLATED

        fun getState(): NodeState {
            return state
        }

        /**
         * Adds edge from the current node to the node with the given id from the same graph.
         * If the node with the given id doesn't exist, this node will be created.
         * If this edge already existed, nothing will change.
         */
        fun addEdge(secondNodeId: Int) {
            val secondNode = nodes[secondNodeId] ?: addNode(secondNodeId)
            adjacentNodes.add(secondNode)
            secondNode.adjacentNodes.add(this)
            updateState()
            secondNode.updateState()
        }

        /**
         * Removes edge between current node and the node with given id from the current graph (if it existed).
         */
        fun removeEdge(secondNodeId: Int) {
            val secondNode = nodes[secondNodeId] ?: return
            adjacentNodes.remove(secondNode)
            secondNode.adjacentNodes.remove(this)
            updateState()
            secondNode.updateState()
            return
        }

        /**
         * @return set of adjacent nodes.
         */
        fun getAdjacentNodesSet(): Set<Node> {
            return adjacentNodes.toSet()
        }

        private fun updateState() {
            state = when (adjacentNodes.size) {
                0 -> NodeState.ISOLATED
                1 -> NodeState.HANGING
                else -> NodeState.INNER
            }
        }
    }

    /**
     * @return set of the nodes in the graph.
     */
    fun getNodes(): Set<Node> {
        return nodes.values.toSet()
    }

    /**
     * Adds new node with the given id if it doesnt't exist.
     * @return graph node with the given id.
     */
    fun addNode(id: Int): Node {
        val node = nodes[id] ?: Node(id)
        nodes[id] = node
        return node
    }

    /**
     * Removes node with the given id (if it existed).
     * @return true if the node existed and false otherwise.
     */
    fun removeNode(id: Int): Boolean {
        val node = nodes[id] ?: return false
        nodes.remove(id)
        for (adjacentNode in node.getAdjacentNodesSet()) {
            node.removeEdge(adjacentNode.id)
        }
        return true
    }

    /**
     * Adds edge between two nodes with the given ids to the graph.
     * If some node doesnt't exist in the graph, it will be created and added to the graph.
     */
    fun addEdge(id1: Int, id2: Int) {
        val node1 = nodes[id1] ?: addNode(id1)
        addNode(id2)
        node1.addEdge(id2)
    }

    /**
     * @return deep copy of the current graph.
     */
    fun deepCopy(): Graph {
        val newGraph = Graph()
        for (node in nodes.values) {
            for (adjacentNode in node.getAdjacentNodesSet()) {
                newGraph.addEdge(node.id, adjacentNode.id)
            }
        }
        return newGraph
    }
}

/**
 * Reads list of edges from the given scanner in the following format:
 *  - N: Int -- number of edges
 *  - N * "Int Int\n" -- edges
 */
fun readEdges(scanner: Scanner): List<Pair<Int, Int>> {
    val graphSize: Int = scanner.nextInt()
    val edges: MutableList<Pair<Int, Int>> = mutableListOf()
    for (i in 1..graphSize) {
        edges.add(Pair(scanner.nextInt(), scanner.nextInt()))
    }
    return edges
}

/**
 * Given a graph represented as list of edges finds distance from every node in graph to the graph core.
 */
fun solve(listOfEdges: List<Pair<Int, Int>>): List<Int> {
    val graph = Graph.buildGraph(listOfEdges)
    val core = Graph.findCore(graph)
    return Graph.bfs(graph, core).toSortedMap().values.toList()
}


fun main(args: Array<String>) {
    val edgesList = readEdges(Scanner(System.`in`))
    solve(edgesList).forEach { print("$it ") }
}