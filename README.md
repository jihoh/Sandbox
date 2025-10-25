# Low-Latency Graph Framework

A high-performance, ultra-low latency graph framework for Java, designed for HFT-level applications such as signal generation and low-latency pricing. Built with **LMAX Disruptor** for event processing and **CSR (Compressed Sparse Row)** format for optimal graph traversal.

## Features

- **Sub-millisecond Latency**: Target < 1ms for full graph evaluation
- **Zero-Allocation Hot Path**: Critical paths avoid object allocation
- **CSR Graph Representation**: Cache-friendly, optimal for traversal
- **LMAX Disruptor Integration**: Lock-free, ultra-low latency event processing
- **Topological Evaluation**: Automatic dependency resolution and optimal evaluation order
- **Primitive Specialization**: Avoid boxing overhead with specialized node types
- **JMH Benchmarking**: Built-in performance benchmarks

## Architecture

### Core Components

1. **CSR Graph** (`com.lowlatency.graph.csr`)
   - Compressed Sparse Row format for optimal memory layout
   - O(V + E) space complexity
   - Cache-friendly sequential access
   - Automatic topological sorting

2. **Node Framework** (`com.lowlatency.graph.node`)
   - `Node<T>`: Base interface for computation nodes
   - `DoubleInputNode`, `DoubleComputeNode`: Specialized for numeric computation
   - Zero-allocation compute methods
   - Support for incremental evaluation

3. **Graph Evaluator** (`com.lowlatency.graph.evaluator`)
   - Sequential topological evaluation
   - O(V) evaluation time
   - Incremental evaluation support
   - Thread-local for optimal performance

4. **Disruptor Integration** (`com.lowlatency.graph.disruptor`)
   - Lock-free event processing
   - Pre-allocated ring buffer
   - Latency tracking and statistics
   - Multiple wait strategies for latency tuning

## Performance Characteristics

### Expected Latency (per event)
- Small graph (5-10 nodes): 100-300 ns
- Medium graph (30-50 nodes): 500-800 ns
- Large graph (100-200 nodes): < 1 µs
- Very large graph (500+ nodes): < 10 µs

### Design Optimizations
1. **Zero allocation** in evaluation hot path
2. **Sequential memory access** via CSR format
3. **Monomorphic call sites** for JIT optimization
4. **Primitive types** to avoid boxing
5. **Cache-line awareness** in critical structures
6. **Lock-free algorithms** via Disruptor

## Quick Start

### 1. Build the Project

```bash
mvn clean package
```

### 2. Simple Example

```java
// Create input nodes
DoubleInputNode a = new DoubleInputNode("a", 10.0);
DoubleInputNode b = new DoubleInputNode("b", 20.0);

// Create computation nodes
DoubleComputeNode sum = new DoubleComputeNode("sum",
    () -> a.getDoubleValue() + b.getDoubleValue());

// Build the graph
GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();
builder.addNode(a);
builder.addNode(b);
int sumId = builder.addNode(sum);

builder.addDependency(sumId, 0);  // sum depends on a
builder.addDependency(sumId, 1);  // sum depends on b

GraphEvaluator evaluator = builder.build();

// Evaluate
evaluator.evaluate();
System.out.println("Sum: " + sum.getDoubleValue());
```

### 3. With Disruptor (Event-Driven)

```java
// Build graph evaluator (same as above)
GraphEvaluator evaluator = builder.build();

// Create Disruptor processor
DisruptorGraphProcessor processor = new DisruptorGraphProcessor(evaluator, 1024);

// Publish events (zero allocation)
processor.publishUpdate(aId, 15.0);  // Update node 'a'
processor.publishUpdate(bId, 25.0);  // Update node 'b'

// Print statistics
processor.printStats();

// Shutdown
processor.shutdown();
```

## Examples

### Run Examples

```bash
# Simple arithmetic graph
mvn exec:java -Dexec.mainClass="com.lowlatency.graph.examples.SimpleGraphExample"

# Black-Scholes pricing with Greeks
mvn exec:java -Dexec.mainClass="com.lowlatency.graph.examples.PricingGraphExample"
```

### Run Benchmarks

