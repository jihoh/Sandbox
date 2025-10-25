package com.lowlatency.graph.hybrid;

import com.lowlatency.graph.hybrid.compiler.CalculatorRegistry;
import com.lowlatency.graph.hybrid.compiler.GraphBuilder;
import com.lowlatency.graph.hybrid.core.HybridCompiledGraph;
import com.lowlatency.graph.hybrid.evaluator.HybridGraphEvaluator;
import com.lowlatency.graph.hybrid.metrics.LatencyTracker;

/**
 * Comprehensive example demonstrating the Hybrid Graph Engine capabilities.
 *
 * This example shows:
 * 1. Building a graph using the fluent API
 * 2. Both FULL and INCREMENTAL evaluation modes
 * 3. Performance comparison between modes
 * 4. Latency tracking and statistics
 * 5. The benefits of Mark & Sweep for localized changes
 */
public class HybridGraphExample {

    public static void main(String[] args) {
        System.out.println("=== Hybrid Graph Engine Example ===\n");

        // Example 1: Simple distance calculation
        simpleDistanceExample();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 2: Performance comparison
        performanceComparisonExample();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 3: Large graph with localized changes
        localizedChangesExample();
    }

    /**
     * Example 1: Calculate Euclidean distance between two points.
     * Graph: sqrt((x2-x1)^2 + (y2-y1)^2)
     */
    private static void simpleDistanceExample() {
        System.out.println("Example 1: Euclidean Distance Calculation\n");

        // Create a standard calculator registry
        CalculatorRegistry registry = CalculatorRegistry.createStandard();

        // Build the graph using fluent API
        HybridCompiledGraph graph = new GraphBuilder()
            .addInput("x1", 1.0)
            .addInput("y1", 2.0)
            .addInput("x2", 4.0)
            .addInput("y2", 6.0)
            .addInput("pow_const", 2.0)
            .addCompute("dx", "SUB", "x2", "x1")        // dx = x2 - x1
            .addCompute("dy", "SUB", "y2", "y1")        // dy = y2 - y1
            .addCompute("dx_sq", "POW", "dx", "pow_const")  // dx^2
            .addCompute("dy_sq", "POW", "dy", "pow_const")  // dy^2
            .addCompute("sum_sq", "ADD", "dx_sq", "dy_sq")  // dx^2 + dy^2
            .addCompute("distance", "SQRT", "sum_sq")       // sqrt(sum)
            .compile(registry);

        graph.printStats();

        // Create evaluator in INCREMENTAL mode
        HybridGraphEvaluator evaluator = new HybridGraphEvaluator(graph);

        // Initial evaluation
        System.out.println("\nInitial calculation:");
        evaluator.evaluate();
        System.out.printf("Distance from (%.1f, %.1f) to (%.1f, %.1f) = %.2f%n",
            graph.getValue("x1"), graph.getValue("y1"),
            graph.getValue("x2"), graph.getValue("y2"),
            graph.getValue("distance"));

        // Update inputs and re-evaluate
        System.out.println("\nUpdating point 2 to (7, 10):");
        evaluator.setInput("x2", 7.0);
        evaluator.setInput("y2", 10.0);
        int nodesComputed = evaluator.evaluate();
        System.out.printf("Distance = %.2f (computed %d nodes)%n",
            graph.getValue("distance"), nodesComputed);

        evaluator.printStats();
    }

    /**
     * Example 2: Compare FULL vs INCREMENTAL evaluation performance.
     */
    private static void performanceComparisonExample() {
        System.out.println("Example 2: Performance Comparison (FULL vs INCREMENTAL)\n");

        // Build a moderate-sized graph: 10 inputs feeding into a tree of computations
        GraphBuilder builder = new GraphBuilder();

        // Add 10 input nodes
        for (int i = 0; i < 10; i++) {
            builder.addInput("in" + i, i * 1.0);
        }

        // Create a computation tree
        // Layer 1: pairwise sums (5 nodes)
        for (int i = 0; i < 5; i++) {
            builder.addCompute("sum_" + i, "ADD", "in" + (i * 2), "in" + (i * 2 + 1));
        }

        // Layer 2: pairwise products (2 nodes + 1 leftover)
        builder.addCompute("prod_0", "MUL", "sum_0", "sum_1");
        builder.addCompute("prod_1", "MUL", "sum_2", "sum_3");

        // Layer 3: final aggregation
        builder.addCompute("final_sum", "SUM", "prod_0", "prod_1", "sum_4");
        builder.addCompute("result", "MUL", "final_sum", "in0");

        HybridCompiledGraph graph = builder.compile();

        System.out.println("Graph structure:");
        graph.printStats();

        // Test FULL mode
        System.out.println("\nTesting FULL evaluation mode:");
        testEvaluationMode(graph, HybridGraphEvaluator.EvaluationMode.FULL);

        // Test INCREMENTAL mode
        System.out.println("\nTesting INCREMENTAL evaluation mode:");
        testEvaluationMode(graph, HybridGraphEvaluator.EvaluationMode.INCREMENTAL);
    }

