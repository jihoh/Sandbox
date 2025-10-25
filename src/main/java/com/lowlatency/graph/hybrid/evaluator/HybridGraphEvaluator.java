package com.lowlatency.graph.hybrid.evaluator;

import com.lowlatency.graph.hybrid.core.Calculator;
import com.lowlatency.graph.hybrid.core.HybridCompiledGraph;

import java.util.ArrayDeque;
import java.util.BitSet;

/**
 * High-performance graph evaluator with configurable evaluation strategies.
 *
 * Supported strategies:
 * 1. FULL: Always evaluates all computation nodes (simple, predictable)
 * 2. INCREMENTAL: Mark & Sweep - only recomputes affected nodes (optimal for localized changes)
 *
 * Design principles:
 * - Zero allocation in hot path
 * - Cache-friendly sequential access
 * - Single-threaded for predictable latency
 * - Reusable data structures
 *
 * Performance:
 * - FULL mode: O(C) where C = computation nodes
 * - INCREMENTAL mode: O(A) where A = affected nodes (usually << C)
 */
public final class HybridGraphEvaluator {

    public enum EvaluationMode {
        /** Always evaluate all computation nodes */
        FULL,
        /** Only evaluate nodes affected by changed inputs (Mark & Sweep) */
        INCREMENTAL
    }

    private final HybridCompiledGraph graph;
    private final EvaluationMode mode;

    // Incremental evaluation state (reused across evaluations)
    private final BitSet dirtyInputs;
    private final BitSet nodesToCompute;
    private final ArrayDeque<Integer> dfsStack;

    // Statistics
    private long evaluationCount = 0;
    private long totalNodesComputed = 0;
    private long totalEvaluationNanos = 0;

    /**
     * Creates an evaluator with specified mode.
     */
    public HybridGraphEvaluator(HybridCompiledGraph graph, EvaluationMode mode) {
        this.graph = graph;
        this.mode = mode;

        // Allocate reusable data structures for incremental mode
        if (mode == EvaluationMode.INCREMENTAL) {
            this.dirtyInputs = new BitSet(graph.nodeCount);
            this.nodesToCompute = new BitSet(graph.nodeCount);
            this.dfsStack = new ArrayDeque<>(graph.nodeCount);
        } else {
            this.dirtyInputs = null;
            this.nodesToCompute = null;
            this.dfsStack = null;
        }
    }

    /**
     * Creates an evaluator with INCREMENTAL mode (default).
     */
    public HybridGraphEvaluator(HybridCompiledGraph graph) {
        this(graph, EvaluationMode.INCREMENTAL);
    }

    // ========== Evaluation API ==========

    /**
     * Evaluates the entire graph based on the configured strategy.
     * Zero-allocation hot path.
     *
     * @return number of nodes computed
     */
    public int evaluate() {
        long startNanos = System.nanoTime();

        int nodesComputed = (mode == EvaluationMode.FULL)
            ? evaluateFull()
            : evaluateIncremental();

        long endNanos = System.nanoTime();
        evaluationCount++;
        totalNodesComputed += nodesComputed;
        totalEvaluationNanos += (endNanos - startNanos);

        return nodesComputed;
    }

    /**
     * Marks an input node as dirty for incremental evaluation.
     * Call this after changing an input value.
     */
    public void markDirty(int nodeId) {
        if (mode == EvaluationMode.INCREMENTAL) {
            dirtyInputs.set(nodeId);
        }
    }

    /**
     * Marks an input node as dirty by name.
     */
    public void markDirty(String nodeName) {
        Integer nodeId = graph.inputNodeIds.get(nodeName);
        if (nodeId == null) {
            throw new IllegalArgumentException("Unknown or non-input node: " + nodeName);
        }
        markDirty(nodeId);
    }

    /**
     * Sets an input value and marks it dirty.
     * Convenience method combining setValue + markDirty.
     */
    public void setInput(String nodeName, double value) {
        Integer nodeId = graph.inputNodeIds.get(nodeName);
        if (nodeId == null) {
            throw new IllegalArgumentException("Unknown or non-input node: " + nodeName);
        }
        setInput(nodeId, value);
    }

    /**
     * Sets an input value by ID and marks it dirty.
     */
    public void setInput(int nodeId, double value) {
        if (changed(graph.values[nodeId], value)) {
            graph.values[nodeId] = value;
            if (mode == EvaluationMode.INCREMENTAL) {
                dirtyInputs.set(nodeId);
            }
        }
    }

    /**
     * Sets multiple inputs at once (batch update).
     */
    public void setInputs(int[] nodeIds, double[] values) {
        if (nodeIds.length != values.length) {
            throw new IllegalArgumentException("nodeIds and values length mismatch");
        }
        for (int i = 0; i < nodeIds.length; i++) {
            setInput(nodeIds[i], values[i]);
        }
    }

    // ========== Full Evaluation ==========

