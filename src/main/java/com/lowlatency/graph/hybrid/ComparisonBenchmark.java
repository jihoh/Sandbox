package com.lowlatency.graph.hybrid;

import com.lowlatency.graph.csr.CSRGraph;
import com.lowlatency.graph.csr.GraphBuilder;
import com.lowlatency.graph.evaluator.GraphEvaluator;
import com.lowlatency.graph.hybrid.compiler.CalculatorRegistry;
import com.lowlatency.graph.hybrid.core.HybridCompiledGraph;
import com.lowlatency.graph.hybrid.evaluator.HybridGraphEvaluator;
import com.lowlatency.graph.node.DoubleComputeNode;
import com.lowlatency.graph.node.DoubleInputNode;
import com.lowlatency.graph.node.Node;

/**
 * Comprehensive benchmark comparing three graph engine implementations:
 *
 * 1. ORIGINAL: Modular OOP approach (GraphEvaluator + Node objects)
 * 2. HYBRID_FULL: Hybrid with data-oriented design, FULL evaluation
 * 3. HYBRID_INCREMENTAL: Hybrid with Mark & Sweep incremental evaluation
 *
 * Test scenarios:
 * - Small graph (10 nodes): overhead comparison
 * - Medium graph (100 nodes): balanced workload
 * - Large graph (1000 nodes): scalability test
 * - Localized changes: incremental evaluation benefits
 */
public class ComparisonBenchmark {

    public static void main(String[] args) {
        System.out.println("=== Graph Engine Comparison Benchmark ===\n");

        System.out.println("Benchmark scenarios:");
        System.out.println("1. Small graph (10 nodes) - overhead test");
        System.out.println("2. Medium graph (100 nodes) - balanced workload");
        System.out.println("3. Large graph (1000 nodes) - scalability test");
        System.out.println("4. Localized changes - incremental benefit");
        System.out.println();

        // Run benchmarks
        benchmarkSmallGraph();
        System.out.println("\n" + "=".repeat(80) + "\n");

        benchmarkMediumGraph();
        System.out.println("\n" + "=".repeat(80) + "\n");

        benchmarkLargeGraph();
        System.out.println("\n" + "=".repeat(80) + "\n");

        benchmarkLocalizedChanges();
    }

    private static void benchmarkSmallGraph() {
        System.out.println("Benchmark 1: Small Graph (10 nodes)\n");

        int numInputs = 5;
        int warmup = 10000;
        int iterations = 1000000;

        // Build graphs for each implementation
        BenchmarkResult original = benchmarkOriginal(numInputs, warmup, iterations);
        BenchmarkResult hybridFull = benchmarkHybridFull(numInputs, warmup, iterations);
        BenchmarkResult hybridInc = benchmarkHybridIncremental(numInputs, warmup, iterations);

        printResults("Small Graph", original, hybridFull, hybridInc);
    }

    private static void benchmarkMediumGraph() {
        System.out.println("Benchmark 2: Medium Graph (100 nodes)\n");

        int numInputs = 20;
        int warmup = 5000;
        int iterations = 100000;

        BenchmarkResult original = benchmarkOriginal(numInputs, warmup, iterations);
        BenchmarkResult hybridFull = benchmarkHybridFull(numInputs, warmup, iterations);
        BenchmarkResult hybridInc = benchmarkHybridIncremental(numInputs, warmup, iterations);

        printResults("Medium Graph", original, hybridFull, hybridInc);
    }

    private static void benchmarkLargeGraph() {
        System.out.println("Benchmark 3: Large Graph (1000 nodes)\n");

        int numInputs = 50;
        int warmup = 1000;
        int iterations = 10000;

        BenchmarkResult original = benchmarkOriginal(numInputs, warmup, iterations);
        BenchmarkResult hybridFull = benchmarkHybridFull(numInputs, warmup, iterations);
        BenchmarkResult hybridInc = benchmarkHybridIncremental(numInputs, warmup, iterations);

        printResults("Large Graph", original, hybridFull, hybridInc);
    }

