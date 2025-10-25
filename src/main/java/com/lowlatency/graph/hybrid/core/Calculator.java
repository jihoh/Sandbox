package com.lowlatency.graph.hybrid.core;

/**
 * A functional interface representing a pure computation kernel for a node.
 *
 * Design principles:
 * - Stateless and non-blocking
 * - Zero allocation in compute path
 * - JIT-friendly for monomorphic call sites
 * - Supports both primitive and object values
 *
 * Performance: This interface is designed to enable JIT inlining.
 * The single abstract method makes it amenable to lambda expressions.
 */
@FunctionalInterface
public interface Calculator {

    /**
     * Computes the value for a given node based on the current graph state.
     *
     * MUST be zero-allocation and non-blocking for ultra-low latency.
     *
     * @param nodeId The unique integer ID of the node to compute
     * @param graph The compiled graph containing all data arrays
     * @return The computed double value for the node
     */
    double compute(int nodeId, HybridCompiledGraph graph);

    /**
     * Returns whether this calculator is deterministic.
     * Default: true (most calculators are pure functions)
     */
    default boolean isDeterministic() {
        return true;
    }

    /**
     * Returns a human-readable description of this calculator.
     * Used for debugging and visualization.
     */
    default String getDescription() {
        return getClass().getSimpleName();
    }
}
