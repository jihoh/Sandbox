package com.lowlatency.graph.node;

import java.util.Arrays;

/**
 * Specialized input node for vector values (double arrays).
 * Optimized for linear algebra computations.
 *
 * Critical for:
 * - Feature vectors in ML pipelines
 * - Time series data
 * - Multi-dimensional signals
 */
public final class VectorInputNode extends AbstractNode<double[]> {

    private double[] value;

    public VectorInputNode(String name, int dimension) {
        super(name);
        this.value = new double[dimension];
    }

    public VectorInputNode(String name, double[] initialValue) {
        super(name);
        this.value = Arrays.copyOf(initialValue, initialValue.length);
    }

    /**
     * Sets the input value. Copies the array to prevent external modification.
     */
    public void setValue(double[] value) {
        if (value.length != this.value.length) {
            throw new IllegalArgumentException(
                "Vector dimension mismatch: expected " + this.value.length +
                ", got " + value.length
            );
        }
        System.arraycopy(value, 0, this.value, 0, value.length);
        this.dirty = true;
    }

    /**
     * Sets a specific element in the vector.
     */
    public void setElement(int index, double val) {
        this.value[index] = val;
        this.dirty = true;
    }

    /**
     * Gets the vector value. Returns the internal array for performance.
     * WARNING: Direct modifications will bypass dirty tracking.
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
    public void compute() {
        this.dirty = false;
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
}
