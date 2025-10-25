import com.lowlatency.graph.node.VectorInputNode;
import com.lowlatency.graph.node.VectorComputeNode;
import com.lowlatency.graph.ops.VectorOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VectorNodeTest {

    private static final double EPSILON = 1e-10;

    @Test
    public void testVectorInputNode() {
        VectorInputNode node = new VectorInputNode("v1", 3);

        assertEquals("v1", node.getName());
        assertEquals(3, node.getDimension());
        assertArrayEquals(new double[]{0, 0, 0}, node.getVectorValue(), EPSILON);
    }

    @Test
    public void testVectorInputNodeWithInitialValue() {
        double[] initial = {1, 2, 3};
        VectorInputNode node = new VectorInputNode("v1", initial);

        assertEquals(3, node.getDimension());
        assertArrayEquals(new double[]{1, 2, 3}, node.getVectorValue(), EPSILON);
    }

    @Test
    public void testVectorInputNodeSetValue() {
        VectorInputNode node = new VectorInputNode("v1", 3);
        assertTrue(node.isDirty());

        node.compute();
        assertFalse(node.isDirty());

        node.setValue(new double[]{4, 5, 6});
        assertTrue(node.isDirty());
        assertArrayEquals(new double[]{4, 5, 6}, node.getVectorValue(), EPSILON);
    }

    @Test
    public void testVectorInputNodeSetElement() {
        VectorInputNode node = new VectorInputNode("v1", new double[]{1, 2, 3});
        node.compute();
        assertFalse(node.isDirty());

        node.setElement(1, 10);
        assertTrue(node.isDirty());
        assertArrayEquals(new double[]{1, 10, 3}, node.getVectorValue(), EPSILON);
    }

    @Test
    public void testVectorInputNodeDimensionMismatch() {
        VectorInputNode node = new VectorInputNode("v1", 3);

        assertThrows(IllegalArgumentException.class, () -> node.setValue(new double[]{1, 2}));
    }

    @Test
    public void testVectorComputeNode() {
        VectorInputNode v1 = new VectorInputNode("v1", new double[]{1, 2, 3});
        VectorInputNode v2 = new VectorInputNode("v2", new double[]{4, 5, 6});

        VectorComputeNode sum = new VectorComputeNode("sum", 3, () ->
            VectorOps.add(v1.getVectorValue(), v2.getVectorValue())
        );

        sum.compute();
        assertArrayEquals(new double[]{5, 7, 9}, sum.getVectorValue(), EPSILON);
    }

    @Test
    public void testVectorComputeNodeChaining() {
        VectorInputNode v1 = new VectorInputNode("v1", new double[]{1, 2, 3});
        VectorInputNode v2 = new VectorInputNode("v2", new double[]{4, 5, 6});

        VectorComputeNode sum = new VectorComputeNode("sum", 3, () ->
            VectorOps.add(v1.getVectorValue(), v2.getVectorValue())
        );

        VectorComputeNode scaled = new VectorComputeNode("scaled", 3, () ->
            VectorOps.scale(sum.getVectorValue(), 2.0)
        );

        sum.compute();
        scaled.compute();

        assertArrayEquals(new double[]{10, 14, 18}, scaled.getVectorValue(), EPSILON);
    }

    @Test
    public void testVectorComputeNodeDirtyFlag() {
        VectorInputNode v1 = new VectorInputNode("v1", new double[]{1, 2, 3});
        VectorComputeNode scaled = new VectorComputeNode("scaled", 3, () ->
            VectorOps.scale(v1.getVectorValue(), 2.0)
        );

        assertTrue(scaled.isDirty());
        scaled.compute();
        assertFalse(scaled.isDirty());

        v1.setValue(new double[]{2, 3, 4});
        // Note: In a full graph evaluator, the dirty flag would propagate
        scaled.markDirty();
        assertTrue(scaled.isDirty());
    }

    @Test
    public void testVectorComputeNodeDimensionMismatch() {
        VectorInputNode v1 = new VectorInputNode("v1", new double[]{1, 2, 3});

        VectorComputeNode node = new VectorComputeNode("bad", 2, () ->
            v1.getVectorValue() // Returns 3 elements but node expects 2
        );

        assertThrows(IllegalStateException.class, () -> node.compute());
    }
}
