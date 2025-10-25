package com.lowlatency.graph.ops;

/**
 * Cache-efficient matrix operations using flat (1D) arrays in row-major order.
 *
 * WHY USE THIS?
 * - 2D arrays (double[][]) require pointer indirection and are cache-inefficient
 * - Flat arrays provide sequential memory access, perfect for CPU cache
 * - Zero allocation for in-place operations
 * - SIMD-friendly access patterns
 *
 * USAGE:
 * Instead of: double[][] matrix = new double[rows][cols];
 * Use:        double[] matrix = new double[rows * cols];  // row-major
 *
 * Access element at (i,j): matrix[i * cols + j]
 *
 * PERFORMANCE GAINS:
 * - Up to 5-10x faster for matrix multiplication
 * - Better cache locality (fewer cache misses)
 * - No pointer chasing overhead
 */
public final class MatrixOpsFlat {

    private MatrixOpsFlat() {
        // Utility class
    }

    /**
     * Adds two matrices element-wise (cache-efficient).
     * dest = a + b
     *
     * All arrays in row-major order, zero allocation.
     */
    public static void add(double[] a, double[] b, double[] dest, int rows, int cols) {
        int length = rows * cols;
        validate(a, length, "a");
        validate(b, length, "b");
        validate(dest, length, "dest");

        for (int i = 0; i < length; i++) {
            dest[i] = a[i] + b[i];
        }
    }

    /**
     * Subtracts two matrices element-wise (cache-efficient).
     * dest = a - b
     */
    public static void subtract(double[] a, double[] b, double[] dest, int rows, int cols) {
        int length = rows * cols;
        validate(a, length, "a");
        validate(b, length, "b");
        validate(dest, length, "dest");

        for (int i = 0; i < length; i++) {
            dest[i] = a[i] - b[i];
        }
    }

    /**
     * Scales a matrix by a scalar (cache-efficient).
     * dest = scalar * a
     */
    public static void scale(double[] a, double scalar, double[] dest, int rows, int cols) {
        int length = rows * cols;
        validate(a, length, "a");
        validate(dest, length, "dest");

        for (int i = 0; i < length; i++) {
            dest[i] = a[i] * scalar;
        }
    }

    /**
     * Multiplies two matrices (cache-efficient, ikj order).
     * dest = a × b
     *
     * a: m×n, b: n×p, dest: m×p
     * All in row-major order.
     *
     * Uses ikj loop order for optimal cache utilization:
     * - Inner loop accesses b sequentially
     * - Minimizes cache misses
     */
    public static void multiply(double[] a, double[] b, double[] dest,
                                 int m, int n, int p) {
        validate(a, m * n, "a");
        validate(b, n * p, "b");
        validate(dest, m * p, "dest");

        // Cache-friendly matrix multiplication (ikj order)
        for (int i = 0; i < m; i++) {
            int destRowStart = i * p;
            int aRowStart = i * n;

            // Initialize result row to zero
            for (int j = 0; j < p; j++) {
                dest[destRowStart + j] = 0.0;
            }

            // Middle loop: columns of a / rows of b
            for (int k = 0; k < n; k++) {
                double aik = a[aRowStart + k];
                int bRowStart = k * p;

                // Inner loop: columns of b (cache-friendly sequential access)
                for (int j = 0; j < p; j++) {
                    dest[destRowStart + j] += aik * b[bRowStart + j];
                }
            }
        }
    }

    /**
     * Transposes a matrix (cache-efficient).
     * dest = a^T
     *
     * a: rows×cols, dest: cols×rows
     */
    public static void transpose(double[] a, double[] dest, int rows, int cols) {
        validate(a, rows * cols, "a");
        validate(dest, rows * cols, "dest");

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                dest[j * rows + i] = a[i * cols + j];
            }
        }
    }

    /**
     * Multiplies a matrix by a vector (cache-efficient).
     * dest = a × x
     *
     * a: m×n (flat), x: vector of length n, dest: vector of length m
     */
    public static void multiplyVector(double[] a, double[] x, double[] dest, int m, int n) {
        validate(a, m * n, "a");
        validate(x, n, "x");
        validate(dest, m, "dest");

        for (int i = 0; i < m; i++) {
            double sum = 0.0;
            int rowStart = i * n;
            for (int j = 0; j < n; j++) {
                sum += a[rowStart + j] * x[j];
            }
            dest[i] = sum;
        }
    }

    /**
     * Computes the trace of a square matrix (sum of diagonal elements).
     */
    public static double trace(double[] a, int n) {
        validate(a, n * n, "a");
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += a[i * n + i];
        }
        return sum;
    }

    /**
     * Computes the Frobenius norm of a matrix.
     * Returns sqrt(sum of all elements squared).
     */
    public static double frobeniusNorm(double[] a, int rows, int cols) {
        int length = rows * cols;
        validate(a, length, "a");

        double sumSquares = 0.0;
        for (int i = 0; i < length; i++) {
            sumSquares += a[i] * a[i];
        }
        return Math.sqrt(sumSquares);
    }

    /**
     * Sets a matrix to the identity matrix.
     */
    public static void setIdentity(double[] dest, int n) {
        validate(dest, n * n, "dest");

        for (int i = 0; i < n * n; i++) {
            dest[i] = 0.0;
        }
        for (int i = 0; i < n; i++) {
            dest[i * n + i] = 1.0;
        }
    }

    /**
     * Fills a matrix with a specific value.
     */
    public static void fill(double[] dest, double value, int rows, int cols) {
        int length = rows * cols;
        validate(dest, length, "dest");

        for (int i = 0; i < length; i++) {
            dest[i] = value;
        }
    }

    /**
     * Copies a matrix.
     */
    public static void copy(double[] src, double[] dest, int rows, int cols) {
        int length = rows * cols;
        validate(src, length, "src");
        validate(dest, length, "dest");

        System.arraycopy(src, 0, dest, 0, length);
    }

    /**
     * Validates array dimension.
     */
    private static void validate(double[] array, int expectedLength, String name) {
        if (array.length != expectedLength) {
            throw new IllegalArgumentException(
                name + ": expected length " + expectedLength + ", got " + array.length
            );
        }
    }
}
