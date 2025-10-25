package com.lowlatency.graph.node;

import java.util.Arrays;

/**
 * Specialized input node for matrix values (2D double arrays stored in row-major order).
 * Optimized for linear algebra computations.
 *
 * Critical for:
 * - Covariance matrices in portfolio optimization
 * - Transformation matrices in geometric computations
 * - Weight matrices in neural networks
 */
public final class MatrixInputNode extends AbstractNode<double[][]> {

    private final int rows;
    private final int cols;
    private double[] value; // Row-major storage for cache efficiency

    public MatrixInputNode(String name, int rows, int cols) {
        super(name);
        this.rows = rows;
        this.cols = cols;
        this.value = new double[rows * cols];
    }

    public MatrixInputNode(String name, double[][] initialValue) {
        super(name);
        this.rows = initialValue.length;
        this.cols = initialValue.length > 0 ? initialValue[0].length : 0;
        this.value = new double[rows * cols];

        // Convert 2D array to row-major 1D array
        for (int i = 0; i < rows; i++) {
            if (initialValue[i].length != cols) {
                throw new IllegalArgumentException("Jagged arrays not supported");
            }
            System.arraycopy(initialValue[i], 0, value, i * cols, cols);
        }
    }

    /**
     * Sets the matrix value from a 2D array.
     */
    public void setValue(double[][] matrix) {
        if (matrix.length != rows) {
            throw new IllegalArgumentException(
                "Matrix dimension mismatch: expected " + rows + " rows, got " + matrix.length
            );
        }
        for (int i = 0; i < rows; i++) {
            if (matrix[i].length != cols) {
                throw new IllegalArgumentException(
                    "Matrix dimension mismatch: expected " + cols + " cols, got " + matrix[i].length
                );
            }
            System.arraycopy(matrix[i], 0, value, i * cols, cols);
        }
        this.dirty = true;
    }

    /**
     * Sets a specific element in the matrix.
     */
    public void setElement(int row, int col, double val) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException(
                "Invalid index: (" + row + ", " + col + ") for matrix of size " + rows + "x" + cols
            );
        }
        this.value[row * cols + col] = val;
        this.dirty = true;
    }

    /**
     * Gets a specific element from the matrix.
     */
    public double getElement(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException(
                "Invalid index: (" + row + ", " + col + ") for matrix of size " + rows + "x" + cols
            );
        }
        return value[row * cols + col];
    }

    /**
     * Gets the internal row-major array for performance.
     * WARNING: Direct modifications will bypass dirty tracking.
     */
    public double[] getMatrixValueFlat() {
        return value;
    }

    /**
     * Gets the matrix as a 2D array (creates a copy).
     */
    public double[][] getMatrixValue() {
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(value, i * cols, result[i], 0, cols);
        }
        return result;
    }

    /**
     * Gets the number of rows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the number of columns.
     */
    public int getCols() {
        return cols;
    }

    @Override
    public void compute() {
        this.dirty = false;
    }

    @Override
    public double[][] getValue() {
        return getMatrixValue();
    }

    @Override
    public String toString() {
        return String.format("%s[id=%d, name=%s, size=%dx%d]",
                getClass().getSimpleName(), nodeId, name, rows, cols);
    }
}
