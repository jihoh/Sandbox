package com.lowlatency.graph.disruptor;

import java.util.Arrays;

/**
 * Simple latency tracker with percentile calculation.
 *
 * Uses a ring buffer to store recent measurements.
 * Lock-free single-producer implementation.
 */
public final class SimpleLatencyTracker implements LatencyTracker {

    private final long[] measurements;
    private final int capacity;
    private int index = 0;
    private int count = 0;

    private long sum = 0;
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;

    public SimpleLatencyTracker(int capacity) {
        this.capacity = capacity;
        this.measurements = new long[capacity];
    }

    @Override
    public void record(long latencyNanos) {
        measurements[index] = latencyNanos;
        index = (index + 1) % capacity;

        if (count < capacity) {
            count++;
        }

        sum += latencyNanos;
        min = Math.min(min, latencyNanos);
        max = Math.max(max, latencyNanos);
    }

    @Override
    public double getAverageNanos() {
        return count == 0 ? 0.0 : (double) sum / count;
    }

    @Override
    public long getMinNanos() {
        return min == Long.MAX_VALUE ? 0 : min;
    }

    @Override
    public long getMaxNanos() {
        return max == Long.MIN_VALUE ? 0 : max;
    }

    @Override
    public long getPercentile(double percentile) {
        if (count == 0) {
            return 0;
        }

        // Copy and sort measurements
        long[] sorted = new long[count];
        System.arraycopy(measurements, 0, sorted, 0, count);
        Arrays.sort(sorted);

        int index = (int) Math.ceil(percentile * count / 100.0) - 1;
        index = Math.max(0, Math.min(index, count - 1));

        return sorted[index];
    }

    @Override
    public void reset() {
        index = 0;
        count = 0;
        sum = 0;
        min = Long.MAX_VALUE;
        max = Long.MIN_VALUE;
        Arrays.fill(measurements, 0);
    }

    public int getCount() {
        return count;
    }
}
