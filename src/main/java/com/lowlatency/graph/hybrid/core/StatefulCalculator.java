package com.lowlatency.graph.hybrid.core;

/**
 * Extension of Calculator that supports stateful computations.
 *
 * Stateful calculators maintain internal state across evaluations, such as:
 * - Historical values (for moving averages, momentum indicators)
 * - Accumulators (for running sums, counts)
 * - Previous results (for delta calculations)
 *
 * Design principles:
 * - State is encapsulated within the calculator instance
 * - Each node gets its own calculator instance (no shared state)
 * - reset() method allows clearing state
 * - Still zero-allocation in the compute() hot path
 *
 * Example use cases:
 * - Simple Moving Average (SMA)
 * - Exponential Moving Average (EMA)
 * - Rate of change
 * - Running statistics (min, max, stddev)
 */
public interface StatefulCalculator extends Calculator {

    /**
     * Resets the internal state of this calculator.
     * Called when the graph needs to be reinitialized.
     */
    void reset();

    /**
     * Returns whether this calculator has accumulated enough data
     * to produce valid results.
     *
     * For example, an SMA(10) needs 10 data points before it can
     * produce a valid average.
     *
     * @return true if the calculator is ready to produce valid results
     */
    default boolean isReady() {
        return true;
    }

    /**
     * Returns the number of evaluations this calculator has seen.
     * Useful for warmup logic.
     */
    default int getEvaluationCount() {
        return 0;
    }
}
