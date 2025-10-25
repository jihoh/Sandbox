package com.lowlatency.graph.hybrid.calculators;

import com.lowlatency.graph.hybrid.core.HybridCompiledGraph;
import com.lowlatency.graph.hybrid.core.StatefulCalculator;

/**
 * Simple Moving Average (SMA) calculator with configurable lookback period.
 *
 * Design:
 * - Uses circular buffer for O(1) updates
 * - Zero allocation in compute() hot path
 * - Maintains running sum for O(1) average calculation
 * - Thread-safe: each node instance gets its own calculator
 *
 * Mathematical formula:
 *   SMA(n) = (x₁ + x₂ + ... + xₙ) / n
 *
 * Performance:
 * - Time complexity: O(1) per compute
 * - Space complexity: O(lookback)
 * - No allocations after construction
 *
 * Example:
 * SMA(3) with inputs [1, 2, 3, 4, 5]:
 *   - After 1st value: 1.0 (only 1 value)
 *   - After 2nd value: 1.5 (avg of 1, 2)
 *   - After 3rd value: 2.0 (avg of 1, 2, 3)
 *   - After 4th value: 3.0 (avg of 2, 3, 4)
 *   - After 5th value: 4.0 (avg of 3, 4, 5)
 */
public final class SMACalculator implements StatefulCalculator {

    private final int lookback;
    private final double[] buffer;
    private int writeIndex;
    private int count;
    private double sum;

    /**
     * Creates an SMA calculator with the specified lookback period.
     *
     * @param lookback number of historical values to average (must be > 0)
     * @throws IllegalArgumentException if lookback <= 0
     */
    public SMACalculator(int lookback) {
        if (lookback <= 0) {
            throw new IllegalArgumentException("Lookback must be positive, got: " + lookback);
        }
        this.lookback = lookback;
        this.buffer = new double[lookback];
        this.writeIndex = 0;
        this.count = 0;
        this.sum = 0.0;
    }

    @Override
    public double compute(int nodeId, HybridCompiledGraph graph) {
        // Get the current input value
        // For SMA, we expect exactly one parent node (the value to average)
        int parentStart = graph.getParentStartIndex(nodeId);
        double currentValue = graph.values[graph.parentValues[parentStart]];

        // Update circular buffer
        if (count == lookback) {
            // Buffer is full - remove oldest value from sum
            sum -= buffer[writeIndex];
        }

        // Add new value to buffer and sum
        buffer[writeIndex] = currentValue;
        sum += currentValue;

        // Update write index (circular)
        writeIndex = (writeIndex + 1) % lookback;

        // Update count (capped at lookback)
        if (count < lookback) {
            count++;
        }

        // Calculate average
        // Note: During warmup (count < lookback), we compute average of available values
        return sum / count;
    }

    @Override
    public void reset() {
        writeIndex = 0;
        count = 0;
        sum = 0.0;
        // Clear buffer (optional, but helps with debugging)
        for (int i = 0; i < lookback; i++) {
            buffer[i] = 0.0;
        }
    }

    @Override
    public boolean isReady() {
        return count >= lookback;
    }

    @Override
    public int getEvaluationCount() {
        return count;
    }

    /**
     * Returns the configured lookback period.
     */
    public int getLookback() {
        return lookback;
    }

    /**
     * Returns the current number of values in the buffer.
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the current sum (for debugging).
     */
    public double getSum() {
        return sum;
    }

    @Override
    public String getDescription() {
        return String.format("SMA(%d)", lookback);
    }

    @Override
    public boolean isDeterministic() {
        // SMA is deterministic, but stateful
        return true;
    }

    @Override
    public String toString() {
        return String.format("SMACalculator[lookback=%d, count=%d, ready=%b]",
            lookback, count, isReady());
    }
}
