package com.lowlatency.graph.hybrid.core;

import java.util.Collections;
import java.util.Map;

/**
 * High-performance compiled graph using Struct-of-Arrays (SOA) design.
 *
 * Architecture:
 * - Primitive arrays for cache-friendly data layout (from SingleThreadedEngine)
 * - CSR (Compressed Sparse Row) for graph topology
 * - Modular design with clear separation of concerns (from modular approach)
 * - Support for both primitive and object values
 *
 * Memory layout optimizations:
 * - All arrays are final and immutable after construction
 * - Primitive doubles packed tightly for cache efficiency
 * - CSR format minimizes pointer chasing
 *
 * Thread safety: Single-writer principle - only one thread modifies values array
 */
public final class HybridCompiledGraph {

    // ========== Core Metadata ==========
    /** Total number of nodes (inputs + computation nodes) */
    public final int nodeCount;

    /** Number of input nodes */
    public final int inputNodeCount;

    /** Number of computation nodes */
    public final int computationNodeCount;

    // ========== Node Naming & Lookup ==========
    /** Maps node ID to original string name (for debugging) */
    public final String[] nodeNames;

    /** Fast lookup: name -> node ID (O(1)) */
    public final Map<String, Integer> nameToId;

    /** Fast lookup: name -> node ID (inputs only) */
    public final Map<String, Integer> inputNodeIds;

    // ========== Values Storage ==========
    /** Current computed values for all nodes (THE hot path array) */
    public final double[] values;

    /** Optional: object values for non-numeric types (null if not used) */
    public final Object[] objectValues;

    // ========== Computation Jump Table ==========
    /** Direct function pointers to Calculator implementations */
    public final Calculator[] calculators;

    // ========== Graph Topology (CSR Format) ==========
    /** Number of parents for each node */
    public final int[] parentCounts;

    /** Flat array of all parent IDs (indexed via parentIndexPointers) */
    public final int[] parentValues;

    /** Pointers into parentValues array: [start, end) for each node */
    public final int[] parentIndexPointers;

    /** Flat array of all child IDs (indexed via childIndexPointers) */
    public final int[] childValues;

    /** Pointers into childValues array: [start, end) for each node */
    public final int[] childIndexPointers;

    // ========== Topological Order ==========
    /** Pre-computed evaluation order (computation nodes only) */
    public final int[] computationOrder;

    /** Full topological order (all nodes including inputs) */
    public final int[] fullTopologicalOrder;

    // ========== Metadata ==========
    /** Total number of edges in the graph */
    public final int edgeCount;

    /** Graph creation timestamp (nanos) */
    public final long creationTimestamp;

    /**
     * Constructor for creating compiled graphs.
     * Normally only called by GraphCompiler to ensure proper validation.
     * Made public to allow access from compiler package.
     */
    public HybridCompiledGraph(
            int nodeCount,
            int inputNodeCount,
            int computationNodeCount,
            String[] nodeNames,
            Map<String, Integer> nameToId,
            Map<String, Integer> inputNodeIds,
            double[] values,
            Object[] objectValues,
            Calculator[] calculators,
            int[] parentCounts,
            int[] parentValues,
            int[] parentIndexPointers,
            int[] childValues,
            int[] childIndexPointers,
            int[] computationOrder,
            int[] fullTopologicalOrder,
            int edgeCount) {

        this.nodeCount = nodeCount;
        this.inputNodeCount = inputNodeCount;
        this.computationNodeCount = computationNodeCount;
        this.nodeNames = nodeNames;
        this.nameToId = Collections.unmodifiableMap(nameToId);
        this.inputNodeIds = Collections.unmodifiableMap(inputNodeIds);
        this.values = values;
        this.objectValues = objectValues;
        this.calculators = calculators;
        this.parentCounts = parentCounts;
        this.parentValues = parentValues;
        this.parentIndexPointers = parentIndexPointers;
        this.childValues = childValues;
        this.childIndexPointers = childIndexPointers;
        this.computationOrder = computationOrder;
        this.fullTopologicalOrder = fullTopologicalOrder;
        this.edgeCount = edgeCount;
        this.creationTimestamp = System.nanoTime();
    }

    // ========== High-Level Query API ==========

    /**
     * Returns the result of the graph computation.
     * Defined as the value of the last node in topological order.
     */
    public double getResult() {
        if (computationOrder.length == 0) {
            return Double.NaN;
        }
        return values[computationOrder[computationOrder.length - 1]];
    }

    /**
     * Returns the current value of a node by name.
     * O(1) lookup via nameToId map.
     *
     * @throws IllegalArgumentException if node name is unknown
     */
    public double getValue(String nodeName) {
        Integer id = nameToId.get(nodeName);
        if (id == null) {
            throw new IllegalArgumentException("Unknown node: " + nodeName);
        }
        return values[id];
    }