```bash
# Build benchmark jar
mvn clean package

# Run all benchmarks
java -jar target/benchmarks.jar

# Run specific benchmark
java -jar target/benchmarks.jar GraphEvaluationBenchmark

# Run with custom parameters
java -jar target/benchmarks.jar GraphEvaluationBenchmark.smallGraph -wi 5 -i 10 -f 3
```

## Use Cases

### 1. Options Pricing
Calculate Black-Scholes prices and Greeks in real-time as market data updates.

### 2. Signal Generation
Build complex signal graphs with multiple indicators, filters, and aggregations.

### 3. Risk Calculations
Real-time portfolio risk metrics with dependency chains.

### 4. Market Making
Ultra-low latency price calculations for automated market making.

## Performance Tuning

### JVM Options for Low Latency

```bash
java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=1 \
     -XX:+AlwaysPreTouch \
     -XX:+UseStringDeduplication \
     -Xms4G -Xmx4G \
     -XX:+UnlockExperimentalVMOptions \
     -XX:+UseEpsilonGC \  # For no-GC runs
     -jar your-app.jar
```

### Disruptor Wait Strategies

```java
// Ultra-low latency (high CPU usage)
new BusySpinWaitStrategy()

// Low latency (moderate CPU)
new YieldingWaitStrategy()  // Default

// Normal latency (low CPU)
new SleepingWaitStrategy()

// Balanced
new BlockingWaitStrategy()
```

### CPU Pinning (Linux)

```bash
# Pin to specific CPUs for consistent latency
taskset -c 0,1 java -jar your-app.jar
```

## API Reference

### Node Types

- **`DoubleInputNode`**: Input node for double values (zero boxing)
- **`InputNode<T>`**: Generic input node
- **`DoubleComputeNode`**: Computation node for doubles
- **`ComputeNode<T>`**: Generic computation node

### Graph Building

```java
GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();

// Add nodes
int nodeId = builder.addNode(node);

// Add dependencies (dependent, dependency)
builder.addDependency(computeNodeId, inputNodeId);

// Build
GraphEvaluator evaluator = builder.build();
```

### Evaluation

```java
// Full evaluation
evaluator.evaluate();

// Incremental (only dirty nodes)
evaluator.evaluateIncremental();

// Get results
double value = ((DoubleComputeNode) evaluator.getNode(nodeId)).getDoubleValue();
```

## Implementation Details

### CSR Format

```
Adjacency List:          CSR Representation:
0 -> [1, 2]             rowOffsets:    [0, 2, 4, 5, 5]
1 -> [2, 3]             columnIndices: [1, 2, 2, 3, 4]
2 -> [4]
3 -> []

Node 0's neighbors: columnIndices[rowOffsets[0]...rowOffsets[1]-1] = [1, 2]
```

### Topological Sort

Uses Kahn's algorithm (O(V + E)) to compute evaluation order at graph construction time, ensuring dependencies are always computed before dependents.

### Zero-Allocation Design

```java
// Hot path - no allocation
public void evaluate() {
    final int[] order = topologicalOrder;
    final Node<?>[] nodes = this.nodes;

    for (int i = 0; i < order.length; i++) {
        nodes[order[i]].compute();
    }
}
```

## Limitations

1. **Static Graphs**: Graph structure is immutable after construction
2. **Single-threaded Evaluation**: Each evaluator is not thread-safe
3. **Acyclic Only**: Graphs must be DAGs (directed acyclic graphs)
4. **JVM Warm-up**: Requires warm-up for optimal performance

## Future Enhancements

- [ ] Parallel evaluation for independent sub-graphs
- [ ] SIMD operations via Vector API (Java 21+)
- [ ] Off-heap memory for larger graphs
- [ ] GPU acceleration for numeric-heavy graphs
- [ ] Dynamic graph updates (add/remove nodes)
- [ ] Distributed graph evaluation

## Requirements

- Java 21+
- Maven 3.6+
- LMAX Disruptor 4.0.0

## License

MIT License

## Contributing

Contributions welcome! Please focus on:
- Performance improvements
- New node types
- Additional examples
- Benchmark results on different hardware

## References

- [LMAX Disruptor](https://lmax-exchange.github.io/disruptor/)
- [Mechanical Sympathy Blog](https://mechanical-sympathy.blogspot.com/)
- [Java Performance: The Definitive Guide](https://www.oreilly.com/library/view/java-performance-the/9781449363512/)

## Contact

For HFT-specific optimizations and consulting, please open an issue.