    private static void testEvaluationMode(HybridCompiledGraph graph,
                                           HybridGraphEvaluator.EvaluationMode mode) {
        HybridGraphEvaluator evaluator = new HybridGraphEvaluator(graph, mode);
        LatencyTracker tracker = new LatencyTracker(10000);

        // Warm up
        for (int i = 0; i < 1000; i++) {
            evaluator.setInput("in0", i);
            evaluator.evaluate();
        }

        evaluator.resetStats();

        // Benchmark: only change one input
        int iterations = 100000;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();

            evaluator.setInput("in0", i);
            evaluator.evaluate();

            long end = System.nanoTime();
            tracker.record(end - start);
        }

        System.out.println("Results after " + iterations + " iterations:");
        evaluator.printStats();
        System.out.println();
        tracker.printStats();
    }

    /**
     * Example 3: Demonstrates incremental evaluation benefits on large graph
     * with localized changes.
     */
    private static void localizedChangesExample() {
        System.out.println("Example 3: Large Graph with Localized Changes\n");

        GraphBuilder builder = new GraphBuilder();

        // Create 3 independent branches, each with 20 inputs
        int branchSize = 20;
        int numBranches = 3;

        for (int branch = 0; branch < numBranches; branch++) {
            String branchName = "branch" + branch;

            // Add inputs for this branch
            for (int i = 0; i < branchSize; i++) {
                builder.addInput(branchName + "_in" + i, i * 1.0);
            }

            // Create a deep computation chain in this branch
            String prev = branchName + "_in0";
            for (int i = 1; i < branchSize; i++) {
                String nodeName = branchName + "_sum" + i;
                builder.addCompute(nodeName, "ADD", prev, branchName + "_in" + i);
                prev = nodeName;
            }

            // Final result for this branch
            builder.addCompute(branchName + "_result", "MUL", prev, branchName + "_in0");
        }

        // Combine all branch results
        builder.addCompute("final_result", "SUM",
            "branch0_result", "branch1_result", "branch2_result");

        HybridCompiledGraph graph = builder.compile();

        System.out.println("Large graph structure:");
        graph.printStats();

        // Compare: change only one branch input
        System.out.println("\nChanging only ONE input in branch 0:");

        // FULL mode: always computes all nodes
        HybridGraphEvaluator fullEvaluator = new HybridGraphEvaluator(
            graph, HybridGraphEvaluator.EvaluationMode.FULL);
        fullEvaluator.setInput("branch0_in0", 42.0);
        int fullNodesComputed = fullEvaluator.evaluate();

        // INCREMENTAL mode: only computes affected nodes
        HybridGraphEvaluator incEvaluator = new HybridGraphEvaluator(
            graph, HybridGraphEvaluator.EvaluationMode.INCREMENTAL);
        incEvaluator.setInput("branch0_in0", 42.0);
        int incNodesComputed = incEvaluator.evaluate();

        System.out.printf("FULL mode computed:        %d nodes (%.1f%% of graph)%n",
            fullNodesComputed,
            100.0 * fullNodesComputed / graph.computationNodeCount);

        System.out.printf("INCREMENTAL mode computed: %d nodes (%.1f%% of graph)%n",
            incNodesComputed,
            100.0 * incNodesComputed / graph.computationNodeCount);

        System.out.printf("Speedup: %.1fx fewer nodes computed%n",
            (double) fullNodesComputed / incNodesComputed);

        System.out.println("\nThis demonstrates the power of incremental evaluation!");
        System.out.println("When only a small part of the graph changes, INCREMENTAL mode");
        System.out.println("can be dramatically faster than FULL mode.");
    }
}
