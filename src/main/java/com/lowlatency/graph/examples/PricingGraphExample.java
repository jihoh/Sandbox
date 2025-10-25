package com.lowlatency.graph.examples;

import com.lowlatency.graph.disruptor.DisruptorGraphProcessor;
import com.lowlatency.graph.evaluator.GraphEvaluator;
import com.lowlatency.graph.evaluator.GraphEvaluatorBuilder;
import com.lowlatency.graph.node.DoubleComputeNode;
import com.lowlatency.graph.node.DoubleInputNode;

/**
 * Example: Low-latency pricing graph for financial instruments.
 *
 * Graph structure:
 *   spotPrice ----\
 *                  \
 *   volatility -----+--> theoreticalPrice --> delta
 *                  /                      \
 *   riskFreeRate -/                        +--> gamma
 *                                          |
 *   timeToExpiry ---------------------+----+
 *
 * This demonstrates:
 * 1. Building a computation graph
 * 2. Using Disruptor for event-driven updates
 * 3. Sub-millisecond latency for pricing calculations
 */
public class PricingGraphExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Low-Latency Pricing Graph Example ===\n");

        // Create input nodes
        DoubleInputNode spotPrice = new DoubleInputNode("spotPrice", 100.0);
        DoubleInputNode volatility = new DoubleInputNode("volatility", 0.2);
        DoubleInputNode riskFreeRate = new DoubleInputNode("riskFreeRate", 0.05);
        DoubleInputNode timeToExpiry = new DoubleInputNode("timeToExpiry", 1.0);
        DoubleInputNode strike = new DoubleInputNode("strike", 105.0);

        // Create computation nodes
        DoubleComputeNode driftAdjustedPrice = new DoubleComputeNode("driftAdjustedPrice",
                () -> spotPrice.getDoubleValue() * Math.exp(riskFreeRate.getDoubleValue() * timeToExpiry.getDoubleValue())
        );

        DoubleComputeNode volatilityTerm = new DoubleComputeNode("volatilityTerm",
                () -> volatility.getDoubleValue() * Math.sqrt(timeToExpiry.getDoubleValue())
        );

        DoubleComputeNode d1 = new DoubleComputeNode("d1", () -> {
            double s = spotPrice.getDoubleValue();
            double k = strike.getDoubleValue();
            double r = riskFreeRate.getDoubleValue();
            double t = timeToExpiry.getDoubleValue();
            double v = volatility.getDoubleValue();
            return (Math.log(s / k) + (r + 0.5 * v * v) * t) / (v * Math.sqrt(t));
        });

        DoubleComputeNode d2 = new DoubleComputeNode("d2",
                () -> d1.getDoubleValue() - volatilityTerm.getDoubleValue()
        );

        // Black-Scholes call option price
        DoubleComputeNode theoreticalPrice = new DoubleComputeNode("theoreticalPrice", () -> {
            double s = spotPrice.getDoubleValue();
            double k = strike.getDoubleValue();
            double r = riskFreeRate.getDoubleValue();
            double t = timeToExpiry.getDoubleValue();
            double d1Val = d1.getDoubleValue();
            double d2Val = d2.getDoubleValue();

            return s * cdf(d1Val) - k * Math.exp(-r * t) * cdf(d2Val);
        });

        // Greeks
        DoubleComputeNode delta = new DoubleComputeNode("delta",
                () -> cdf(d1.getDoubleValue())
        );

        DoubleComputeNode gamma = new DoubleComputeNode("gamma", () -> {
            double s = spotPrice.getDoubleValue();
            double v = volatility.getDoubleValue();
            double t = timeToExpiry.getDoubleValue();
            double d1Val = d1.getDoubleValue();
            return pdf(d1Val) / (s * v * Math.sqrt(t));
        });

        DoubleComputeNode vega = new DoubleComputeNode("vega", () -> {
            double s = spotPrice.getDoubleValue();
            double t = timeToExpiry.getDoubleValue();
            double d1Val = d1.getDoubleValue();
            return s * pdf(d1Val) * Math.sqrt(t);
        });

        // Build the graph
        GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();

        // Add nodes
        int spotId = builder.addNode(spotPrice);
        int volId = builder.addNode(volatility);
        int rateId = builder.addNode(riskFreeRate);
        int timeId = builder.addNode(timeToExpiry);
        int strikeId = builder.addNode(strike);
        int driftId = builder.addNode(driftAdjustedPrice);
        int volTermId = builder.addNode(volatilityTerm);
        int d1Id = builder.addNode(d1);
        int d2Id = builder.addNode(d2);
        int priceId = builder.addNode(theoreticalPrice);
        int deltaId = builder.addNode(delta);
        int gammaId = builder.addNode(gamma);
        int vegaId = builder.addNode(vega);

        // Define dependencies
        builder.addDependency(driftId, spotId);
        builder.addDependency(driftId, rateId);
        builder.addDependency(driftId, timeId);

        builder.addDependency(volTermId, volId);
        builder.addDependency(volTermId, timeId);

        builder.addDependency(d1Id, spotId);
        builder.addDependency(d1Id, strikeId);
        builder.addDependency(d1Id, rateId);
        builder.addDependency(d1Id, timeId);
        builder.addDependency(d1Id, volId);

        builder.addDependency(d2Id, d1Id);
        builder.addDependency(d2Id, volTermId);

        builder.addDependency(priceId, spotId);
        builder.addDependency(priceId, strikeId);
        builder.addDependency(priceId, rateId);
        builder.addDependency(priceId, timeId);
        builder.addDependency(priceId, d1Id);
        builder.addDependency(priceId, d2Id);

        builder.addDependency(deltaId, d1Id);

        builder.addDependency(gammaId, spotId);
        builder.addDependency(gammaId, volId);
        builder.addDependency(gammaId, timeId);
        builder.addDependency(gammaId, d1Id);

        builder.addDependency(vegaId, spotId);
        builder.addDependency(vegaId, timeId);
        builder.addDependency(vegaId, d1Id);

        System.out.println("Building graph...");
        GraphEvaluator evaluator = builder.build();
        System.out.printf("Graph built: %d nodes, %d dependencies%n%n",
                builder.getNumNodes(), builder.getNumDependencies());

        // Create Disruptor processor
        int bufferSize = 1024; // Must be power of 2
        DisruptorGraphProcessor processor = new DisruptorGraphProcessor(evaluator, bufferSize);

        System.out.println("Starting event processing...\n");

        // Warm up the JVM
        System.out.println("Warming up JVM...");
        for (int i = 0; i < 10000; i++) {
            processor.publishUpdate(spotId, 100.0 + i * 0.01);
        }

        // Allow warm-up to complete
        Thread.sleep(100);
        processor.getEventHandler().resetStats();

        // Simulate market data updates
        System.out.println("Processing market updates...");
        int numUpdates = 100000;
        long startTime = System.nanoTime();

        for (int i = 0; i < numUpdates; i++) {
            // Simulate spot price changing
            double newSpot = 100.0 + Math.sin(i * 0.001) * 10.0;
            processor.publishUpdate(spotId, newSpot);

            // Occasionally update volatility
            if (i % 100 == 0) {
                double newVol = 0.2 + Math.cos(i * 0.01) * 0.05;
                processor.publishUpdate(volId, newVol);
            }
        }

        // Wait for processing to complete
        while (processor.getRemainingCapacity() < bufferSize - 1) {
            Thread.sleep(1);
        }

        long endTime = System.nanoTime();
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;

        System.out.printf("Processed %d updates in %.2f ms%n", numUpdates, totalTimeMs);
        System.out.printf("Throughput: %.2f updates/sec%n%n", numUpdates / (totalTimeMs / 1000.0));

        // Print final values
        System.out.println("=== Final Values ===");
        System.out.printf("Spot Price: %.2f%n", spotPrice.getDoubleValue());
        System.out.printf("Theoretical Price: %.4f%n", theoreticalPrice.getDoubleValue());
        System.out.printf("Delta: %.4f%n", delta.getDoubleValue());
        System.out.printf("Gamma: %.6f%n", gamma.getDoubleValue());
        System.out.printf("Vega: %.4f%n%n", vega.getDoubleValue());

        // Print statistics
        processor.printStats();

        // Shutdown
        processor.shutdown();
        System.out.println("\nShutdown complete.");
    }

    // Standard normal CDF approximation
    private static double cdf(double x) {
        return 0.5 * (1.0 + erf(x / Math.sqrt(2.0)));
    }

    // Standard normal PDF
    private static double pdf(double x) {
        return Math.exp(-0.5 * x * x) / Math.sqrt(2.0 * Math.PI);
    }

    // Error function approximation
    private static double erf(double x) {
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;

        int sign = x < 0 ? -1 : 1;
        x = Math.abs(x);

        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

        return sign * y;
    }
}
