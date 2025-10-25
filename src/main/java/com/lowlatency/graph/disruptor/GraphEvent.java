package com.lowlatency.graph.disruptor;

/**
 * Event object for LMAX Disruptor ring buffer.
 *
 * This is a mutable, reusable event object to avoid allocation.
 * Events are pre-allocated in the ring buffer and reused.
 *
 * Design for zero-allocation:
 * 1. All fields are mutable
 * 2. Use primitive types when possible
 * 3. Complex data passed via object references (avoid copying)
 * 4. clear() method for resetting state
 */
public final class GraphEvent {

    // Event type
    private EventType type;

    // For single value updates
    private int nodeId;
    private double doubleValue;
    private long longValue;
    private Object objectValue;

    // For batch updates
    private int[] nodeIds;
    private double[] doubleValues;

    // Timestamp for latency tracking
    private long timestamp;

    public GraphEvent() {
        // Default constructor required by Disruptor
    }

    public void clear() {
        this.type = null;
        this.nodeId = -1;
        this.doubleValue = 0.0;
        this.longValue = 0L;
        this.objectValue = null;
        this.nodeIds = null;
        this.doubleValues = null;
        this.timestamp = 0L;
    }

    // Getters and setters

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public Object getObjectValue() {
        return objectValue;
    }

    public void setObjectValue(Object objectValue) {
        this.objectValue = objectValue;
    }

    public int[] getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(int[] nodeIds) {
        this.nodeIds = nodeIds;
    }

    public double[] getDoubleValues() {
        return doubleValues;
    }

    public void setDoubleValues(double[] doubleValues) {
        this.doubleValues = doubleValues;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Event types for different operations.
     */
    public enum EventType {
        SINGLE_UPDATE,      // Update single node
        BATCH_UPDATE,       // Update multiple nodes
        EVALUATE_GRAPH,     // Trigger full graph evaluation
        SHUTDOWN            // Shutdown signal
    }
}
