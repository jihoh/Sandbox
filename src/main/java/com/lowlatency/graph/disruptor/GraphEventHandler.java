package com.lowlatency.graph.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lowlatency.graph.evaluator.GraphEvaluator;
import com.lowlatency.graph.node.DoubleInputNode;
import com.lowlatency.graph.node.InputNode;
import com.lowlatency.graph.node.Node;

/**
 * Event handler for processing graph events via LMAX Disruptor.
 *
 * This handler:
 * 1. Receives events from the ring buffer
 * 2. Updates input nodes
 * 3. Triggers graph evaluation
 * 4. Tracks latency metrics
 *
 * Zero-allocation design in onEvent() for ultra-low latency.
 */
public final class GraphEventHandler implements EventHandler<GraphEvent> {

    private final GraphEvaluator evaluator;
    private final LatencyTracker latencyTracker;

    // Statistics
    private long eventCount = 0;
    private long totalEvaluationNanos = 0;

    public GraphEventHandler(GraphEvaluator evaluator) {
        this(evaluator, null);
    }

    public GraphEventHandler(GraphEvaluator evaluator, LatencyTracker latencyTracker) {
        this.evaluator = evaluator;
        this.latencyTracker = latencyTracker;
    }

    @Override
    public void onEvent(GraphEvent event, long sequence, boolean endOfBatch) throws Exception {
        final long startNanos = System.nanoTime();

        switch (event.getType()) {
            case SINGLE_UPDATE:
                handleSingleUpdate(event);
                break;

            case BATCH_UPDATE:
                handleBatchUpdate(event);
                break;

            case EVALUATE_GRAPH:
                evaluator.evaluate();
                break;

            case SHUTDOWN:
                // Handle shutdown if needed
                break;

            default:
                throw new IllegalArgumentException("Unknown event type: " + event.getType());
        }

        // Track latency
        final long endNanos = System.nanoTime();
        final long latencyNanos = endNanos - startNanos;

        eventCount++;
        totalEvaluationNanos += latencyNanos;

        if (latencyTracker != null) {
            long eventTimestamp = event.getTimestamp();
            if (eventTimestamp > 0) {
                long totalLatencyNanos = endNanos - eventTimestamp;
                latencyTracker.record(totalLatencyNanos);
            }
        }
    }

    private void handleSingleUpdate(GraphEvent event) {
        int nodeId = event.getNodeId();
        Node<?> node = evaluator.getNode(nodeId);

        // Update the input node based on type
        if (node instanceof DoubleInputNode) {
            ((DoubleInputNode) node).setValue(event.getDoubleValue());
        } else if (node instanceof InputNode) {
            @SuppressWarnings("unchecked")
            InputNode<Object> inputNode = (InputNode<Object>) node;
            inputNode.setValue(event.getObjectValue());
        }

        // Evaluate the graph
        evaluator.evaluate();
    }

    private void handleBatchUpdate(GraphEvent event) {
        int[] nodeIds = event.getNodeIds();
        double[] values = event.getDoubleValues();

        if (nodeIds == null || values == null) {
            throw new IllegalArgumentException("Batch update requires nodeIds and values");
        }

        if (nodeIds.length != values.length) {
            throw new IllegalArgumentException("nodeIds and values length mismatch");
        }

        // Update all input nodes
        for (int i = 0; i < nodeIds.length; i++) {
            int nodeId = nodeIds[i];
            Node<?> node = evaluator.getNode(nodeId);

            if (node instanceof DoubleInputNode) {
                ((DoubleInputNode) node).setValue(values[i]);
            }
        }

        // Evaluate the graph once after all updates
        evaluator.evaluate();
    }

    /**
     * Returns the number of events processed.
     */
    public long getEventCount() {
        return eventCount;
    }

    /**
     * Returns the average evaluation time in nanoseconds.
     */
    public double getAverageEvaluationNanos() {
        return eventCount == 0 ? 0.0 : (double) totalEvaluationNanos / eventCount;
    }

    /**
     * Returns the average evaluation time in microseconds.
     */
    public double getAverageEvaluationMicros() {
        return getAverageEvaluationNanos() / 1000.0;
    }

    /**
     * Resets statistics.
     */
    public void resetStats() {
        eventCount = 0;
        totalEvaluationNanos = 0;
    }
}
