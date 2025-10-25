package com.lowlatency.graph.csr;

/**
 * Compressed Sparse Row (CSR) graph representation optimized for low-latency traversal.
 *
 * Memory layout:
 * - rowOffsets[i] points to the start of neighbors for node i
 * - rowOffsets[i+1] - rowOffsets[i] gives the number of neighbors
 * - columnIndices[rowOffsets[i]...rowOffsets[i+1]-1] contains the neighbor node IDs
 *
 * This layout is cache-friendly and enables fast iteration over neighbors.
 *
 * Time Complexity:
 * - Get neighbors: O(degree) with excellent cache locality
 * - Check edge existence: O(degree) - can be optimized with sorted neighbors
 *
 * Space Complexity: O(V + E) where V = vertices, E = edges
 */
public final class CSRGraph {

    // CSR structure
    private final int[] rowOffsets;      // Size: numNodes + 1
    private final int[] columnIndices;   // Size: numEdges

    // Graph metadata
    private final int numNodes;
    private final int numEdges;

    // Topological order for efficient evaluation
    private final int[] topologicalOrder;

    /**
     * Constructs a CSR graph. Use GraphBuilder to create instances.
     */
    CSRGraph(int[] rowOffsets, int[] columnIndices, int numNodes, int numEdges, int[] topologicalOrder) {
        this.rowOffsets = rowOffsets;
        this.columnIndices = columnIndices;
        this.numNodes = numNodes;
        this.numEdges = numEdges;
        this.topologicalOrder = topologicalOrder;
    }

    /**
     * Returns the number of nodes in the graph.
     */
    public int getNumNodes() {
        return numNodes;
    }

    /**
     * Returns the number of edges in the graph.
     */
    public int getNumEdges() {
        return numEdges;
    }

    /**
     * Returns the out-degree (number of outgoing edges) for a node.
     * Inlined for performance.
     */
    public int getOutDegree(int nodeId) {
        return rowOffsets[nodeId + 1] - rowOffsets[nodeId];
    }

    /**
     * Returns the start index in columnIndices for a node's neighbors.
     * Inlined for performance.
     */
    public int getNeighborStartIndex(int nodeId) {
        return rowOffsets[nodeId];
    }

    /**
     * Returns the end index in columnIndices for a node's neighbors.
     * Inlined for performance.
     */
    public int getNeighborEndIndex(int nodeId) {
        return rowOffsets[nodeId + 1];
    }

    /**
     * Returns the column indices array (neighbors).
     * Direct access for performance-critical code.
     */
    public int[] getColumnIndices() {
        return columnIndices;
    }

    /**
     * Returns the topological order for evaluation.
     */
    public int[] getTopologicalOrder() {
        return topologicalOrder;
    }

    /**
     * Iterates over neighbors of a node.
     * Zero-allocation design for hot path.
     */
    public void forEachNeighbor(int nodeId, NeighborConsumer consumer) {
        final int start = rowOffsets[nodeId];
        final int end = rowOffsets[nodeId + 1];
        final int[] neighbors = columnIndices;

        for (int i = start; i < end; i++) {
            consumer.accept(neighbors[i]);
        }
    }

    /**
     * Functional interface for neighbor iteration.
     */
    @FunctionalInterface
    public interface NeighborConsumer {
        void accept(int neighborId);
    }

    /**
     * Returns memory footprint in bytes.
     */
    public long getMemoryFootprint() {
        return (long) (rowOffsets.length + columnIndices.length + topologicalOrder.length) * Integer.BYTES
                + 16 // object header (approximate)
                + 3 * 8 // array references
                + 2 * 4; // int fields
    }

    @Override
    public String toString() {
        return String.format("CSRGraph[nodes=%d, edges=%d, memory=%d bytes]",
                numNodes, numEdges, getMemoryFootprint());
    }
}
