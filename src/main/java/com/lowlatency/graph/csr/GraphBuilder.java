package com.lowlatency.graph.csr;

import java.util.*;

/**
 * Builder for constructing CSR graphs with topological ordering.
 *
 * This class:
 * 1. Collects edges in adjacency list format
 * 2. Performs topological sort (Kahn's algorithm)
 * 3. Converts to CSR format for optimal traversal
 *
 * Thread-safety: Not thread-safe. Build graphs on a single thread.
 */
public final class GraphBuilder {

    private final int numNodes;
    private final List<List<Integer>> adjList;
    private final int[] inDegree;

    public GraphBuilder(int numNodes) {
        this.numNodes = numNodes;
        this.adjList = new ArrayList<>(numNodes);
        this.inDegree = new int[numNodes];

        for (int i = 0; i < numNodes; i++) {
            adjList.add(new ArrayList<>());
        }
    }

    /**
     * Adds a directed edge from -> to.
     *
     * @param from source node ID
     * @param to destination node ID
     */
    public GraphBuilder addEdge(int from, int to) {
        if (from < 0 || from >= numNodes) {
            throw new IllegalArgumentException("Invalid source node: " + from);
        }
        if (to < 0 || to >= numNodes) {
            throw new IllegalArgumentException("Invalid destination node: " + to);
        }

        adjList.get(from).add(to);
        inDegree[to]++;
        return this;
    }

    /**
     * Builds the CSR graph with topological ordering.
     *
     * @return CSRGraph optimized for evaluation
     * @throws IllegalStateException if graph contains a cycle
     */
    public CSRGraph build() {
        // Perform topological sort
        int[] topoOrder = topologicalSort();

        // Count total edges
        int numEdges = 0;
        for (List<Integer> neighbors : adjList) {
            numEdges += neighbors.size();
        }

        // Build CSR structure
        int[] rowOffsets = new int[numNodes + 1];
        int[] columnIndices = new int[numEdges];

        int offset = 0;
        for (int i = 0; i < numNodes; i++) {
            rowOffsets[i] = offset;
            List<Integer> neighbors = adjList.get(i);

            // Copy neighbors to column indices
            for (int neighbor : neighbors) {
                columnIndices[offset++] = neighbor;
            }
        }
        rowOffsets[numNodes] = offset;

        return new CSRGraph(rowOffsets, columnIndices, numNodes, numEdges, topoOrder);
    }

    /**
     * Performs topological sort using Kahn's algorithm.
     * O(V + E) time complexity, O(V) space.
     *
     * @return array of node IDs in topological order
     * @throws IllegalStateException if cycle detected
     */
    private int[] topologicalSort() {
        int[] inDegreeCopy = Arrays.copyOf(inDegree, numNodes);
        int[] result = new int[numNodes];
        int resultIndex = 0;

        // Queue for nodes with in-degree 0
        Deque<Integer> queue = new ArrayDeque<>();

        // Add all nodes with in-degree 0
        for (int i = 0; i < numNodes; i++) {
            if (inDegreeCopy[i] == 0) {
                queue.offer(i);
            }
        }

        while (!queue.isEmpty()) {
            int node = queue.poll();
            result[resultIndex++] = node;

            // Reduce in-degree for neighbors
            for (int neighbor : adjList.get(node)) {
                inDegreeCopy[neighbor]--;
                if (inDegreeCopy[neighbor] == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // Check for cycle
        if (resultIndex != numNodes) {
            throw new IllegalStateException(
                    "Graph contains a cycle. Cannot create topological ordering. " +
                            "Processed " + resultIndex + " out of " + numNodes + " nodes."
            );
        }

        return result;
    }

    /**
     * Returns the number of nodes in the graph being built.
     */
    public int getNumNodes() {
        return numNodes;
    }

    /**
     * Returns the current number of edges.
     */
    public int getNumEdges() {
        int count = 0;
        for (List<Integer> neighbors : adjList) {
            count += neighbors.size();
        }
        return count;
    }
}
