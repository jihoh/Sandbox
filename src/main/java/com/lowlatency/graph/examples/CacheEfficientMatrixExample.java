package com.lowlatency.graph.examples;

import com.lowlatency.graph.node.*;
import com.lowlatency.graph.ops.MatrixOpsFlat;
import com.lowlatency.graph.evaluator.GraphEvaluator;
import com.lowlatency.graph.evaluator.GraphEvaluatorBuilder;

/**
 * Example demonstrating cache-efficient matrix operations using flat arrays.
 *
 * WHY THIS MATTERS:
 * - 2D arrays (double[][]) have pointer indirection overhead
 * - Each row is a separate object, breaking cache locality
 * - Flat arrays (double[]) provide sequential memory access
 * - CPU can prefetch and cache-line optimize
 *
 * PERFORMANCE COMPARISON:
 * - 2D array access: matrix[i][j] → 2 pointer dereferences
 * - Flat array access: matrix[i*cols + j] → 1 multiplication + addition (faster!)
 * - Modern CPUs can compute offsets faster than chasing pointers
 *
 * This example shows the RECOMMENDED way to use matrices in hot paths.
 */
public class CacheEfficientMatrixExample {

    public static void main(String[] args) {
        System.out.println("=== Cache-Efficient Matrix Example ===\n");

        matrixMultiplicationExample();
        System.out.println();
        portfolioRiskExample();
    }

    /**
     * Example 1: Matrix multiplication using cache-efficient flat arrays
     */
    private static void matrixMultiplicationExample() {
        System.out.println("--- Matrix Multiplication (Flat Arrays) ---");

        // Create matrices using flat arrays (row-major order)
        // A = [[1, 2],     B = [[5, 6],
        //      [3, 4]]          [7, 8]]
        MatrixInputNode a = new MatrixInputNode("A", new double[][]{{1, 2}, {3, 4}});
        MatrixInputNode b = new MatrixInputNode("B", new double[][]{{5, 6}, {7, 8}});

        // Create compute node using FLAT ARRAY API (cache-efficient)
        MatrixComputeNode product = new MatrixComputeNode("A×B", 2, 2,
            (MatrixComputeNode.FlatMatrixComputeFunction) dest -> {
                // Get flat arrays directly (no conversion overhead)
                double[] aFlat = a.getMatrixValueFlat();
                double[] bFlat = b.getMatrixValueFlat();

                // Use cache-efficient flat matrix multiplication
                MatrixOpsFlat.multiply(aFlat, bFlat, dest, 2, 2, 2);
            }
        );

        // Build and evaluate
        GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();
        builder.addNode(a);
        builder.addNode(b);
        builder.addNode(product);
        builder.addDependency("A×B", "A");
        builder.addDependency("A×B", "B");

        GraphEvaluator evaluator = builder.build();
        evaluator.evaluate();

        System.out.println("Matrix A:");
        printMatrix(a.getMatrixValue());
        System.out.println("Matrix B:");
        printMatrix(b.getMatrixValue());
        System.out.println("A × B (using cache-efficient flat arrays):");
        printMatrix(product.getMatrixValue());
    }

    /**
     * Example 2: Portfolio risk calculation using flat arrays
     * Computes: risk = sqrt(w^T * Cov * w)
     */
    private static void portfolioRiskExample() {
        System.out.println("--- Portfolio Risk (Cache-Efficient) ---");

        // Covariance matrix (3×3)
        double[][] covMatrix = {
            {0.04, 0.01, 0.02},
            {0.01, 0.09, 0.03},
            {0.02, 0.03, 0.16}
        };
        MatrixInputNode covariance = new MatrixInputNode("Covariance", covMatrix);

        // Portfolio weights
        VectorInputNode weights = new VectorInputNode("Weights", new double[]{0.5, 0.3, 0.2});

        // Compute Cov * w using cache-efficient flat arrays
        VectorComputeNode covWeights = new VectorComputeNode("Cov*w", 3,
            (VectorComputeNode.VectorComputeFunction) () -> {
                double[] result = new double[3];
                // Use flat array API for cache efficiency
                MatrixOpsFlat.multiplyVector(
                    covariance.getMatrixValueFlat(),
                    weights.getVectorValue(),
                    result,
                    3, 3
                );
                return result;
            }
        );

        // Compute w^T * (Cov * w) = portfolio variance
        DoubleComputeNode variance = new DoubleComputeNode("Variance", () -> {
            double[] w = weights.getVectorValue();
            double[] covW = covWeights.getVectorValue();
            double sum = 0.0;
            for (int i = 0; i < 3; i++) {
                sum += w[i] * covW[i];
            }
            return sum;
        });

        // Compute volatility = sqrt(variance)
        DoubleComputeNode volatility = new DoubleComputeNode("Volatility", () ->
            Math.sqrt(variance.getDoubleValue())
        );

        // Build and evaluate
        GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();
        builder.addNode(covariance);
        builder.addNode(weights);
        builder.addNode(covWeights);
        builder.addNode(variance);
        builder.addNode(volatility);

        builder.addDependency("Cov*w", "Covariance");
        builder.addDependency("Cov*w", "Weights");
        builder.addDependency("Variance", "Weights");
        builder.addDependency("Variance", "Cov*w");
        builder.addDependency("Volatility", "Variance");

        GraphEvaluator evaluator = builder.build();
        evaluator.evaluate();

        System.out.println("Portfolio Weights: [0.5, 0.3, 0.2]");
        System.out.printf("Portfolio Variance: %.6f\n", variance.getDoubleValue());
        System.out.printf("Portfolio Volatility (Risk): %.4f%%\n", volatility.getDoubleValue() * 100);
        System.out.println("\nNote: This computation used cache-efficient flat arrays!");
    }

    private static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            System.out.print("  [");
            for (int i = 0; i < row.length; i++) {
                System.out.printf("%.2f", row[i]);
                if (i < row.length - 1) System.out.print(", ");
            }
            System.out.println("]");
        }
    }
}
