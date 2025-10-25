package com.lowlatency.graph.node;

/**
 * Specialized input node for double values.
 * Avoids boxing overhead for numeric computations.
 *
 * Critical for:
 * - Pricing calculations
 * - Signal generation
 * - Market data processing
 */
public final class DoubleInputNode extends AbstractNode<Double> {

    private double value;

    public DoubleInputNode(String name) {
        super(name);
        this.value = 0.0;
    }

    public DoubleInputNode(String name, double initialValue) {
        super(name);
        this.value = initialValue;
    }

    /**
     * Sets the input value. Zero allocation.
     */
    public void setValue(double value) {
        this.value = value;
        this.dirty = true;
    }

    /**
     * Gets the primitive value. Inlined for performance.
     */
    public double getDoubleValue() {
        return value;
    }

    @Override
    public void compute() {
        this.dirty = false;
    }

    @Override
    public Double getValue() {
        return value; // Boxing only when needed
    }
}
