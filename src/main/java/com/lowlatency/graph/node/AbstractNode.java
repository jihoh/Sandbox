package com.lowlatency.graph.node;

/**
 * Abstract base class for nodes providing common functionality.
 *
 * Implements standard node behavior:
 * - Node ID management
 * - Name management
 * - Dirty flag tracking
 *
 * Subclasses only need to implement compute() and getValue().
 */
public abstract class AbstractNode<T> implements Node<T> {

    protected int nodeId;
    protected final String name;
    protected boolean dirty = true;

    protected AbstractNode(String name) {
        this.name = name;
    }

    @Override
    public int getNodeId() {
        return nodeId;
    }

    @Override
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void reset() {
        this.dirty = true;
    }

    @Override
    public String toString() {
        return String.format("%s[id=%d, name=%s]",
                getClass().getSimpleName(), nodeId, name);
    }
}
