import com.lowlatency.graph.node.MatrixInputNode;
import com.lowlatency.graph.node.MatrixComputeNode;
import com.lowlatency.graph.ops.MatrixOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MatrixNodeTest {

    private static final double EPSILON = 1e-10;

    @Test
    public void testMatrixInputNode() {
        MatrixInputNode node = new MatrixInputNode("m1", 2, 3);

        assertEquals("m1", node.getName());
        assertEquals(2, node.getRows());
        assertEquals(3, node.getCols());

        double[][] value = node.getMatrixValue();
        assertEquals(2, value.length);
        assertEquals(3, value[0].length);
    }

    @Test
    public void testMatrixInputNodeWithInitialValue() {
        double[][] initial = {{1, 2, 3}, {4, 5, 6}};
        MatrixInputNode node = new MatrixInputNode("m1", initial);

        assertEquals(2, node.getRows());
        assertEquals(3, node.getCols());

        double[][] value = node.getMatrixValue();
        assertArrayEquals(new double[]{1, 2, 3}, value[0], EPSILON);
        assertArrayEquals(new double[]{4, 5, 6}, value[1], EPSILON);
    }

    @Test
    public void testMatrixInputNodeSetValue() {
        MatrixInputNode node = new MatrixInputNode("m1", 2, 2);
        assertTrue(node.isDirty());

        node.compute();
        assertFalse(node.isDirty());

        double[][] newValue = {{1, 2}, {3, 4}};
        node.setValue(newValue);
        assertTrue(node.isDirty());

        double[][] value = node.getMatrixValue();
        assertArrayEquals(new double[]{1, 2}, value[0], EPSILON);
        assertArrayEquals(new double[]{3, 4}, value[1], EPSILON);
    }

    @Test
    public void testMatrixInputNodeSetElement() {
        double[][] initial = {{1, 2}, {3, 4}};
        MatrixInputNode node = new MatrixInputNode("m1", initial);
        node.compute();
        assertFalse(node.isDirty());

        node.setElement(0, 1, 10);
        assertTrue(node.isDirty());
        assertEquals(10.0, node.getElement(0, 1), EPSILON);
    }

    @Test
    public void testMatrixInputNodeGetElement() {
        double[][] initial = {{1, 2, 3}, {4, 5, 6}};
        MatrixInputNode node = new MatrixInputNode("m1", initial);

        assertEquals(1.0, node.getElement(0, 0), EPSILON);
        assertEquals(5.0, node.getElement(1, 1), EPSILON);
        assertEquals(6.0, node.getElement(1, 2), EPSILON);
    }

    @Test
    public void testMatrixInputNodeDimensionMismatch() {
        MatrixInputNode node = new MatrixInputNode("m1", 2, 2);

        assertThrows(IllegalArgumentException.class, () ->
            node.setValue(new double[][]{{1, 2, 3}})
        );
    }

    @Test
    public void testMatrixInputNodeJaggedArray() {
        double[][] jagged = {{1, 2}, {3, 4, 5}};

        assertThrows(IllegalArgumentException.class, () ->
            new MatrixInputNode("m1", jagged)
        );
    }

    @Test
    public void testMatrixComputeNode() {
        MatrixInputNode m1 = new MatrixInputNode("m1", new double[][]{{1, 2}, {3, 4}});
        MatrixInputNode m2 = new MatrixInputNode("m2", new double[][]{{5, 6}, {7, 8}});

        MatrixComputeNode sum = new MatrixComputeNode("sum", 2, 2, () ->
            MatrixOps.add(m1.getMatrixValue(), m2.getMatrixValue())
        );

        sum.compute();
        double[][] result = sum.getMatrixValue();
        assertArrayEquals(new double[]{6, 8}, result[0], EPSILON);
        assertArrayEquals(new double[]{10, 12}, result[1], EPSILON);
    }

    @Test
    public void testMatrixComputeNodeMultiplication() {
        MatrixInputNode m1 = new MatrixInputNode("m1", new double[][]{{1, 2}, {3, 4}});
        MatrixInputNode m2 = new MatrixInputNode("m2", new double[][]{{5, 6}, {7, 8}});

        MatrixComputeNode product = new MatrixComputeNode("product", 2, 2, () ->
            MatrixOps.multiply(m1.getMatrixValue(), m2.getMatrixValue())
        );

        product.compute();
        double[][] result = product.getMatrixValue();
        assertArrayEquals(new double[]{19, 22}, result[0], EPSILON);
        assertArrayEquals(new double[]{43, 50}, result[1], EPSILON);
    }

    @Test
    public void testMatrixComputeNodeTranspose() {
        MatrixInputNode m1 = new MatrixInputNode("m1", new double[][]{{1, 2, 3}, {4, 5, 6}});

        MatrixComputeNode transposed = new MatrixComputeNode("transposed", 3, 2, () ->
            MatrixOps.transpose(m1.getMatrixValue())
        );

        transposed.compute();
        double[][] result = transposed.getMatrixValue();
        assertEquals(3, result.length);
        assertEquals(2, result[0].length);
        assertArrayEquals(new double[]{1, 4}, result[0], EPSILON);
        assertArrayEquals(new double[]{2, 5}, result[1], EPSILON);
        assertArrayEquals(new double[]{3, 6}, result[2], EPSILON);
    }

    @Test
    public void testMatrixComputeNodeDirtyFlag() {
        MatrixInputNode m1 = new MatrixInputNode("m1", new double[][]{{1, 2}, {3, 4}});
        MatrixComputeNode scaled = new MatrixComputeNode("scaled", 2, 2, () ->
            MatrixOps.scale(m1.getMatrixValue(), 2.0)
        );

        assertTrue(scaled.isDirty());
        scaled.compute();
        assertFalse(scaled.isDirty());

        m1.setValue(new double[][]{{2, 3}, {4, 5}});
        scaled.markDirty();
        assertTrue(scaled.isDirty());
    }

    @Test
    public void testMatrixComputeNodeDimensionMismatch() {
        MatrixInputNode m1 = new MatrixInputNode("m1", new double[][]{{1, 2, 3}, {4, 5, 6}});

        MatrixComputeNode node = new MatrixComputeNode("bad", 2, 2, () ->
            m1.getMatrixValue() // Returns 2x3 but node expects 2x2
        );

        assertThrows(IllegalStateException.class, () -> node.compute());
    }

    @Test
    public void testMatrixComputeNodeGetElement() {
        MatrixInputNode m1 = new MatrixInputNode("m1", new double[][]{{1, 2}, {3, 4}});
        MatrixComputeNode scaled = new MatrixComputeNode("scaled", 2, 2, () ->
            MatrixOps.scale(m1.getMatrixValue(), 2.0)
        );

        scaled.compute();
        assertEquals(2.0, scaled.getElement(0, 0), EPSILON);
        assertEquals(4.0, scaled.getElement(0, 1), EPSILON);
        assertEquals(6.0, scaled.getElement(1, 0), EPSILON);
        assertEquals(8.0, scaled.getElement(1, 1), EPSILON);
    }
}
