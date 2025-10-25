import com.lowlatency.graph.ops.MatrixOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MatrixOpsTest {

    private static final double EPSILON = 1e-10;

    @Test
    public void testAdd() {
        double[][] a = {{1, 2}, {3, 4}};
        double[][] b = {{5, 6}, {7, 8}};
        double[][] result = MatrixOps.add(a, b);

        assertArrayEquals(new double[]{6, 8}, result[0], EPSILON);
        assertArrayEquals(new double[]{10, 12}, result[1], EPSILON);
    }

    @Test
    public void testAddDimensionMismatch() {
        double[][] a = {{1, 2}, {3, 4}};
        double[][] b = {{5, 6, 7}};

        assertThrows(IllegalArgumentException.class, () -> MatrixOps.add(a, b));
    }

    @Test
    public void testSubtract() {
        double[][] a = {{5, 6}, {7, 8}};
        double[][] b = {{1, 2}, {3, 4}};
        double[][] result = MatrixOps.subtract(a, b);

        assertArrayEquals(new double[]{4, 4}, result[0], EPSILON);
        assertArrayEquals(new double[]{4, 4}, result[1], EPSILON);
    }

    @Test
    public void testMultiplyElementwise() {
        double[][] a = {{1, 2}, {3, 4}};
        double[][] b = {{5, 6}, {7, 8}};
        double[][] result = MatrixOps.multiplyElementwise(a, b);

        assertArrayEquals(new double[]{5, 12}, result[0], EPSILON);
        assertArrayEquals(new double[]{21, 32}, result[1], EPSILON);
    }

    @Test
    public void testMultiply() {
        double[][] a = {{1, 2}, {3, 4}};
        double[][] b = {{5, 6}, {7, 8}};
        double[][] result = MatrixOps.multiply(a, b);

        // [1*5 + 2*7, 1*6 + 2*8] = [19, 22]
        // [3*5 + 4*7, 3*6 + 4*8] = [43, 50]
        assertArrayEquals(new double[]{19, 22}, result[0], EPSILON);
        assertArrayEquals(new double[]{43, 50}, result[1], EPSILON);
    }

    @Test
    public void testMultiplyDimensionMismatch() {
        double[][] a = {{1, 2, 3}};
        double[][] b = {{4, 5}};

        assertThrows(IllegalArgumentException.class, () -> MatrixOps.multiply(a, b));
    }

    @Test
    public void testMultiplyVector() {
        double[][] a = {{1, 2, 3}, {4, 5, 6}};
        double[] x = {7, 8, 9};
        double[] result = MatrixOps.multiplyVector(a, x);

        // [1*7 + 2*8 + 3*9, 4*7 + 5*8 + 6*9] = [7+16+27, 28+40+54] = [50, 122]
        assertArrayEquals(new double[]{50, 122}, result, EPSILON);
    }

    @Test
    public void testScale() {
        double[][] a = {{1, 2}, {3, 4}};
        double[][] result = MatrixOps.scale(a, 2.0);

        assertArrayEquals(new double[]{2, 4}, result[0], EPSILON);
        assertArrayEquals(new double[]{6, 8}, result[1], EPSILON);
    }

    @Test
    public void testTranspose() {
        double[][] a = {{1, 2, 3}, {4, 5, 6}};
        double[][] result = MatrixOps.transpose(a);

        assertEquals(3, result.length);
        assertEquals(2, result[0].length);
        assertArrayEquals(new double[]{1, 4}, result[0], EPSILON);
        assertArrayEquals(new double[]{2, 5}, result[1], EPSILON);
        assertArrayEquals(new double[]{3, 6}, result[2], EPSILON);
    }

    @Test
    public void testIdentity() {
        double[][] result = MatrixOps.identity(3);

        assertArrayEquals(new double[]{1, 0, 0}, result[0], EPSILON);
        assertArrayEquals(new double[]{0, 1, 0}, result[1], EPSILON);
        assertArrayEquals(new double[]{0, 0, 1}, result[2], EPSILON);
    }

    @Test
    public void testZeros() {
        double[][] result = MatrixOps.zeros(2, 3);

        assertEquals(2, result.length);
        assertEquals(3, result[0].length);
        assertArrayEquals(new double[]{0, 0, 0}, result[0], EPSILON);
        assertArrayEquals(new double[]{0, 0, 0}, result[1], EPSILON);
    }

    @Test
    public void testFill() {
        double[][] result = MatrixOps.fill(2, 2, 5.0);

        assertArrayEquals(new double[]{5, 5}, result[0], EPSILON);
        assertArrayEquals(new double[]{5, 5}, result[1], EPSILON);
    }

    @Test
    public void testCopy() {
        double[][] a = {{1, 2}, {3, 4}};
        double[][] result = MatrixOps.copy(a);

        assertArrayEquals(a[0], result[0], EPSILON);
        assertArrayEquals(a[1], result[1], EPSILON);
        assertNotSame(a, result);
        assertNotSame(a[0], result[0]);
    }

    @Test
    public void testTrace() {
        double[][] a = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        double result = MatrixOps.trace(a);

        // 1 + 5 + 9 = 15
        assertEquals(15.0, result, EPSILON);
    }

    @Test
    public void testTraceNonSquare() {
        double[][] a = {{1, 2, 3}, {4, 5, 6}};

        assertThrows(IllegalArgumentException.class, () -> MatrixOps.trace(a));
    }

    @Test
    public void testFrobeniusNorm() {
        double[][] a = {{3, 4}};
        double result = MatrixOps.frobeniusNorm(a);

        // sqrt(3^2 + 4^2) = sqrt(9 + 16) = 5
        assertEquals(5.0, result, EPSILON);
    }

    @Test
    public void testDimensions() {
        double[][] a = {{1, 2, 3}, {4, 5, 6}};
        int[] dims = MatrixOps.dimensions(a);

        assertArrayEquals(new int[]{2, 3}, dims);
    }

    @Test
    public void testFlattenAndUnflatten() {
        double[][] a = {{1, 2, 3}, {4, 5, 6}};
        double[] flat = MatrixOps.flatten(a);

        assertArrayEquals(new double[]{1, 2, 3, 4, 5, 6}, flat, EPSILON);

        double[][] unflattened = MatrixOps.unflatten(flat, 2, 3);
        assertArrayEquals(a[0], unflattened[0], EPSILON);
        assertArrayEquals(a[1], unflattened[1], EPSILON);
    }

    @Test
    public void testUnflattenDimensionMismatch() {
        double[] flat = {1, 2, 3, 4, 5};

        assertThrows(IllegalArgumentException.class, () -> MatrixOps.unflatten(flat, 2, 3));
    }
}