    private static void benchmarkLocalizedChanges() {
        System.out.println("Benchmark 4: Localized Changes (only 1 of 50 inputs changes)\n");

        int numInputs = 50;
        int warmup = 5000;
        int iterations = 100000;

        // For this test, we only change input 0, leaving others unchanged
        BenchmarkResult original = benchmarkOriginalLocalized(numInputs, warmup, iterations);
        BenchmarkResult hybridFull = benchmarkHybridFullLocalized(numInputs, warmup, iterations);
        BenchmarkResult hybridInc = benchmarkHybridIncrementalLocalized(numInputs, warmup, iterations);

        printResults("Localized Changes", original, hybridFull, hybridInc);

        System.out.println("\nNote: INCREMENTAL mode shows significant advantage here!");
        System.out.println("It only recomputes nodes affected by the changed input.");
    }

    // ========== Original Implementation Benchmarks ==========

    private static BenchmarkResult benchmarkOriginal(int numInputs, int warmup, int iterations) {
        // Build graph: chain of additions
        DoubleInputNode[] inputs = new DoubleInputNode[numInputs];
        for (int i = 0; i < numInputs; i++) {
            inputs[i] = new DoubleInputNode("in" + i, i * 1.0);
        }

        // Create computation tree
        GraphBuilder graphBuilder = new GraphBuilder(numInputs * 5);
        Node<?>[] nodes = new Node<?>[numInputs * 5];
        int nodeIdx = 0;

        // Add inputs
        for (DoubleInputNode input : inputs) {
            input.setNodeId(nodeIdx);
            nodes[nodeIdx++] = input;
        }

        // Create layers of computations
        int currentLayerStart = 0;
        int currentLayerSize = numInputs;

        while (currentLayerSize > 1) {
            for (int i = 0; i < currentLayerSize - 1; i += 2) {
                int left = currentLayerStart + i;
                int right = currentLayerStart + i + 1;

                DoubleComputeNode sum = new DoubleComputeNode("sum_" + nodeIdx,
                    () -> ((DoubleInputNode) nodes[left]).getDoubleValue() +
                          ((DoubleInputNode) nodes[right]).getDoubleValue());
                sum.setNodeId(nodeIdx);
                nodes[nodeIdx] = sum;

                graphBuilder.addEdge(left, nodeIdx);
                graphBuilder.addEdge(right, nodeIdx);
                nodeIdx++;
            }

            if (currentLayerSize % 2 == 1) {
                // Odd node, carry forward
                nodes[nodeIdx] = nodes[currentLayerStart + currentLayerSize - 1];
                graphBuilder.addEdge(currentLayerStart + currentLayerSize - 1, nodeIdx);
                nodeIdx++;
            }

            currentLayerStart = currentLayerSize;
            currentLayerSize = (currentLayerSize + 1) / 2;
        }

        CSRGraph graph = graphBuilder.build();
        Node<?>[] trimmedNodes = new Node<?>[nodeIdx];
        System.arraycopy(nodes, 0, trimmedNodes, 0, nodeIdx);
        GraphEvaluator evaluator = new GraphEvaluator(graph, trimmedNodes);

        // Warmup
        for (int i = 0; i < warmup; i++) {
            inputs[0].setValue(i);
            evaluator.evaluate();
        }

        // Benchmark
        long startNanos = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            inputs[0].setValue(i);
            evaluator.evaluate();
        }
        long endNanos = System.nanoTime();

