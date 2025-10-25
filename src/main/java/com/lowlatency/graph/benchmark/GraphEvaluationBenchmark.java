package com.lowlatency.graph.benchmark;

import com.lowlatency.graph.evaluator.GraphEvaluator;
import com.lowlatency.graph.evaluator.GraphEvaluatorBuilder;
import com.lowlatency.graph.node.DoubleComputeNode;
import com.lowlatency.graph.node.DoubleInputNode;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark for graph evaluation performance.
 *
 * Run with:
 * mvn clean package
 * java -jar target/benchmarks.jar GraphEvaluationBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
public class GraphEvaluationBenchmark {

    @State(Scope.Thread)
    public static class SmallGraphState {
        GraphEvaluator evaluator;
        DoubleInputNode input1;
        DoubleInputNode input2;
        int counter = 0;

        @Setup(Level.Trial)
        public void setup() {
            // Small graph: 2 inputs, 3 compute nodes
            input1 = new DoubleInputNode("input1", 10.0);
            input2 = new DoubleInputNode("input2", 20.0);

            DoubleComputeNode sum = new DoubleComputeNode("sum",
                    () -> input1.getDoubleValue() + input2.getDoubleValue());

            DoubleComputeNode product = new DoubleComputeNode("product",
                    () -> input1.getDoubleValue() * input2.getDoubleValue());

            DoubleComputeNode result = new DoubleComputeNode("result",
                    () -> sum.getDoubleValue() + product.getDoubleValue());

            GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();
            builder.addNode(input1);
            builder.addNode(input2);
            int sumId = builder.addNode(sum);
            int productId = builder.addNode(product);
            builder.addNode(result);

            builder.addDependency(sumId, 0);
            builder.addDependency(sumId, 1);
            builder.addDependency(productId, 0);
            builder.addDependency(productId, 1);
            builder.addDependency(4, sumId);
            builder.addDependency(4, productId);

            evaluator = builder.build();
        }
    }

    @State(Scope.Thread)
    public static class MediumGraphState {
        GraphEvaluator evaluator;
        DoubleInputNode[] inputs;
        int counter = 0;

        @Setup(Level.Trial)
        public void setup() {
            // Medium graph: 10 inputs, ~30 compute nodes (3 layers)
            inputs = new DoubleInputNode[10];
            GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();

            for (int i = 0; i < 10; i++) {
                inputs[i] = new DoubleInputNode("input" + i, i * 10.0);
                builder.addNode(inputs[i]);
            }

            // Layer 1: pairwise sums (5 nodes)
            DoubleComputeNode[] layer1 = new DoubleComputeNode[5];
            for (int i = 0; i < 5; i++) {
                final int idx = i;
                layer1[i] = new DoubleComputeNode("sum" + i,
                        () -> inputs[idx * 2].getDoubleValue() + inputs[idx * 2 + 1].getDoubleValue());
                int nodeId = builder.addNode(layer1[i]);
                builder.addDependency(nodeId, idx * 2);
                builder.addDependency(nodeId, idx * 2 + 1);
            }

            // Layer 2: pairwise products (5 nodes)
            DoubleComputeNode[] layer2 = new DoubleComputeNode[5];
            for (int i = 0; i < 5; i++) {
                final int idx = i;
                layer2[i] = new DoubleComputeNode("product" + i,
                        () -> inputs[idx * 2].getDoubleValue() * inputs[idx * 2 + 1].getDoubleValue());
                int nodeId = builder.addNode(layer2[i]);
                builder.addDependency(nodeId, idx * 2);
                builder.addDependency(nodeId, idx * 2 + 1);
            }

            // Layer 3: combine (10 nodes)
            for (int i = 0; i < 5; i++) {
                final int idx = i;
                DoubleComputeNode combined = new DoubleComputeNode("combined" + i,
                        () -> layer1[idx].getDoubleValue() + layer2[idx].getDoubleValue());
                int nodeId = builder.addNode(combined);
                builder.addDependency(nodeId, 10 + idx);
                builder.addDependency(nodeId, 15 + idx);
            }

            // Final aggregation
            DoubleComputeNode finalSum = new DoubleComputeNode("finalSum", () -> {
                double sum = 0;
                for (int i = 0; i < 5; i++) {
                    sum += layer1[i].getDoubleValue() + layer2[i].getDoubleValue();
                }
                return sum;
            });

            int finalId = builder.addNode(finalSum);
            for (int i = 10; i < 20; i++) {
                builder.addDependency(finalId, i);
            }

            evaluator = builder.build();
        }
    }

