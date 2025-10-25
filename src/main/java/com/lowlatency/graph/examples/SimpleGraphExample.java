package com.lowlatency.graph.examples;

import com.lowlatency.graph.evaluator.GraphEvaluator;
import com.lowlatency.graph.evaluator.GraphEvaluatorBuilder;
import com.lowlatency.graph.node.DoubleComputeNode;
import com.lowlatency.graph.node.DoubleInputNode;

/**
 * Simple example demonstrating basic graph framework usage.
 *
 * Graph: a --> sum <-- b
 *        |           ^
 *        |           |
 *        +-> product-+
 *
 * sum = a + b
 * product = a * b
 */
public class SimpleGraphExample {

    public static void main(String[] args) {
        System.out.println("=== Simple Graph Example ===\n");

        // Create input nodes
        DoubleInputNode a = new DoubleInputNode("a", 10.0);
        DoubleInputNode b = new DoubleInputNode("b", 20.0);

        // Create computation nodes
        DoubleComputeNode sum = new DoubleComputeNode("sum",
                () -> a.getDoubleValue() + b.getDoubleValue()
        );

        DoubleComputeNode product = new DoubleComputeNode("product",
                () -> a.getDoubleValue() * b.getDoubleValue()
        );

        DoubleComputeNode sumPlusProduct = new DoubleComputeNode("sumPlusProduct",
                () -> sum.getDoubleValue() + product.getDoubleValue()
        );

        // Build the graph
        GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();

        int aId = builder.addNode(a);
        int bId = builder.addNode(b);
        int sumId = builder.addNode(sum);
        int productId = builder.addNode(product);
        int sumPlusProductId = builder.addNode(sumPlusProduct);

        // Define dependencies
        builder.addDependency("sum", "a");
        builder.addDependency("sum", "b");
        builder.addDependency("product", "a");
        builder.addDependency("product", "b");
        builder.addDependency("sumPlusProduct", "sum");
        builder.addDependency("sumPlusProduct", "product");

        GraphEvaluator evaluator = builder.build();

        System.out.printf("Graph: %d nodes, %d dependencies%n%n",
                builder.getNumNodes(), builder.getNumDependencies());

        // Initial evaluation
        System.out.println("Initial values:");
        evaluator.evaluate();
        printResults(a, b, sum, product, sumPlusProduct);

        // Update input and re-evaluate
        System.out.println("\nUpdating a = 5.0, b = 15.0");
        a.setValue(5.0);
        b.setValue(15.0);
        evaluator.evaluate();
        printResults(a, b, sum, product, sumPlusProduct);

        // Benchmark
        System.out.println("\n=== Performance Test ===");
        int iterations = 1_000_000;

        // Warm up
        for (int i = 0; i < 10000; i++) {
            a.setValue(i);
            evaluator.evaluate();
        }

        // Measure
        long startNanos = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            a.setValue(i);
            evaluator.evaluate();
        }
        long endNanos = System.nanoTime();

        double totalTimeMs = (endNanos - startNanos) / 1_000_000.0;
        double avgTimeNanos = (endNanos - startNanos) / (double) iterations;

        System.out.printf("Iterations: %d%n", iterations);
        System.out.printf("Total time: %.2f ms%n", totalTimeMs);
        System.out.printf("Avg time per evaluation: %.2f ns (%.4f µs)%n",
                avgTimeNanos, avgTimeNanos / 1000.0);
        System.out.printf("Throughput: %.2f evaluations/sec%n",
                iterations / (totalTimeMs / 1000.0));
    }

    private static void printResults(DoubleInputNode a, DoubleInputNode b,
                                      DoubleComputeNode sum, DoubleComputeNode product,
                                      DoubleComputeNode sumPlusProduct) {
        System.out.printf("  a = %.2f%n", a.getDoubleValue());
        System.out.printf("  b = %.2f%n", b.getDoubleValue());
        System.out.printf("  sum = %.2f%n", sum.getDoubleValue());
        System.out.printf("  product = %.2f%n", product.getDoubleValue());
        System.out.printf("  sumPlusProduct = %.2f%n", sumPlusProduct.getDoubleValue());
    }
}
