package com.lowlatency.graph.hybrid.metrics;

import java.util.Arrays;

/**
 * High-performance latency tracker with histogram and percentile calculations.
 *
 * Design:
 * - Fixed-size circular buffer for recent latencies
 * - Zero allocation after initialization
 * - Fast percentile calculation via sorting
 * - Thread-safe: use one tracker per thread
 *
 * Use case: Track end-to-end latency of graph evaluations
 */
public final class LatencyTracker {

    private final long[] latencies;
    private int writeIndex;
    private int count;
    private final int capacity;

    // Running statistics
    private long totalNanos;
    private long minNanos;
    private long maxNanos;

    /**
     * Creates a tracker with specified capacity.
     *
     * @param capacity max number of recent latencies to track
     */
    public LatencyTracker(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        this.latencies = new long[capacity];
        this.writeIndex = 0;
        this.count = 0;
        this.totalNanos = 0;
        this.minNanos = Long.MAX_VALUE;
        this.maxNanos = Long.MIN_VALUE;
    }

    /**
     * Records a latency measurement in nanoseconds.
     * Zero-allocation method.
     */
    public void record(long latencyNanos) {
        if (latencyNanos < 0) {
            return; // Ignore invalid measurements
        }

        latencies[writeIndex] = latencyNanos;
        writeIndex = (writeIndex + 1) % capacity;

        if (count < capacity) {
            count++;
        }

        totalNanos += latencyNanos;
        minNanos = Math.min(minNanos, latencyNanos);
        maxNanos = Math.max(maxNanos, latencyNanos);
    }

    /**
     * Returns the average latency in nanoseconds.
     */
    public double getAverageNanos() {
        return count == 0 ? 0.0 : (double) totalNanos / count;
    }

    /**
     * Returns the minimum latency in nanoseconds.
     */
    public long getMinNanos() {
        return count == 0 ? 0 : minNanos;
    }

    /**
     * Returns the maximum latency in nanoseconds.
     */
    public long getMaxNanos() {
        return count == 0 ? 0 : maxNanos;
    }

    /**
     * Calculates a percentile value.
     *
     * @param percentile value between 0 and 100 (e.g., 95 for P95)
     * @return latency at the given percentile in nanoseconds
     */
    public long getPercentile(double percentile) {
        if (count == 0) {
            return 0;
        }

        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("Percentile must be between 0 and 100");
        }

        // Copy active portion of buffer
        long[] sorted = new long[count];
        if (count == capacity) {
            System.arraycopy(latencies, 0, sorted, 0, count);
        } else {
            System.arraycopy(latencies, 0, sorted, 0, count);
        }

        Arrays.sort(sorted);

        int index = (int) Math.ceil((percentile / 100.0) * count) - 1;
        index = Math.max(0, Math.min(index, count - 1));

        return sorted[index];
    }

    /**
     * Returns P50 (median) latency.
     */
    public long getP50() {
        return getPercentile(50.0);
    }

    /**
     * Returns P95 latency.
     */
    public long getP95() {
        return getPercentile(95.0);
    }

    /**
     * Returns P99 latency.
     */
    public long getP99() {
        return getPercentile(99.0);
    }

    /**
     * Returns P99.9 latency.
     */
    public long getP999() {
        return getPercentile(99.9);
    }

    /**
     * Returns the number of measurements recorded.
     */
    public int getCount() {
        return count;
    }

    /**
     * Resets all statistics.
     */
    public void reset() {
        writeIndex = 0;
        count = 0;
        totalNanos = 0;
        minNanos = Long.MAX_VALUE;
        maxNanos = Long.MIN_VALUE;
        Arrays.fill(latencies, 0);
    }

    /**
     * Prints detailed statistics.
     */
    public void printStats() {
        if (count == 0) {
            System.out.println("No latency data recorded");
            return;
        }

        System.out.println("=== Latency Statistics ===");
        System.out.printf("Samples:    %,d%n", count);
        System.out.printf("Average:    %,.2f µs%n", getAverageNanos() / 1000.0);
        System.out.printf("Min:        %,.2f µs%n", getMinNanos() / 1000.0);
        System.out.printf("Max:        %,.2f µs%n", getMaxNanos() / 1000.0);
        System.out.printf("P50:        %,.2f µs%n", getP50() / 1000.0);
        System.out.printf("P95:        %,.2f µs%n", getP95() / 1000.0);
        System.out.printf("P99:        %,.2f µs%n", getP99() / 1000.0);
        System.out.printf("P99.9:      %,.2f µs%n", getP999() / 1000.0);
    }

    /**
     * Returns a formatted summary string.
     */
    @Override
    public String toString() {
        return String.format("LatencyTracker[count=%d, avg=%.2fµs, p50=%.2fµs, p95=%.2fµs, p99=%.2fµs]",
            count,
            getAverageNanos() / 1000.0,
            getP50() / 1000.0,
            getP95() / 1000.0,
            getP99() / 1000.0);
    }
}
