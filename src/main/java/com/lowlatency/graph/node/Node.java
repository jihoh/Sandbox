package com.lowlatency.graph.node;

/**
 * Base interface for computation nodes in the graph.
 *
 * Design principles:
 * 1. Zero allocation in compute() - critical for sub-millisecond latency
 * 2. Pull-based computation - nodes pull data from dependencies
 * 3. State is mutable - avoid object creation
 * 4. Type safety via generics
 *
 * Performance considerations:
 * - compute() should be final or monomorphic for JIT optimization
 * - Avoid virtual calls in hot paths
 * - Use primitive types when possible
 * - Minimize memory footprint
 */
public interface Node<T> {

    /**
     * Computes the node's value based on its dependencies.
     * MUST be zero-allocation for ultra-low latency.
     *
     * Called during graph evaluation in topological order.
     */
    void compute();

    /**
     * Returns the computed value.
     * Should be an inlined getter for performance.
     *
     * @return current computed value
     */
    T getValue();

    /**
     * Returns the node ID (index in the graph).
     */
    int getNodeId();

    /**
     * Sets the node ID. Called during graph construction.
     */
    void setNodeId(int nodeId);

    /**
     * Returns a human-readable name for debugging.
     */
    String getName();

    /**
     * Marks the node as dirty (needs recomputation).
     * Used for incremental evaluation optimization.
     */
    default void markDirty() {
        // Optional: implement for incremental evaluation
    }

    /**
     * Returns whether the node is dirty.
     */
    default boolean isDirty() {
        return true; // Default: always recompute
    }

    /**
     * Resets the node state. Called before graph evaluation.
     */
    default void reset() {
        // Optional: implement if needed
    }
}
