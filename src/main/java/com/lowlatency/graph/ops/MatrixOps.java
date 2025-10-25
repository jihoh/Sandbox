package com.lowlatency.graph.ops;

/**
 * Utility class for matrix operations.
 * Matrices are represented as 2D double arrays in row-major order.
 *
 * Design principles:
 * - Cache-friendly access patterns
 * - Input validation for safety
 * - Support for both standard and flat (1D) representations
 * - SIMD-friendly when possible
 */
public final class MatrixOps {

    private MatrixOps() {
        // Utility class
    }

    /**
     * Adds two matrices element-wise.
     * Returns a new matrix C = A + B.
     */
    public static double[][] add(double[][] a, double[][] b) {
        validateSameDimension(a, b, "add");
        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] + b[i][j];
            }
        }
        return result;
    }

    /**
     * Subtracts two matrices element-wise.
     * Returns a new matrix C = A - B.
     */
    public static double[][] subtract(double[][] a, double[][] b) {
        validateSameDimension(a, b, "subtract");
        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] - b[i][j];
            }
        }
        return result;
    }

    /**
     * Multiplies two matrices element-wise (Hadamard product).
     * Returns a new matrix C = A * B (element-wise).
     */
    public static double[][] multiplyElementwise(double[][] a, double[][] b) {
        validateSameDimension(a, b, "multiplyElementwise");
        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] * b[i][j];
            }
        }
        return result;
    }

    /**
     * Multiplies two matrices using standard matrix multiplication.
     * Returns a new matrix C = A × B.
     * A must be m×n, B must be n×p, result will be m×p.
     */
    public static double[][] multiply(double[][] a, double[][] b) {
        validateMatrixMultiplication(a, b);
        int m = a.length;
        int n = a[0].length;
        int p = b[0].length;
        double[][] result = new double[m][p];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += a[i][k] * b[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    /**
     * Multiplies a matrix by a vector.
     * Returns a new vector y = A × x.
     * A must be m×n, x must have length n, result will have length m.
     */
    public static double[] multiplyVector(double[][] a, double[] x) {
        if (a.length == 0 || a[0].length == 0) {
            throw new IllegalArgumentException("Matrix cannot be empty");
        }
        if (a[0].length != x.length) {
            throw new IllegalArgumentException(
                "Matrix-vector multiplication dimension mismatch: " +
                "matrix has " + a[0].length + " columns, vector has " + x.length + " elements"
            );
        }

        int m = a.length;
        int n = a[0].length;
        double[] result = new double[m];

        for (int i = 0; i < m; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                sum += a[i][j] * x[j];
            }
            result[i] = sum;
        }
        return result;
    }

    /**
     * Scales a matrix by a scalar.
     * Returns a new matrix C = scalar × A.
     */
    public static double[][] scale(double[][] a, double scalar) {
        int rows = a.length;
        if (rows == 0) return new double[0][0];
        int cols = a[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] * scalar;
            }
        }
        return result;
    }

    /**
     * Transposes a matrix.
     * Returns a new matrix B = A^T.
     */
    public static double[][] transpose(double[][] a) {
        int rows = a.length;
        if (rows == 0) return new double[0][0];
        int cols = a[0].length;
        double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = a[i][j];
            }
        }
        return result;
    }

    /**
     * Creates an identity matrix of size n×n.
     */
    public static double[][] identity(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Matrix size must be non-negative");
        }
        double[][] result = new double[n][n];
        for (int i = 0; i < n; i++) {
            result[i][i] = 1.0;
        }
        return result;
    }

    /**
     * Creates a zero matrix of size rows×cols.
     */
    public static double[][] zeros(int rows, int cols) {
        if (rows < 0 || cols < 0) {
            throw new IllegalArgumentException("Matrix dimensions must be non-negative");
        }
        return new double[rows][cols];
    }

    /**
     * Creates a matrix filled with a specific value.
     */
    public static double[][] fill(int rows, int cols, double value) {
        if (rows < 0 || cols < 0) {
            throw new IllegalArgumentException("Matrix dimensions must be non-negative");
        }
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = value;
            }
        }
        return result;
    }

    /**
     * Creates a copy of a matrix.
     */
    public static double[][] copy(double[][] a) {
        int rows = a.length;
        if (rows == 0) return new double[0][0];
        int cols = a[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(a[i], 0, result[i], 0, cols);
        }
        return result;
    }

    /**
     * Computes the trace of a square matrix (sum of diagonal elements).
     */
    public static double trace(double[][] a) {
        validateSquare(a, "trace");
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i][i];
        }
        return sum;
    }

    /**
     * Computes the Frobenius norm of a matrix.
     * Returns sqrt(sum of all elements squared).
     */
    public static double frobeniusNorm(double[][] a) {
        double sumSquares = 0.0;
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                sumSquares += a[i][j] * a[i][j];
            }
        }
        return Math.sqrt(sumSquares);
    }

    /**
     * Gets the dimensions of a matrix as [rows, cols].
     */
    public static int[] dimensions(double[][] a) {
        if (a.length == 0) {
            return new int[]{0, 0};
        }
        return new int[]{a.length, a[0].length};
    }

    /**
     * Converts a 2D matrix to a flat 1D array (row-major order).
     */
    public static double[] flatten(double[][] a) {
        int rows = a.length;
        if (rows == 0) return new double[0];
        int cols = a[0].length;
        double[] result = new double[rows * cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(a[i], 0, result, i * cols, cols);
        }
        return result;
    }

    /**
     * Converts a flat 1D array to a 2D matrix (row-major order).
     */
    public static double[][] unflatten(double[] flat, int rows, int cols) {
        if (flat.length != rows * cols) {
            throw new IllegalArgumentException(
                "Array length " + flat.length + " does not match dimensions " + rows + "×" + cols
            );
        }
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(flat, i * cols, result[i], 0, cols);
        }
        return result;
    }

    /**
     * Validates that two matrices have the same dimensions.
     */
    private static void validateSameDimension(double[][] a, double[][] b, String operation) {
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                operation + ": row dimension mismatch - " +
                "a has " + a.length + " rows, b has " + b.length + " rows"
            );
        }
        if (a.length > 0 && b.length > 0 && a[0].length != b[0].length) {
            throw new IllegalArgumentException(
                operation + ": column dimension mismatch - " +
                "a has " + a[0].length + " cols, b has " + b[0].length + " cols"
            );
        }
    }

    /**
     * Validates that matrix dimensions are compatible for multiplication.
     */
    private static void validateMatrixMultiplication(double[][] a, double[][] b) {
        if (a.length == 0 || a[0].length == 0 || b.length == 0 || b[0].length == 0) {
            throw new IllegalArgumentException("Matrices cannot be empty");
        }
        if (a[0].length != b.length) {
            throw new IllegalArgumentException(
                "Matrix multiplication dimension mismatch: " +
                "a has " + a[0].length + " columns, b has " + b.length + " rows"
            );
        }
    }

    /**
     * Validates that a matrix is square.
     */
    private static void validateSquare(double[][] a, String operation) {
        if (a.length == 0) {
            throw new IllegalArgumentException(operation + ": matrix cannot be empty");
        }
        if (a.length != a[0].length) {
            throw new IllegalArgumentException(
                operation + ": matrix must be square, got " + a.length + "×" + a[0].length
            );
        }
    }
}
