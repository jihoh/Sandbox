package com.lowlatency.graph.node;

/**
 * Generic computation node with arbitrary logic.
 *
 * Uses a functional interface for computation, allowing flexible node definitions.
 * For maximum performance, consider creating specialized subclasses with
 * final compute() methods for monomorphic call sites.
 */
public final class ComputeNode<T> extends AbstractNode<T> {

    private final ComputeFunction<T> function;
    private T value;

    public ComputeNode(String name, ComputeFunction<T> function) {
        super(name);
        this.function = function;
    }

    @Override
    public void compute() {
        this.value = function.compute();
        this.dirty = false;
    }

    @Override
    public T getValue() {
        return value;
    }

    /**
     * Functional interface for node computation.
     * Should be zero-allocation in the compute() method.
     */
    @FunctionalInterface
    public interface ComputeFunction<T> {
        T compute();
    }
}
