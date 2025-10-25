package com.lowlatency.graph.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.lowlatency.graph.evaluator.GraphEvaluator;

/**
 * High-performance graph processor using LMAX Disruptor.
 *
 * Features:
 * 1. Lock-free event processing
 * 2. Pre-allocated ring buffer (zero allocation in hot path)
 * 3. Mechanical sympathy - cache-line padding, etc.
 * 4. Configurable wait strategies for latency tuning
 *
 * Typical latency: 50-500 nanoseconds for simple graphs
 * Target: < 1 millisecond for complex graphs
 *
 * Thread model:
 * - Producer thread: publishes events to ring buffer
 * - Consumer thread: processes events and evaluates graph
 */
public final class DisruptorGraphProcessor {

    private final Disruptor<GraphEvent> disruptor;
    private final RingBuffer<GraphEvent> ringBuffer;
    private final GraphEventHandler eventHandler;
    private final LatencyTracker latencyTracker;

    /**
     * Creates a processor with default configuration.
     *
     * @param evaluator the graph evaluator
     * @param bufferSize ring buffer size (must be power of 2)
     */
    public DisruptorGraphProcessor(GraphEvaluator evaluator, int bufferSize) {
        this(evaluator, bufferSize, true);
    }

    /**
     * Creates a processor with latency tracking option.
     *
     * @param evaluator the graph evaluator
     * @param bufferSize ring buffer size (must be power of 2)
     * @param enableLatencyTracking whether to track latency
     */
    public DisruptorGraphProcessor(GraphEvaluator evaluator, int bufferSize, boolean enableLatencyTracking) {
        if (Integer.bitCount(bufferSize) != 1) {
            throw new IllegalArgumentException("Buffer size must be a power of 2");
        }

        // Create latency tracker if enabled
        this.latencyTracker = enableLatencyTracking ? new SimpleLatencyTracker(10000) : null;

        // Create event handler
        this.eventHandler = new GraphEventHandler(evaluator, latencyTracker);

        // Create disruptor
        // YieldingWaitStrategy: low latency, high CPU usage
        // For ultra-low latency, consider BusySpinWaitStrategy
        this.disruptor = new Disruptor<>(
                GraphEvent::new,
                bufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,  // Single producer for best performance
                new YieldingWaitStrategy()
        );

        // Set up event handler
        disruptor.handleEventsWith(eventHandler);

        // Start the disruptor
        this.ringBuffer = disruptor.start();
    }

    /**
     * Publishes a single value update event.
     * Zero allocation method for hot path.
     *
     * @param nodeId the input node ID to update
     * @param value the new value
     */
    public void publishUpdate(int nodeId, double value) {
        long sequence = ringBuffer.next();
        try {
            GraphEvent event = ringBuffer.get(sequence);
            event.clear();
            event.setType(GraphEvent.EventType.SINGLE_UPDATE);
            event.setNodeId(nodeId);
            event.setDoubleValue(value);
            event.setTimestamp(System.nanoTime());
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    /**
     * Publishes a batch update event.
     *
     * @param nodeIds array of node IDs
     * @param values array of values
     */
    public void publishBatchUpdate(int[] nodeIds, double[] values) {
        long sequence = ringBuffer.next();
        try {
            GraphEvent event = ringBuffer.get(sequence);
            event.clear();
            event.setType(GraphEvent.EventType.BATCH_UPDATE);
            event.setNodeIds(nodeIds);
            event.setDoubleValues(values);
            event.setTimestamp(System.nanoTime());
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    /**
     * Publishes an evaluate graph event.
     */
    public void publishEvaluate() {
        long sequence = ringBuffer.next();
        try {
            GraphEvent event = ringBuffer.get(sequence);
            event.clear();
            event.setType(GraphEvent.EventType.EVALUATE_GRAPH);
            event.setTimestamp(System.nanoTime());
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    /**
     * Returns the latency tracker if enabled.
     */
    public LatencyTracker getLatencyTracker() {
        return latencyTracker;
    }

    /**
     * Returns the event handler for statistics.
     */
    public GraphEventHandler getEventHandler() {
        return eventHandler;
    }

    /**
     * Returns the ring buffer.
     */
    public RingBuffer<GraphEvent> getRingBuffer() {
        return ringBuffer;
    }

    /**
     * Shuts down the processor.
     * Waits for all events to be processed.
     */
    public void shutdown() {
        disruptor.shutdown();
    }

    /**
     * Gets the remaining capacity in the ring buffer.
     */
    public long getRemainingCapacity() {
        return ringBuffer.remainingCapacity();
    }

    /**
     * Prints statistics to stdout.
     */
    public void printStats() {
        System.out.println("=== Graph Processor Statistics ===");
        System.out.printf("Events processed: %d%n", eventHandler.getEventCount());
        System.out.printf("Avg evaluation time: %.2f µs%n", eventHandler.getAverageEvaluationMicros());

        if (latencyTracker != null && latencyTracker instanceof SimpleLatencyTracker) {
            SimpleLatencyTracker tracker = (SimpleLatencyTracker) latencyTracker;
            System.out.printf("End-to-end latency:%n");
            System.out.printf("  Average: %.2f µs%n", tracker.getAverageNanos() / 1000.0);
            System.out.printf("  Min: %.2f µs%n", tracker.getMinNanos() / 1000.0);
            System.out.printf("  Max: %.2f µs%n", tracker.getMaxNanos() / 1000.0);
            System.out.printf("  P50: %.2f µs%n", tracker.getPercentile(50) / 1000.0);
            System.out.printf("  P95: %.2f µs%n", tracker.getPercentile(95) / 1000.0);
            System.out.printf("  P99: %.2f µs%n", tracker.getPercentile(99) / 1000.0);
            System.out.printf("  P99.9: %.2f µs%n", tracker.getPercentile(99.9) / 1000.0);
        }
    }
}
