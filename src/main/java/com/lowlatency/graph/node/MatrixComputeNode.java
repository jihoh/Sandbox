package com.lowlatency.graph.node;

/**
 * Specialized computation node for matrix values (2D double arrays stored in row-major order).
 * Optimized for linear algebra computations.
 *
 * Example usage:
 * <pre>
 * MatrixInputNode m1 = new MatrixInputNode("m1", new double[][]{{1, 2}, {3, 4}});
 * MatrixInputNode m2 = new MatrixInputNode("m2", new double[][]{{5, 6}, {7, 8}});
 * MatrixComputeNode sum = new MatrixComputeNode("sum", 2, 2,
 *     () -> {
 *         double[][] a = m1.getMatrixValue();
 *         double[][] b = m2.getMatrixValue();
 *         double[][] result = new double[2][2];
 *         for (int i = 0; i < 2; i++) {
 *             for (int j = 0; j < 2; j++) {
 *                 result[i][j] = a[i][j] + b[i][j];
 *             }
 *         }
 *         return result;
 *     }
 * );
 * </pre>
 */
public final class MatrixComputeNode extends AbstractNode<double[][]> {

    private final MatrixComputeFunction function;
    private final int rows;
    private final int cols;
    private double[] value; // Row-major storage for cache efficiency

    public MatrixComputeNode(String name, int rows, int cols, MatrixComputeFunction function) {
        super(name);
        this.function = function;
        this.rows = rows;
        this.cols = cols;
        this.value = new double[rows * cols];
    }

    @Override
    public void compute() {
        double[][] result = function.compute();
        if (result.length != rows) {
            throw new IllegalStateException(
                "Matrix dimension mismatch in computation: expected " + rows + " rows, got " + result.length
            );
        }
        for (int i = 0; i < rows; i++) {
            if (result[i].length != cols) {
                throw new IllegalStateException(
                    "Matrix dimension mismatch in computation: expected " + cols + " cols, got " + result[i].length
                );
            }
            System.arraycopy(result[i], 0, value, i * cols, cols);
        }
        this.dirty = false;
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
    public double[][] getValue() {
        return getMatrixValue();
    }

    @Override
    public String toString() {
        return String.format("%s[id=%d, name=%s, size=%dx%d]",
                getClass().getSimpleName(), nodeId, name, rows, cols);
    }

    /**
     * Functional interface for matrix computation.
     * Returns a new 2D double array with the computation result.
     */
    @FunctionalInterface
    public interface MatrixComputeFunction {
        double[][] compute();
    }
}
