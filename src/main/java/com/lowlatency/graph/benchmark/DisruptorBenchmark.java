package com.lowlatency.graph.benchmark;

import com.lowlatency.graph.disruptor.DisruptorGraphProcessor;
import com.lowlatency.graph.evaluator.GraphEvaluator;
import com.lowlatency.graph.evaluator.GraphEvaluatorBuilder;
import com.lowlatency.graph.node.DoubleComputeNode;
import com.lowlatency.graph.node.DoubleInputNode;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark for end-to-end Disruptor-based event processing.
 *
 * Measures: Event publishing + Ring buffer + Graph evaluation
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
public class DisruptorBenchmark {

    @State(Scope.Thread)
    public static class ProcessorState {
        DisruptorGraphProcessor processor;
        DoubleInputNode priceNode;
        int priceId;

        @Setup(Level.Trial)
        public void setup() {
            // Create a small pricing graph
            priceNode = new DoubleInputNode("price", 100.0);
            DoubleInputNode qtyNode = new DoubleInputNode("qty", 1000.0);

            DoubleComputeNode notional = new DoubleComputeNode("notional",
                    () -> priceNode.getDoubleValue() * qtyNode.getDoubleValue());

            DoubleComputeNode commission = new DoubleComputeNode("commission",
                    () -> notional.getDoubleValue() * 0.001);

            DoubleComputeNode total = new DoubleComputeNode("total",
                    () -> notional.getDoubleValue() + commission.getDoubleValue());

            GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();
            priceId = builder.addNode(priceNode);
            int qtyId = builder.addNode(qtyNode);
            int notionalId = builder.addNode(notional);
            int commissionId = builder.addNode(commission);
            builder.addNode(total);

            builder.addDependency(notionalId, priceId);
            builder.addDependency(notionalId, qtyId);
            builder.addDependency(commissionId, notionalId);
            builder.addDependency(4, notionalId);
            builder.addDependency(4, commissionId);

            GraphEvaluator evaluator = builder.build();

            // Create processor with 1024 buffer
            processor = new DisruptorGraphProcessor(evaluator, 1024, false);

            // Warm up
            for (int i = 0; i < 10000; i++) {
                processor.publishUpdate(priceId, 100.0 + i * 0.1);
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @TearDown(Level.Trial)
        public void teardown() {
            processor.shutdown();
        }
    }

    @Benchmark
    public void publishAndProcess(ProcessorState state) {
        state.processor.publishUpdate(state.priceId, 100.0);
        // The event will be processed asynchronously by the Disruptor
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void publishOnly(ProcessorState state) {
        // Measure just the publish latency
        state.processor.publishUpdate(state.priceId, 100.0);
    }
}
