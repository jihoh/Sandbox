package com.lowlatency.graph.ops;

/**
 * Utility class for vector operations.
 * Provides zero-allocation operations where possible.
 *
 * Design principles:
 * - Cache-friendly sequential access
 * - Input validation for safety
 * - Zero allocation for in-place operations
 * - SIMD-friendly access patterns
 */
public final class VectorOps {

    private VectorOps() {
        // Utility class
    }

    /**
     * Adds two vectors element-wise.
     * Returns a new vector c = a + b.
     */
    public static double[] add(double[] a, double[] b) {
        validateSameDimension(a, b, "add");
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    /**
     * Adds two vectors element-wise, storing result in dest.
     * Zero allocation version of add.
     */
    public static void addInPlace(double[] a, double[] b, double[] dest) {
        validateSameDimension(a, b, "addInPlace");
        validateSameDimension(a, dest, "addInPlace");
        for (int i = 0; i < a.length; i++) {
            dest[i] = a[i] + b[i];
        }
    }

    /**
     * Subtracts two vectors element-wise.
     * Returns a new vector c = a - b.
     */
    public static double[] subtract(double[] a, double[] b) {
        validateSameDimension(a, b, "subtract");
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    /**
     * Subtracts two vectors element-wise, storing result in dest.
     * Zero allocation version of subtract.
     */
    public static void subtractInPlace(double[] a, double[] b, double[] dest) {
        validateSameDimension(a, b, "subtractInPlace");
        validateSameDimension(a, dest, "subtractInPlace");
        for (int i = 0; i < a.length; i++) {
            dest[i] = a[i] - b[i];
        }
    }

    /**
     * Multiplies two vectors element-wise (Hadamard product).
     * Returns a new vector c = a * b.
     */
    public static double[] multiply(double[] a, double[] b) {
        validateSameDimension(a, b, "multiply");
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * b[i];
        }
        return result;
    }

    /**
     * Scales a vector by a scalar.
     * Returns a new vector c = scalar * a.
     */
    public static double[] scale(double[] a, double scalar) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * scalar;
        }
        return result;
    }

    /**
     * Scales a vector by a scalar, storing result in dest.
     * Zero allocation version of scale.
     */
    public static void scaleInPlace(double[] a, double scalar, double[] dest) {
        validateSameDimension(a, dest, "scaleInPlace");
        for (int i = 0; i < a.length; i++) {
            dest[i] = a[i] * scalar;
        }
    }

    /**
     * Computes the dot product of two vectors.
     * Returns sum(a[i] * b[i]).
     */
    public static double dotProduct(double[] a, double[] b) {
        validateSameDimension(a, b, "dotProduct");
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    /**
     * Computes the L2 norm (Euclidean length) of a vector.
     * Returns sqrt(sum(a[i]^2)).
     */
    public static double norm(double[] a) {
        double sumSquares = 0.0;
        for (int i = 0; i < a.length; i++) {
            sumSquares += a[i] * a[i];
        }
        return Math.sqrt(sumSquares);
    }

    /**
     * Computes the squared L2 norm of a vector.
     * Returns sum(a[i]^2).
     * Faster than norm() as it avoids the sqrt operation.
     */
    public static double normSquared(double[] a) {
        double sumSquares = 0.0;
        for (int i = 0; i < a.length; i++) {
            sumSquares += a[i] * a[i];
        }
        return sumSquares;
    }

    /**
     * Normalizes a vector to unit length.
     * Returns a new vector a / ||a||.
     */
    public static double[] normalize(double[] a) {
        double norm = norm(a);
        if (norm == 0.0) {
            throw new ArithmeticException("Cannot normalize zero vector");
        }
        return scale(a, 1.0 / norm);
    }

    /**
     * Computes the sum of all elements in a vector.
     */
    public static double sum(double[] a) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i];
        }
        return sum;
    }

    /**
     * Computes the mean of all elements in a vector.
     */
    public static double mean(double[] a) {
        if (a.length == 0) {
            throw new IllegalArgumentException("Cannot compute mean of empty vector");
        }
        return sum(a) / a.length;
    }

    /**
     * Computes the maximum element in a vector.
     */
    public static double max(double[] a) {
        if (a.length == 0) {
            throw new IllegalArgumentException("Cannot compute max of empty vector");
        }
        double max = a[0];
        for (int i = 1; i < a.length; i++) {
            if (a[i] > max) {
                max = a[i];
            }
        }
        return max;
    }

    /**
     * Computes the minimum element in a vector.
     */
    public static double min(double[] a) {
        if (a.length == 0) {
            throw new IllegalArgumentException("Cannot compute min of empty vector");
        }
        double min = a[0];
        for (int i = 1; i < a.length; i++) {
            if (a[i] < min) {
                min = a[i];
            }
        }
        return min;
    }

    /**
     * Creates a copy of a vector.
     */
    public static double[] copy(double[] a) {
        double[] result = new double[a.length];
        System.arraycopy(a, 0, result, 0, a.length);
        return result;
    }

    /**
     * Validates that two vectors have the same dimension.
     */
    private static void validateSameDimension(double[] a, double[] b, String operation) {
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                operation + ": dimension mismatch - " +
                "a.length=" + a.length + ", b.length=" + b.length
            );
        }
    }
}
