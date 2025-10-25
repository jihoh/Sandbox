package com.lowlatency.graph.disruptor;

/**
 * Interface for tracking latency metrics.
 */
public interface LatencyTracker {

    /**
     * Records a latency measurement in nanoseconds.
     */
    void record(long latencyNanos);

    /**
     * Gets the average latency in nanoseconds.
     */
    double getAverageNanos();

    /**
     * Gets the minimum latency in nanoseconds.
     */
    long getMinNanos();

    /**
     * Gets the maximum latency in nanoseconds.
     */
    long getMaxNanos();

    /**
     * Gets a percentile latency in nanoseconds.
     */
    long getPercentile(double percentile);

    /**
     * Resets all statistics.
     */
    void reset();
}
