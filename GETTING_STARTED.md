# Getting Started

This guide will help you get started with the Low-Latency Graph Framework in 10 minutes.

## Installation

### Prerequisites

- Java 21 or later
- Maven 3.6 or later

### Build

```bash
git clone <repository-url>
cd Sandbox
mvn clean package
```

## Your First Graph

Let's build a simple graph that calculates price-weighted volume.

### Step 1: Create Input Nodes

```java
import com.lowlatency.graph.node.DoubleInputNode;

// Market data inputs
DoubleInputNode price = new DoubleInputNode("price", 100.0);
DoubleInputNode volume = new DoubleInputNode("volume", 1000.0);
```

### Step 2: Create Computation Nodes

```java
import com.lowlatency.graph.node.DoubleComputeNode;

// Calculate notional (price * volume)
DoubleComputeNode notional = new DoubleComputeNode("notional",
    () -> price.getDoubleValue() * volume.getDoubleValue()
);

// Calculate VWAP-like metric
DoubleComputeNode vwap = new DoubleComputeNode("vwap",
    () -> notional.getDoubleValue() / volume.getDoubleValue()
);
```

### Step 3: Build the Graph

```java
import com.lowlatency.graph.evaluator.GraphEvaluator;
import com.lowlatency.graph.evaluator.GraphEvaluatorBuilder;

GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();

// Add nodes
int priceId = builder.addNode(price);
int volumeId = builder.addNode(volume);
int notionalId = builder.addNode(notional);
int vwapId = builder.addNode(vwap);

// Define dependencies
builder.addDependency(notionalId, priceId);   // notional depends on price
builder.addDependency(notionalId, volumeId);  // notional depends on volume
builder.addDependency(vwapId, notionalId);    // vwap depends on notional
builder.addDependency(vwapId, volumeId);      // vwap depends on volume

// Build
GraphEvaluator evaluator = builder.build();
```

### Step 4: Evaluate

```java
// Evaluate the graph
evaluator.evaluate();

// Get results
System.out.printf("Price: %.2f%n", price.getDoubleValue());
System.out.printf("Volume: %.2f%n", volume.getDoubleValue());
System.out.printf("Notional: %.2f%n", notional.getDoubleValue());
System.out.printf("VWAP: %.2f%n", vwap.getDoubleValue());

// Update inputs and re-evaluate
price.setValue(105.0);
volume.setValue(2000.0);
evaluator.evaluate();

System.out.printf("Updated VWAP: %.2f%n", vwap.getDoubleValue());
```

### Complete Example

```java
package com.mycompany;

import com.lowlatency.graph.evaluator.GraphEvaluator;
import com.lowlatency.graph.evaluator.GraphEvaluatorBuilder;
import com.lowlatency.graph.node.DoubleComputeNode;
import com.lowlatency.graph.node.DoubleInputNode;

public class MyFirstGraph {
    public static void main(String[] args) {
        // Input nodes
        DoubleInputNode price = new DoubleInputNode("price", 100.0);
        DoubleInputNode volume = new DoubleInputNode("volume", 1000.0);

        // Compute nodes
        DoubleComputeNode notional = new DoubleComputeNode("notional",
            () -> price.getDoubleValue() * volume.getDoubleValue());

        DoubleComputeNode vwap = new DoubleComputeNode("vwap",
            () -> notional.getDoubleValue() / volume.getDoubleValue());

        // Build graph
        GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();
        int priceId = builder.addNode(price);
        int volumeId = builder.addNode(volume);
        int notionalId = builder.addNode(notional);
        int vwapId = builder.addNode(vwap);

        builder.addDependency(notionalId, priceId);
        builder.addDependency(notionalId, volumeId);
        builder.addDependency(vwapId, notionalId);
        builder.addDependency(vwapId, volumeId);

        GraphEvaluator evaluator = builder.build();

        // Evaluate
        evaluator.evaluate();
        System.out.printf("VWAP: %.2f%n", vwap.getDoubleValue());

        // Update and re-evaluate
        price.setValue(105.0);
        evaluator.evaluate();
        System.out.printf("Updated VWAP: %.2f%n", vwap.getDoubleValue());
    }
}
```

## Event-Driven Processing

For ultra-low latency event processing, use the Disruptor integration.

```java
import com.lowlatency.graph.disruptor.DisruptorGraphProcessor;

// ... build evaluator as above ...

// Create processor with ring buffer size (must be power of 2)
DisruptorGraphProcessor processor = new DisruptorGraphProcessor(evaluator, 1024);

// Publish events (zero allocation)
processor.publishUpdate(priceId, 101.5);
processor.publishUpdate(priceId, 102.0);
processor.publishUpdate(volumeId, 1500.0);

// Events are processed asynchronously by the Disruptor

// Wait a bit for processing
Thread.sleep(100);

// Print statistics
processor.printStats();

// Shutdown
processor.shutdown();
```

## Running Examples

The framework includes two complete examples:

### Simple Example

