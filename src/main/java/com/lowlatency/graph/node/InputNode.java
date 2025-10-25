package com.lowlatency.graph.node;

/**
 * Input node that receives external data.
 *
 * These are leaf nodes in the dependency graph with no incoming edges.
 * Values are set externally via setValue().
 *
 * Type-specialized versions (DoubleInputNode, LongInputNode) can avoid boxing.
 */
public final class InputNode<T> extends AbstractNode<T> {

    private T value;

    public InputNode(String name) {
        super(name);
    }

    public InputNode(String name, T initialValue) {
        super(name);
        this.value = initialValue;
    }

    /**
     * Sets the input value. Call this before graph evaluation.
     * Zero allocation if T is already allocated.
     */
    public void setValue(T value) {
        this.value = value;
        this.dirty = true; // Mark as dirty to trigger downstream recomputation
    }

    @Override
    public void compute() {
        // Input nodes don't compute - they receive values externally
        this.dirty = false;
    }

    @Override
    public T getValue() {
        return value;
    }
}
