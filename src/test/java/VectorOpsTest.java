import com.lowlatency.graph.ops.VectorOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VectorOpsTest {

    private static final double EPSILON = 1e-10;

    @Test
    public void testAdd() {
        double[] a = {1, 2, 3};
        double[] b = {4, 5, 6};
        double[] result = VectorOps.add(a, b);

        assertArrayEquals(new double[]{5, 7, 9}, result, EPSILON);
    }

    @Test
    public void testAddDimensionMismatch() {
        double[] a = {1, 2, 3};
        double[] b = {4, 5};

        assertThrows(IllegalArgumentException.class, () -> VectorOps.add(a, b));
    }

    @Test
    public void testSubtract() {
        double[] a = {5, 7, 9};
        double[] b = {1, 2, 3};
        double[] result = VectorOps.subtract(a, b);

        assertArrayEquals(new double[]{4, 5, 6}, result, EPSILON);
    }

    @Test
    public void testMultiply() {
        double[] a = {2, 3, 4};
        double[] b = {5, 6, 7};
        double[] result = VectorOps.multiply(a, b);

        assertArrayEquals(new double[]{10, 18, 28}, result, EPSILON);
    }

    @Test
    public void testScale() {
        double[] a = {1, 2, 3};
        double[] result = VectorOps.scale(a, 2.0);

        assertArrayEquals(new double[]{2, 4, 6}, result, EPSILON);
    }

    @Test
    public void testDotProduct() {
        double[] a = {1, 2, 3};
        double[] b = {4, 5, 6};
        double result = VectorOps.dotProduct(a, b);

        // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        assertEquals(32.0, result, EPSILON);
    }

    @Test
    public void testNorm() {
        double[] a = {3, 4};
        double result = VectorOps.norm(a);

        // sqrt(3^2 + 4^2) = sqrt(9 + 16) = sqrt(25) = 5
        assertEquals(5.0, result, EPSILON);
    }

    @Test
    public void testNormSquared() {
        double[] a = {3, 4};
        double result = VectorOps.normSquared(a);

        // 3^2 + 4^2 = 9 + 16 = 25
        assertEquals(25.0, result, EPSILON);
    }

    @Test
    public void testNormalize() {
        double[] a = {3, 4};
        double[] result = VectorOps.normalize(a);

        assertArrayEquals(new double[]{0.6, 0.8}, result, EPSILON);
        assertEquals(1.0, VectorOps.norm(result), EPSILON);
    }

    @Test
    public void testNormalizeZeroVector() {
        double[] a = {0, 0, 0};

        assertThrows(ArithmeticException.class, () -> VectorOps.normalize(a));
    }

    @Test
    public void testSum() {
        double[] a = {1, 2, 3, 4};
        double result = VectorOps.sum(a);

        assertEquals(10.0, result, EPSILON);
    }

    @Test
    public void testMean() {
        double[] a = {1, 2, 3, 4};
        double result = VectorOps.mean(a);

        assertEquals(2.5, result, EPSILON);
    }

    @Test
    public void testMax() {
        double[] a = {1, 5, 3, 2};
        double result = VectorOps.max(a);

        assertEquals(5.0, result, EPSILON);
    }

    @Test
    public void testMin() {
        double[] a = {1, 5, 3, 2};
        double result = VectorOps.min(a);

        assertEquals(1.0, result, EPSILON);
    }

    @Test
    public void testCopy() {
        double[] a = {1, 2, 3};
        double[] result = VectorOps.copy(a);

        assertArrayEquals(a, result, EPSILON);
        assertNotSame(a, result); // Ensure it's a different array
    }

    @Test
    public void testAddInPlace() {
        double[] a = {1, 2, 3};
        double[] b = {4, 5, 6};
        double[] dest = new double[3];

        VectorOps.addInPlace(a, b, dest);

        assertArrayEquals(new double[]{5, 7, 9}, dest, EPSILON);
    }

    @Test
    public void testScaleInPlace() {
        double[] a = {1, 2, 3};
        double[] dest = new double[3];

        VectorOps.scaleInPlace(a, 2.0, dest);

        assertArrayEquals(new double[]{2, 4, 6}, dest, EPSILON);
    }
}
