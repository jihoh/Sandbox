package com.lowlatency.graph.examples;

import com.lowlatency.graph.node.*;
import com.lowlatency.graph.ops.MatrixOps;
import com.lowlatency.graph.ops.VectorOps;
import com.lowlatency.graph.evaluator.GraphEvaluator;
import com.lowlatency.graph.evaluator.GraphEvaluatorBuilder;

import java.util.Arrays;

/**
 * Example demonstrating Vector and Matrix support in the graph framework.
 *
 * This example shows:
 * 1. Vector operations (addition, scaling, dot product)
 * 2. Matrix operations (multiplication, transpose)
 * 3. Integration with the graph evaluation system
 *
 * Use case: Simple linear algebra pipeline for a portfolio optimization calculation
 */
public class VectorMatrixExample {

    public static void main(String[] args) {
        System.out.println("=== Vector and Matrix Graph Example ===\n");

        vectorExample();
        System.out.println();
        matrixExample();
        System.out.println();
        combinedExample();
    }

    /**
     * Example 1: Vector operations
     */
    private static void vectorExample() {
        System.out.println("--- Vector Example ---");

        // Create input vectors (e.g., asset returns)
        VectorInputNode returns1 = new VectorInputNode("returns1", new double[]{0.05, 0.03, 0.07});
        VectorInputNode returns2 = new VectorInputNode("returns2", new double[]{0.02, 0.04, 0.06});
        VectorInputNode weights = new VectorInputNode("weights", new double[]{0.4, 0.3, 0.3});

        // Compute average returns
        VectorComputeNode avgReturns = new VectorComputeNode("avgReturns", 3, () ->
            VectorOps.scale(
                VectorOps.add(returns1.getVectorValue(), returns2.getVectorValue()),
                0.5
            )
        );

        // Compute weighted average (portfolio return components)
        VectorComputeNode weightedReturns = new VectorComputeNode("weightedReturns", 3, () ->
            VectorOps.multiply(avgReturns.getVectorValue(), weights.getVectorValue())
        );

        // Compute total portfolio return (dot product)
        DoubleComputeNode portfolioReturn = new DoubleComputeNode("portfolioReturn", () ->
            VectorOps.sum(weightedReturns.getVectorValue())
        );

        // Build and evaluate graph
        GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();
        builder.addNode(returns1);
        builder.addNode(returns2);
        builder.addNode(weights);
        builder.addNode(avgReturns);
        builder.addNode(weightedReturns);
        builder.addNode(portfolioReturn);

        // Define dependencies
        builder.addDependency("avgReturns", "returns1");
        builder.addDependency("avgReturns", "returns2");
        builder.addDependency("weightedReturns", "avgReturns");
        builder.addDependency("weightedReturns", "weights");
        builder.addDependency("portfolioReturn", "weightedReturns");

        GraphEvaluator evaluator = builder.build();

        evaluator.evaluate();

        System.out.println("Returns 1: " + Arrays.toString(returns1.getVectorValue()));
        System.out.println("Returns 2: " + Arrays.toString(returns2.getVectorValue()));
        System.out.println("Weights: " + Arrays.toString(weights.getVectorValue()));
        System.out.println("Avg Returns: " + Arrays.toString(avgReturns.getVectorValue()));
        System.out.println("Weighted Returns: " + Arrays.toString(weightedReturns.getVectorValue()));
        System.out.printf("Portfolio Return: %.4f\n", portfolioReturn.getDoubleValue());
    }

    /**
     * Example 2: Matrix operations
     */
    private static void matrixExample() {
        System.out.println("--- Matrix Example ---");

        // Create transformation matrix
        MatrixInputNode transform = new MatrixInputNode("transform",
            new double[][]{{2, 0}, {0, 3}}
        );

        // Create point vectors as a matrix
        MatrixInputNode points = new MatrixInputNode("points",
            new double[][]{{1, 2}, {3, 4}}
        );

        // Apply transformation
        MatrixComputeNode transformed = new MatrixComputeNode("transformed", 2, 2, () ->
            MatrixOps.multiply(transform.getMatrixValue(), points.getMatrixValue())
        );

        // Compute transpose
        MatrixComputeNode transposed = new MatrixComputeNode("transposed", 2, 2, () ->
            MatrixOps.transpose(transformed.getMatrixValue())
        );

        // Build and evaluate graph
        GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();
        builder.addNode(transform);
        builder.addNode(points);
        builder.addNode(transformed);
        builder.addNode(transposed);

        // Define dependencies
        builder.addDependency("transformed", "transform");
        builder.addDependency("transformed", "points");
        builder.addDependency("transposed", "transformed");

        GraphEvaluator evaluator = builder.build();
        evaluator.evaluate();

        System.out.println("Transform matrix:");
        printMatrix(transform.getMatrixValue());
        System.out.println("Points matrix:");
        printMatrix(points.getMatrixValue());
        System.out.println("Transformed matrix:");
        printMatrix(transformed.getMatrixValue());
        System.out.println("Transposed matrix:");
        printMatrix(transposed.getMatrixValue());
    }

    /**
     * Example 3: Combined vector and matrix operations
     */
    private static void combinedExample() {
        System.out.println("--- Combined Vector & Matrix Example ---");

        // Covariance matrix (simplified)
        MatrixInputNode covariance = new MatrixInputNode("covariance",
            new double[][]{{0.04, 0.01}, {0.01, 0.09}}
        );

        // Portfolio weights
        VectorInputNode weights = new VectorInputNode("weights", new double[]{0.6, 0.4});

        // Compute covariance * weights (intermediate step)
        VectorComputeNode covWeights = new VectorComputeNode("covWeights", 2, () ->
            MatrixOps.multiplyVector(covariance.getMatrixValue(), weights.getVectorValue())
        );

        // Compute portfolio variance: weights^T * covariance * weights
        DoubleComputeNode portfolioVariance = new DoubleComputeNode("portfolioVariance", () ->
            VectorOps.dotProduct(weights.getVectorValue(), covWeights.getVectorValue())
        );

        // Compute portfolio volatility (standard deviation)
        DoubleComputeNode portfolioVolatility = new DoubleComputeNode("portfolioVolatility", () ->
            Math.sqrt(portfolioVariance.getDoubleValue())
        );

        // Build and evaluate graph
        GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();
        builder.addNode(covariance);
        builder.addNode(weights);
        builder.addNode(covWeights);
        builder.addNode(portfolioVariance);
        builder.addNode(portfolioVolatility);

        // Define dependencies
        builder.addDependency("covWeights", "covariance");
        builder.addDependency("covWeights", "weights");
        builder.addDependency("portfolioVariance", "weights");
        builder.addDependency("portfolioVariance", "covWeights");
        builder.addDependency("portfolioVolatility", "portfolioVariance");

        GraphEvaluator evaluator = builder.build();
        evaluator.evaluate();

        System.out.println("Covariance matrix:");
        printMatrix(covariance.getMatrixValue());
        System.out.println("Weights: " + Arrays.toString(weights.getVectorValue()));
        System.out.println("Cov * Weights: " + Arrays.toString(covWeights.getVectorValue()));
        System.out.printf("Portfolio Variance: %.6f\n", portfolioVariance.getDoubleValue());
        System.out.printf("Portfolio Volatility: %.6f\n", portfolioVolatility.getDoubleValue());
    }

    private static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            System.out.println("  " + Arrays.toString(row));
        }
    }
}
