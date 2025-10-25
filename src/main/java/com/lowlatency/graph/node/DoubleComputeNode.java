package com.lowlatency.graph.node;

/**
 * Specialized computation node for double values.
 * Avoids boxing overhead for numeric computations.
 *
 * Example usage:
 * <pre>
 * DoubleInputNode price = new DoubleInputNode("price");
 * DoubleInputNode quantity = new DoubleInputNode("quantity");
 * DoubleComputeNode notional = new DoubleComputeNode("notional",
 *     () -> price.getDoubleValue() * quantity.getDoubleValue()
 * );
 * </pre>
 */
public final class DoubleComputeNode extends AbstractNode<Double> {

    private final DoubleComputeFunction function;
    private double value;

    public DoubleComputeNode(String name, DoubleComputeFunction function) {
        super(name);
        this.function = function;
    }

    @Override
    public void compute() {
        this.value = function.compute();
        this.dirty = false;
    }

    /**
     * Gets the primitive value. Inlined for performance.
     */
    public double getDoubleValue() {
        return value;
    }

    @Override
    public Double getValue() {
        return value; // Boxing only when needed
    }

    /**
     * Functional interface for double computation.
     * Zero allocation design.
     */
    @FunctionalInterface
    public interface DoubleComputeFunction {
        double compute();
    }
}
