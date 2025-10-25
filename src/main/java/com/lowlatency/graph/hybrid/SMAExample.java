package com.lowlatency.graph.hybrid;

import com.lowlatency.graph.hybrid.calculators.SMACalculator;
import com.lowlatency.graph.hybrid.compiler.CalculatorRegistry;
import com.lowlatency.graph.hybrid.compiler.GraphBuilder;
import com.lowlatency.graph.hybrid.core.HybridCompiledGraph;
import com.lowlatency.graph.hybrid.evaluator.HybridGraphEvaluator;

/**
 * Example demonstrating Simple Moving Average (SMA) support with configurable lookback.
 *
 * This example shows:
 * 1. How to register stateful calculators (SMA with different lookback periods)
 * 2. How SMA maintains state across evaluations
 * 3. Comparing different SMA windows (fast vs slow moving averages)
 * 4. Real-world time-series analysis use case
 */
public class SMAExample {

    public static void main(String[] args) {
        System.out.println("=== SMA (Simple Moving Average) Example ===\n");

        // Create registry with standard operations
        CalculatorRegistry registry = CalculatorRegistry.createStandard();

        // Register SMA operations with different lookback periods
        // Each node using these will get its own SMACalculator instance
        registry.registerFixed("SMA3", 1, () -> new SMACalculator(3), "3-period SMA", true);
        registry.registerFixed("SMA5", 1, () -> new SMACalculator(5), "5-period SMA", true);
        registry.registerFixed("SMA10", 1, () -> new SMACalculator(10), "10-period SMA", true);

        // Build a graph with price input and multiple SMA windows
        HybridCompiledGraph graph = new GraphBuilder()
            .addInput("price", 100.0)
            .addCompute("sma3", "SMA3", "price")      // Fast MA
            .addCompute("sma5", "SMA5", "price")      // Medium MA
            .addCompute("sma10", "SMA10", "price")    // Slow MA
            .addCompute("spread_fast_slow", "SUB", "sma3", "sma10")  // Spread indicator
            .compile(registry);

        // Create evaluator in FULL mode (stateful calculators need all nodes evaluated)
        HybridGraphEvaluator evaluator = new HybridGraphEvaluator(
            graph, HybridGraphEvaluator.EvaluationMode.FULL);

        // Simulate a price series (simple uptrend then downtrend)
        double[] prices = {
            100.0, 102.0, 105.0, 103.0, 107.0,  // Uptrend
            110.0, 112.0, 111.0, 109.0, 106.0,  // Peak + start of downtrend
            104.0, 102.0, 100.0, 98.0, 96.0     // Downtrend
        };

        System.out.println("Price series with 3 different SMA windows:");
        System.out.println("-".repeat(80));
        System.out.printf("%-5s | %-8s | %-10s | %-10s | %-10s | %-10s%n",
            "Step", "Price", "SMA(3)", "SMA(5)", "SMA(10)", "Spread");
        System.out.println("-".repeat(80));

        for (int i = 0; i < prices.length; i++) {
            // Update price and evaluate
            evaluator.setInput("price", prices[i]);
            evaluator.evaluate();

            // Get results
            double price = graph.getValue("price");
            double sma3 = graph.getValue("sma3");
            double sma5 = graph.getValue("sma5");
            double sma10 = graph.getValue("sma10");
            double spread = graph.getValue("spread_fast_slow");

            System.out.printf("%-5d | %8.2f | %10.2f | %10.2f | %10.2f | %10.2f%n",
                i + 1, price, sma3, sma5, sma10, spread);
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("\nObservations:");
        System.out.println("1. SMAs smooth out price fluctuations");
        System.out.println("2. Shorter windows (SMA3) react faster to price changes");
        System.out.println("3. Longer windows (SMA10) are smoother but lag more");
        System.out.println("4. Spread (SMA3 - SMA10) can signal trend strength");
        System.out.println("   - Positive spread: uptrend");
        System.out.println("   - Negative spread: downtrend");
        System.out.println("   - Crossover: potential trend reversal");

        // Demonstrate trading signal
        System.out.println("\n" + "=".repeat(80));
        System.out.println("\nSimple Trading Signal Example:");
        System.out.println("Strategy: Buy when fast SMA crosses above slow SMA, sell when it crosses below");
        System.out.println();

        demonstrateTradingSignals(registry);
    }

    private static void demonstrateTradingSignals(CalculatorRegistry registry) {
        // Build a simple crossover strategy graph
        HybridCompiledGraph graph = new GraphBuilder()
            .addInput("price", 100.0)
            .addCompute("fast_sma", "SMA3", "price")
            .addCompute("slow_sma", "SMA10", "price")
            .addCompute("spread", "SUB", "fast_sma", "slow_sma")
            .compile(registry);

        HybridGraphEvaluator evaluator = new HybridGraphEvaluator(
            graph, HybridGraphEvaluator.EvaluationMode.FULL);

        // Price series designed to show crossover
        double[] prices = {
            100, 99, 98, 97, 96,    // Downtrend
            95, 94, 93, 94, 95,     // Bottom
            97, 100, 103, 106, 109  // Uptrend (crossover happens here)
        };

        double prevSpread = 0;
        for (int i = 0; i < prices.length; i++) {
            evaluator.setInput("price", prices[i]);
            evaluator.evaluate();

            double spread = graph.getValue("spread");

            // Detect crossover
            if (i > 0) {
                if (prevSpread <= 0 && spread > 0) {
                    System.out.printf("Day %2d: 🔔 BUY SIGNAL! Fast SMA crossed above slow SMA (spread: %.2f)%n",
                        i + 1, spread);
                } else if (prevSpread >= 0 && spread < 0) {
                    System.out.printf("Day %2d: 🔔 SELL SIGNAL! Fast SMA crossed below slow SMA (spread: %.2f)%n",
                        i + 1, spread);
                }
            }

            prevSpread = spread;
        }

        System.out.println("\nThis demonstrates how SMA can be used for trend-following strategies!");
    }
}