    @State(Scope.Thread)
    public static class LargeGraphState {
        GraphEvaluator evaluator;
        DoubleInputNode[] inputs;
        int counter = 0;

        @Setup(Level.Trial)
        public void setup() {
            // Large graph: 50 inputs, ~200 nodes (5 layers)
            inputs = new DoubleInputNode[50];
            GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();

            for (int i = 0; i < 50; i++) {
                inputs[i] = new DoubleInputNode("input" + i, i * 10.0);
                builder.addNode(inputs[i]);
            }

            int currentId = 50;

            // Create 4 processing layers
            for (int layer = 0; layer < 4; layer++) {
                for (int i = 0; i < 25; i++) {
                    final int idx = i;
                    final int layerNum = layer;

                    DoubleComputeNode node = new DoubleComputeNode("layer" + layer + "_" + i, () -> {
                        double sum = 0;
                        int baseIdx = layerNum == 0 ? idx * 2 : currentId - 25 + (idx / 2);
                        if (layerNum == 0) {
                            sum = inputs[idx * 2 % 50].getDoubleValue() + inputs[(idx * 2 + 1) % 50].getDoubleValue();
                        } else {
                            sum = Math.sin(idx) * 100;
                        }
                        return sum;
                    });

                    int nodeId = builder.addNode(node);
                    if (layer == 0) {
                        builder.addDependency(nodeId, (i * 2) % 50);
                        builder.addDependency(nodeId, (i * 2 + 1) % 50);
                    } else {
                        builder.addDependency(nodeId, currentId - 25 + (i / 2));
                    }
                    currentId++;
                }
            }

            evaluator = builder.build();
        }
    }

    @Benchmark
    public void smallGraph_staticEvaluation(SmallGraphState state, Blackhole blackhole) {
        state.evaluator.evaluate();
        blackhole.consume(state.evaluator.getNode(4).getValue());
    }

    @Benchmark
    public void smallGraph_dynamicEvaluation(SmallGraphState state, Blackhole blackhole) {
        state.input1.setValue(state.counter++);
        state.evaluator.evaluate();
        blackhole.consume(state.evaluator.getNode(4).getValue());
    }

    @Benchmark
    public void mediumGraph_staticEvaluation(MediumGraphState state, Blackhole blackhole) {
        state.evaluator.evaluate();
        blackhole.consume(state.evaluator.getNode(state.evaluator.getNumNodes() - 1).getValue());
    }

    @Benchmark
    public void mediumGraph_dynamicEvaluation(MediumGraphState state, Blackhole blackhole) {
        state.inputs[state.counter++ % 10].setValue(state.counter * 0.1);
        state.evaluator.evaluate();
        blackhole.consume(state.evaluator.getNode(state.evaluator.getNumNodes() - 1).getValue());
    }

    @Benchmark
    public void largeGraph_staticEvaluation(LargeGraphState state, Blackhole blackhole) {
        state.evaluator.evaluate();
        blackhole.consume(state.evaluator.getNode(state.evaluator.getNumNodes() - 1).getValue());
    }

    @Benchmark
    public void largeGraph_dynamicEvaluation(LargeGraphState state, Blackhole blackhole) {
        state.inputs[state.counter++ % 50].setValue(state.counter * 0.1);
        state.evaluator.evaluate();
        blackhole.consume(state.evaluator.getNode(state.evaluator.getNumNodes() - 1).getValue());
    }
}
