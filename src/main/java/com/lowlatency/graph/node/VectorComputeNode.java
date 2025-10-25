package com.lowlatency.graph.node;

import java.util.Arrays;

/**
 * Specialized computation node for vector values (double arrays).
 * Optimized for linear algebra computations.
 *
 * Example usage:
 * <pre>
 * VectorInputNode v1 = new VectorInputNode("v1", new double[]{1, 2, 3});
 * VectorInputNode v2 = new VectorInputNode("v2", new double[]{4, 5, 6});
 * VectorComputeNode sum = new VectorComputeNode("sum", 3,
 *     () -> {
 *         double[] result = new double[3];
 *         double[] a = v1.getVectorValue();
 *         double[] b = v2.getVectorValue();
 *         for (int i = 0; i < 3; i++) {
 *             result[i] = a[i] + b[i];
 *         }
 *         return result;
 *     }
 * );
 * </pre>
 */
public final class VectorComputeNode extends AbstractNode<double[]> {

    private final VectorComputeFunction function;
    private double[] value;

    public VectorComputeNode(String name, int dimension, VectorComputeFunction function) {
        super(name);
        this.function = function;
        this.value = new double[dimension];
    }

    @Override
    public void compute() {
        double[] result = function.compute();
        if (result.length != value.length) {
            throw new IllegalStateException(
                "Vector dimension mismatch in computation: expected " + value.length +
                ", got " + result.length
            );
        }
        System.arraycopy(result, 0, value, 0, value.length);
        this.dirty = false;
    }

    /**
     * Gets the vector value. Returns the internal array for performance.
     */
    public double[] getVectorValue() {
        return value;
    }

    /**
     * Gets the dimension of the vector.
     */
    public int getDimension() {
        return value.length;
    }

    @Override
    public double[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s[id=%d, name=%s, dim=%d, value=%s]",
                getClass().getSimpleName(), nodeId, name, value.length,
                Arrays.toString(value));
    }

    /**
     * Functional interface for vector computation.
     * Returns a new double array with the computation result.
     */
    @FunctionalInterface
    public interface VectorComputeFunction {
        double[] compute();
    }
}