    /**
     * Evaluates all computation nodes in topological order.
     * Simple, predictable, always correct.
     */
    private int evaluateFull() {
        final int[] order = graph.computationOrder;
        final Calculator[] calculators = graph.calculators;
        final double[] values = graph.values;
        final int length = order.length;

        for (int i = 0; i < length; i++) {
            final int nodeId = order[i];
            values[nodeId] = calculators[nodeId].compute(nodeId, graph);
        }

        return length;
    }

    // ========== Incremental Evaluation (Mark & Sweep) ==========

    /**
     * Evaluates only nodes affected by dirty inputs.
     * Two-phase algorithm:
     * 1. MARK: DFS from dirty inputs to mark all downstream nodes
     * 2. SWEEP: Evaluate marked nodes in topological order
     */
    private int evaluateIncremental() {
        // Optimization: if no inputs changed, nothing to compute
        if (dirtyInputs.isEmpty()) {
            return 0;
        }

        // --- MARK PHASE ---
        // Find all nodes downstream from dirty inputs
        for (int dirtyId = dirtyInputs.nextSetBit(0);
             dirtyId >= 0;
             dirtyId = dirtyInputs.nextSetBit(dirtyId + 1)) {

            // Mark all direct children of this dirty input
            int childStart = graph.getChildStartIndex(dirtyId);
            int childEnd = graph.getChildEndIndex(dirtyId);

            for (int i = childStart; i < childEnd; i++) {
                dfsMarkIterative(graph.childValues[i]);
            }
        }

        dirtyInputs.clear(); // Reset for next evaluation

        // --- SWEEP PHASE ---
        // Evaluate marked nodes in topological order
        final int[] order = graph.computationOrder;
        final Calculator[] calculators = graph.calculators;
        final double[] values = graph.values;
        int nodesComputed = 0;

        for (int nodeId : order) {
            if (nodesToCompute.get(nodeId)) {
                values[nodeId] = calculators[nodeId].compute(nodeId, graph);
                nodesComputed++;
            }
        }

        nodesToCompute.clear(); // Reset for next evaluation

        return nodesComputed;
    }

    /**
     * Iterative DFS to mark a node and all its descendants.
     * Stack-safe alternative to recursion.
     */
    private void dfsMarkIterative(int startNodeId) {
        // Optimization: if already marked, skip
        if (nodesToCompute.get(startNodeId)) {
            return;
        }

        dfsStack.push(startNodeId);

        while (!dfsStack.isEmpty()) {
            int nodeId = dfsStack.pop();

            // Double-check in case marked while on stack
            if (nodesToCompute.get(nodeId)) {
                continue;
            }

            nodesToCompute.set(nodeId); // Mark as needing computation

            // Add all children to stack
            int childStart = graph.getChildStartIndex(nodeId);
            int childEnd = graph.getChildEndIndex(nodeId);

            // Push in reverse order for roughly FIFO processing
            for (int i = childEnd - 1; i >= childStart; i--) {
                int childId = graph.childValues[i];
                if (!nodesToCompute.get(childId)) {
                    dfsStack.push(childId);
                }
            }
        }
    }

    // ========== Utilities ==========

    /**
     * NaN-safe comparison for double values.
     * Using != is incorrect for NaN since NaN != NaN is true.
     */
    private static boolean changed(double a, double b) {
        return Double.doubleToRawLongBits(a) != Double.doubleToRawLongBits(b);
    }

    /**
     * Resets all statistics.
     */
    public void resetStats() {
        evaluationCount = 0;
        totalNodesComputed = 0;
        totalEvaluationNanos = 0;
    }

    // ========== Getters ==========

    public HybridCompiledGraph getGraph() {
        return graph;
    }

    public EvaluationMode getMode() {
        return mode;
    }

    public long getEvaluationCount() {
        return evaluationCount;
    }

    public long getTotalNodesComputed() {
        return totalNodesComputed;
    }

    public double getAverageNodesPerEvaluation() {
        return evaluationCount == 0 ? 0.0 : (double) totalNodesComputed / evaluationCount;
    }

    public double getAverageEvaluationNanos() {
        return evaluationCount == 0 ? 0.0 : (double) totalEvaluationNanos / evaluationCount;
    }

    public double getAverageEvaluationMicros() {
        return getAverageEvaluationNanos() / 1000.0;
    }

    /**
     * Prints evaluation statistics.
     */
    public void printStats() {
        System.out.println("=== Evaluator Statistics ===");
        System.out.printf("Mode:                  %s%n", mode);
        System.out.printf("Evaluations:           %,d%n", evaluationCount);
        System.out.printf("Total nodes computed:  %,d%n", totalNodesComputed);
        System.out.printf("Avg nodes/eval:        %.2f (%.1f%% of total)%n",
            getAverageNodesPerEvaluation(),
            100.0 * getAverageNodesPerEvaluation() / graph.computationNodeCount);
        System.out.printf("Avg time/eval:         %.2f µs (%.0f ns)%n",
            getAverageEvaluationMicros(),
            getAverageEvaluationNanos());
    }
}
