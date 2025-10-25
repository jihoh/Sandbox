package com.lowlatency.graph.evaluator;

import com.lowlatency.graph.csr.CSRGraph;
import com.lowlatency.graph.node.Node;

/**
 * High-performance graph evaluator with topological traversal.
 *
 * Design principles:
 * 1. Zero allocation during evaluation - critical for sub-ms latency
 * 2. Topological order ensures dependencies computed before dependents
 * 3. Sequential evaluation for CPU cache efficiency
 * 4. Single-threaded for predictable latency
 *
 * Performance characteristics:
 * - O(V) time complexity where V = number of nodes
 * - Cache-friendly sequential access
 * - No virtual call overhead in hot path
 * - JIT-friendly with monomorphic call sites
 *
 * Thread safety: Not thread-safe. Use one evaluator per thread.
 */
public final class GraphEvaluator {

    private final CSRGraph graph;
    private final Node<?>[] nodes;
    private final int[] topologicalOrder;

    /**
     * Creates a graph evaluator.
     *
     * @param graph the CSR graph structure
     * @param nodes array of compute nodes (indexed by node ID)
     */
    public GraphEvaluator(CSRGraph graph, Node<?>[] nodes) {
        if (graph.getNumNodes() != nodes.length) {
            throw new IllegalArgumentException(
                    String.format("Graph has %d nodes but %d node objects provided",
                            graph.getNumNodes(), nodes.length)
            );
        }

        this.graph = graph;
        this.nodes = nodes;
        this.topologicalOrder = graph.getTopologicalOrder();

        // Validate node IDs match array indices
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].getNodeId() != i) {
                throw new IllegalArgumentException(
                        String.format("Node at index %d has ID %d", i, nodes[i].getNodeId())
                );
            }
        }
    }

    /**
     * Evaluates the entire graph in topological order.
     * Zero allocation hot path.
     *
     * Time complexity: O(V) where V = number of nodes
     */
    public void evaluate() {
        final int[] order = topologicalOrder;
        final Node<?>[] nodeArray = nodes;
        final int length = order.length;

        // Sequential evaluation in topological order
        // This loop is the hot path - must be zero allocation
        for (int i = 0; i < length; i++) {
            final int nodeId = order[i];
            nodeArray[nodeId].compute();
        }
    }

    /**
     * Evaluates only dirty nodes (incremental evaluation).
     * For advanced use cases with selective recomputation.
     */
    public void evaluateIncremental() {
        final int[] order = topologicalOrder;
        final Node<?>[] nodeArray = nodes;
        final int length = order.length;

        for (int i = 0; i < length; i++) {
            final int nodeId = order[i];
            final Node<?> node = nodeArray[nodeId];

            if (node.isDirty()) {
                node.compute();
            }
        }
    }

    /**
     * Gets a node by ID.
     */
    public Node<?> getNode(int nodeId) {
        return nodes[nodeId];
    }

    /**
     * Gets a node by ID with type casting.
     */
    @SuppressWarnings("unchecked")
    public <T> Node<T> getNode(int nodeId, Class<T> type) {
        return (Node<T>) nodes[nodeId];
    }

    /**
     * Returns the number of nodes in the graph.
     */
    public int getNumNodes() {
        return nodes.length;
    }

    /**
     * Returns the underlying CSR graph.
     */
    public CSRGraph getGraph() {
        return graph;
    }

    /**
     * Resets all nodes. Call before starting a new evaluation cycle.
     */
    public void reset() {
        for (Node<?> node : nodes) {
            node.reset();
        }
    }
}