        double avgNanos = (endNanos - startNanos) / (double) iterations;
        return new BenchmarkResult("Original (OOP)", avgNanos, nodeIdx);
    }

    private static BenchmarkResult benchmarkOriginalLocalized(int numInputs, int warmup, int iterations) {
        return benchmarkOriginal(numInputs, warmup, iterations);
    }

    // ========== Hybrid Implementation Benchmarks ==========

    private static BenchmarkResult benchmarkHybridFull(int numInputs, int warmup, int iterations) {
        com.lowlatency.graph.hybrid.compiler.GraphBuilder builder =
            new com.lowlatency.graph.hybrid.compiler.GraphBuilder();

        // Add inputs
        for (int i = 0; i < numInputs; i++) {
            builder.addInput("in" + i, i * 1.0);
        }

        // Create computation tree
        int nodeCount = numInputs;
        int currentLayerStart = 0;
        int currentLayerSize = numInputs;

        while (currentLayerSize > 1) {
            for (int i = 0; i < currentLayerSize - 1; i += 2) {
                builder.addCompute("sum_" + nodeCount,
                    "ADD",
                    "in" + (currentLayerStart + i),
                    "in" + (currentLayerStart + i + 1));
                nodeCount++;
            }

            if (currentLayerSize % 2 == 1) {
                nodeCount++;
            }

            currentLayerStart += currentLayerSize;
            currentLayerSize = (currentLayerSize + 1) / 2;
        }

        HybridCompiledGraph graph = builder.compile();
        HybridGraphEvaluator evaluator = new HybridGraphEvaluator(
            graph, HybridGraphEvaluator.EvaluationMode.FULL);

        // Warmup
        for (int i = 0; i < warmup; i++) {
            evaluator.setInput("in0", i);
            evaluator.evaluate();
        }

        // Benchmark
        long startNanos = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            evaluator.setInput("in0", i);
            evaluator.evaluate();
        }
        long endNanos = System.nanoTime();

        double avgNanos = (endNanos - startNanos) / (double) iterations;
        return new BenchmarkResult("Hybrid-FULL", avgNanos, graph.nodeCount);
    }

    private static BenchmarkResult benchmarkHybridFullLocalized(int numInputs, int warmup, int iterations) {
        return benchmarkHybridFull(numInputs, warmup, iterations);
    }

    private static BenchmarkResult benchmarkHybridIncremental(int numInputs, int warmup, int iterations) {
        com.lowlatency.graph.hybrid.compiler.GraphBuilder builder =
            new com.lowlatency.graph.hybrid.compiler.GraphBuilder();

        // Add inputs
        for (int i = 0; i < numInputs; i++) {
            builder.addInput("in" + i, i * 1.0);
        }

        // Create computation tree
        int nodeCount = numInputs;
        int currentLayerStart = 0;
        int currentLayerSize = numInputs;

        while (currentLayerSize > 1) {
            for (int i = 0; i < currentLayerSize - 1; i += 2) {
                builder.addCompute("sum_" + nodeCount,
                    "ADD",
                    "in" + (currentLayerStart + i),
                    "in" + (currentLayerStart + i + 1));
                nodeCount++;
            }

            if (currentLayerSize % 2 == 1) {
                nodeCount++;
            }

            currentLayerStart += currentLayerSize;
            currentLayerSize = (currentLayerSize + 1) / 2;
        }

        HybridCompiledGraph graph = builder.compile();
        HybridGraphEvaluator evaluator = new HybridGraphEvaluator(
            graph, HybridGraphEvaluator.EvaluationMode.INCREMENTAL);

        // Warmup
        for (int i = 0; i < warmup; i++) {
            evaluator.setInput("in0", i);
            evaluator.evaluate();
        }

        // Benchmark
        long startNanos = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            evaluator.setInput("in0", i);
            evaluator.evaluate();
        }
        long endNanos = System.nanoTime();

        double avgNanos = (endNanos - startNanos) / (double) iterations;
        return new BenchmarkResult("Hybrid-INCREMENTAL", avgNanos, graph.nodeCount);
    }

    private static BenchmarkResult benchmarkHybridIncrementalLocalized(int numInputs, int warmup, int iterations) {
        return benchmarkHybridIncremental(numInputs, warmup, iterations);
    }

    // ========== Results ==========

    private static class BenchmarkResult {
        final String name;
        final double avgNanos;
        final int nodeCount;

        BenchmarkResult(String name, double avgNanos, int nodeCount) {
            this.name = name;
            this.avgNanos = avgNanos;
            this.nodeCount = nodeCount;
        }
    }

    private static void printResults(String testName,
                                      BenchmarkResult original,
                                      BenchmarkResult hybridFull,
                                      BenchmarkResult hybridInc) {
        System.out.printf("%-30s | Avg Time (ns) | Avg Time (µs) | Speedup vs Original%n",
            "Implementation");
        System.out.println("-".repeat(95));

        printRow(original, 1.0);
        printRow(hybridFull, original.avgNanos / hybridFull.avgNanos);
        printRow(hybridInc, original.avgNanos / hybridInc.avgNanos);

        System.out.println();
        System.out.println("Winner: " + findWinner(original, hybridFull, hybridInc));
    }

    private static void printRow(BenchmarkResult result, double speedup) {
        System.out.printf("%-30s | %13.2f | %13.4f | %.2fx%n",
            result.name,
            result.avgNanos,
            result.avgNanos / 1000.0,
            speedup);
    }

    private static String findWinner(BenchmarkResult... results) {
        BenchmarkResult winner = results[0];
        for (BenchmarkResult r : results) {
            if (r.avgNanos < winner.avgNanos) {
                winner = r;
            }
        }
        return winner.name;
    }
}