```bash
mvn exec:java -Dexec.mainClass="com.lowlatency.graph.examples.SimpleGraphExample"
```

Output:
```
=== Simple Graph Example ===

Graph: 5 nodes, 6 dependencies

Initial values:
  a = 10.00
  b = 20.00
  sum = 30.00
  product = 200.00
  sumPlusProduct = 230.00
...
```

### Pricing Example (Black-Scholes)

```bash
mvn exec:java -Dexec.mainClass="com.lowlatency.graph.examples.PricingGraphExample"
```

Output:
```
=== Low-Latency Pricing Graph Example ===

Building graph...
Graph built: 13 nodes, 20 dependencies

Processing market updates...
Processed 100000 updates in 156.23 ms
Throughput: 639947.21 updates/sec

=== Final Values ===
Spot Price: 100.00
Theoretical Price: 8.0213
Delta: 0.4247
Gamma: 0.009876
Vega: 37.5432

=== Graph Processor Statistics ===
Events processed: 100000
Avg evaluation time: 1.23 µs
...
```

## Performance Testing

Run built-in benchmarks:

```bash
# Build benchmarks
mvn clean package

# Run all benchmarks
java -jar target/benchmarks.jar

# Run specific benchmark
java -jar target/benchmarks.jar GraphEvaluationBenchmark.smallGraph
```

## Common Patterns

### 1. Moving Average

```java
class MovingAverage extends AbstractNode<Double> {
    private final DoubleInputNode input;
    private final double[] buffer;
    private int index = 0;
    private double sum = 0.0;
    private double value = 0.0;

    MovingAverage(String name, DoubleInputNode input, int period) {
        super(name);
        this.input = input;
        this.buffer = new double[period];
    }

    @Override
    public void compute() {
        double newVal = input.getDoubleValue();
        double oldVal = buffer[index];
        buffer[index] = newVal;
        index = (index + 1) % buffer.length;

        sum = sum - oldVal + newVal;
        value = sum / buffer.length;
        dirty = false;
    }

    @Override
    public Double getValue() {
        return value;
    }

    public double getDoubleValue() {
        return value;
    }
}
```

### 2. Conditional Logic

```java
DoubleComputeNode spread = new DoubleComputeNode("spread",
    () -> {
        double bid = bidNode.getDoubleValue();
        double ask = askNode.getDoubleValue();
        return ask > bid ? ask - bid : 0.0;
    }
);
```

### 3. Multiple Inputs

```java
DoubleComputeNode basketPrice = new DoubleComputeNode("basketPrice", () -> {
    double total = 0.0;
    for (DoubleInputNode stock : stocks) {
        total += stock.getDoubleValue() * weights[i];
    }
    return total;
});
```

### 4. Stateful Computations

```java
class EMA extends DoubleComputeNode {
    private final double alpha;
    private double ema;
    private boolean initialized = false;

    EMA(String name, DoubleInputNode input, double alpha) {
        super(name, () -> 0.0);  // Temporary
        this.alpha = alpha;
    }

    @Override
    public void compute() {
        double current = input.getDoubleValue();
        if (!initialized) {
            ema = current;
            initialized = true;
        } else {
            ema = alpha * current + (1 - alpha) * ema;
        }
        dirty = false;
    }

    public double getDoubleValue() {
        return ema;
    }
}
```

## Next Steps

1. Read the [README.md](README.md) for architecture details
2. Check [PERFORMANCE_GUIDE.md](PERFORMANCE_GUIDE.md) for tuning tips
3. Explore examples in `src/main/java/com/lowlatency/graph/examples/`
4. Run benchmarks to understand performance characteristics
5. Build your own trading signals or pricing models!

## Common Issues

### Graph has cycle error

```
IllegalStateException: Graph contains a cycle
```

**Solution**: Check your dependencies. The graph must be acyclic (DAG).

### Out of memory

```
java.lang.OutOfMemoryError: Java heap space
```

**Solution**: Increase heap size with `-Xms` and `-Xmx` flags.

### High latency

**Solutions**:
1. Warm up JVM with 10k+ iterations
2. Check GC logs for pauses
3. Use performance profiling tools
4. See [PERFORMANCE_GUIDE.md](PERFORMANCE_GUIDE.md)

## Support

- Open an issue on GitHub
- Check existing examples
- Review documentation

## Quick Reference

```java
// Input node
DoubleInputNode input = new DoubleInputNode("name", initialValue);
input.setValue(newValue);  // Update

// Compute node
DoubleComputeNode node = new DoubleComputeNode("name",
    () -> input1.getDoubleValue() + input2.getDoubleValue()
);

// Build graph
GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();
int id = builder.addNode(node);
builder.addDependency(dependentId, dependencyId);
GraphEvaluator evaluator = builder.build();

// Evaluate
evaluator.evaluate();
double result = node.getDoubleValue();

// With Disruptor
DisruptorGraphProcessor processor = new DisruptorGraphProcessor(evaluator, 1024);
processor.publishUpdate(inputId, value);
processor.printStats();
processor.shutdown();
```

Happy coding!