    /**
     * Returns the current value of a node by ID.
     * Direct array access - fastest possible.
     */
    public double getValue(int nodeId) {
        return values[nodeId];
    }

    /**
     * Sets the value of an input node by name.
     * O(1) lookup via inputNodeIds map.
     *
     * @throws IllegalArgumentException if not an input node
     */
    public void setValue(String nodeName, double value) {
        Integer id = inputNodeIds.get(nodeName);
        if (id == null) {
            throw new IllegalArgumentException("Unknown or non-input node: " + nodeName);
        }
        values[id] = value;
    }

    /**
     * Sets the value of an input node by ID.
     * Direct array access - fastest possible.
     * Note: No validation that this is actually an input node!
     */
    public void setValue(int nodeId, double value) {
        values[nodeId] = value;
    }

    // ========== Graph Traversal Helpers ==========

    /**
     * Returns the parent node IDs for a given node.
     * Zero-allocation: returns indices into parentValues array.
     *
     * @return [startIndex, endIndex) range in parentValues
     */
    public int getParentStartIndex(int nodeId) {
        return parentIndexPointers[nodeId];
    }

    public int getParentEndIndex(int nodeId) {
        return parentIndexPointers[nodeId + 1];
    }

    /**
     * Returns the child node IDs for a given node.
     * Zero-allocation: returns indices into childValues array.
     *
     * @return [startIndex, endIndex) range in childValues
     */
    public int getChildStartIndex(int nodeId) {
        return childIndexPointers[nodeId];
    }

    public int getChildEndIndex(int nodeId) {
        return childIndexPointers[nodeId + 1];
    }

    /**
     * Iterates over all parents of a node.
     * Zero-allocation hot path.
     */
    public void forEachParent(int nodeId, IntConsumer consumer) {
        int start = parentIndexPointers[nodeId];
        int end = parentIndexPointers[nodeId + 1];
        for (int i = start; i < end; i++) {
            consumer.accept(parentValues[i]);
        }
    }

    /**
     * Iterates over all children of a node.
     * Zero-allocation hot path.
     */
    public void forEachChild(int nodeId, IntConsumer consumer) {
        int start = childIndexPointers[nodeId];
        int end = childIndexPointers[nodeId + 1];
        for (int i = start; i < end; i++) {
            consumer.accept(childValues[i]);
        }
    }

    @FunctionalInterface
    public interface IntConsumer {
        void accept(int value);
    }

    // ========== Statistics & Debugging ==========

    /**
     * Calculates the memory footprint of this graph in bytes.
     */
    public long getMemoryFootprint() {
        long bytes = 0;

        // Primitive arrays
        bytes += (long) values.length * Double.BYTES;
        bytes += (long) parentCounts.length * Integer.BYTES;
        bytes += (long) parentValues.length * Integer.BYTES;
        bytes += (long) parentIndexPointers.length * Integer.BYTES;
        bytes += (long) childValues.length * Integer.BYTES;
        bytes += (long) childIndexPointers.length * Integer.BYTES;
        bytes += (long) computationOrder.length * Integer.BYTES;
        bytes += (long) fullTopologicalOrder.length * Integer.BYTES;

        // Object arrays (references only, not deep size)
        bytes += (long) nodeNames.length * 8; // reference size
        bytes += (long) calculators.length * 8;
        if (objectValues != null) {
            bytes += (long) objectValues.length * 8;
        }

        // Maps (approximate)
        bytes += nameToId.size() * 40L; // rough estimate per entry
        bytes += inputNodeIds.size() * 40L;

        // Object overhead
        bytes += 128; // approximate object header + fields

        return bytes;
    }

    /**
     * Returns a human-readable summary of the graph.
     */
    @Override
    public String toString() {
        return String.format(
            "HybridCompiledGraph[nodes=%d (inputs=%d, compute=%d), edges=%d, memory=%,d bytes]",
            nodeCount, inputNodeCount, computationNodeCount, edgeCount, getMemoryFootprint()
        );
    }

    /**
     * Prints detailed graph statistics.
     */
    public void printStats() {
        System.out.println("=== Graph Statistics ===");
        System.out.printf("Total nodes:        %d%n", nodeCount);
        System.out.printf("  Input nodes:      %d%n", inputNodeCount);
        System.out.printf("  Compute nodes:    %d%n", computationNodeCount);
        System.out.printf("Total edges:        %d%n", edgeCount);
        System.out.printf("Avg parents/node:   %.2f%n", (double) edgeCount / nodeCount);
        System.out.printf("Memory footprint:   %,d bytes (%.2f KB)%n",
            getMemoryFootprint(), getMemoryFootprint() / 1024.0);
        System.out.printf("Values array size:  %d doubles (%.2f KB)%n",
            values.length, values.length * 8.0 / 1024.0);
    }
}
