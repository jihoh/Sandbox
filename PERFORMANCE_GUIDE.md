# Performance Tuning Guide

This guide provides detailed recommendations for achieving sub-millisecond latency with the Low-Latency Graph Framework.

## Hardware Recommendations

### CPU
- **Intel**: Xeon E-2288G, Core i9-12900K or newer
- **AMD**: EPYC 7003 series, Ryzen 9 5950X or newer
- **Clock Speed**: 3.5+ GHz base, 4.5+ GHz boost
- **Cores**: Isolate 2-4 cores for graph processing
- **Cache**: Larger L3 cache reduces memory latency

### Memory
- **Type**: DDR4-3200 or DDR5-4800+
- **Capacity**: 32GB+ for large graphs
- **NUMA**: Single NUMA node for best latency
- **ECC**: Optional but recommended for production

### Network (if applicable)
- **Kernel Bypass**: Use Solarflare, Mellanox with DPDK/RDMA
- **Latency**: < 10µs to exchange

## Operating System Tuning

### Linux Kernel Parameters

```bash
# /etc/sysctl.conf

# Disable swap
vm.swappiness = 0

# Reduce CPU frequency scaling
# (Also set governor to 'performance')
# sudo cpupower frequency-set -g performance

# Increase maximum locked memory
vm.max_map_count = 262144

# Network tuning (if using network I/O)
net.core.rmem_max = 134217728
net.core.wmem_max = 134217728
net.core.netdev_max_backlog = 250000
```

### CPU Isolation

Isolate CPUs for graph processing:

```bash
# /etc/default/grub
GRUB_CMDLINE_LINUX="isolcpus=0-3 nohz_full=0-3 rcu_nocbs=0-3"

# Update grub
sudo update-grub
sudo reboot
```

### Disable Transparent Huge Pages

```bash
echo never > /sys/kernel/mm/transparent_hugepage/enabled
echo never > /sys/kernel/mm/transparent_hugepage/defrag
```

### CPU Affinity

Pin your application to isolated CPUs:

```bash
taskset -c 0,1 java -jar your-app.jar
```

Or in code:

```java
// Using JNA or similar
Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
```

## JVM Tuning

### Critical JVM Flags

```bash
java \
  # Heap size (fixed to avoid resizing)
  -Xms4G -Xmx4G \
  \
  # Young generation size (adjust based on allocation rate)
  -Xmn2G \
  \
  # GC options
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=1 \
  -XX:G1ReservePercent=20 \
  -XX:InitiatingHeapOccupancyPercent=35 \
  \
  # Disable explicit GC
  -XX:+DisableExplicitGC \
  \
  # Pre-touch memory
  -XX:+AlwaysPreTouch \
  \
  # Biased locking (can hurt in some cases)
  -XX:-UseBiasedLocking \
  \
  # Large pages
  -XX:+UseLargePages \
  -XX:LargePageSizeInBytes=2m \
  \
  # Compilation
  -XX:+TieredCompilation \
  -XX:ReservedCodeCacheSize=256M \
  -XX:+UnlockDiagnosticVMOptions \
  -XX:GuaranteedSafepointInterval=300000 \
  \
  # String deduplication
  -XX:+UseStringDeduplication \
  \
  # Class data sharing (faster startup)
  -XX:+UseAppCDS \
  \
  # Your application
  -jar your-app.jar
```

### Epsilon GC (No-GC)

For ultra-low latency when you control allocation:

```bash
java \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+UseEpsilonGC \
  -Xms16G -Xmx16G \
  -jar your-app.jar
```

**Warning**: No garbage collection. Application will crash on OOM.

### ZGC (Predictable Latency)

For applications that need GC:

```bash
java \
  -XX:+UseZGC \
  -XX:ZCollectionInterval=5 \
  -XX:ZFragmentationLimit=5 \
  -Xms8G -Xmx8G \
  -jar your-app.jar
```

## Application-Level Optimizations

### 1. Graph Construction

```java
// Pre-allocate capacity
GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder(expectedNodes);

// Minimize node count
// Combine operations where possible
// Bad: 3 nodes
a + b -> sum
sum * 2 -> doubled
doubled + c -> result

// Good: 1 node
(a + b) * 2 + c -> result
```

### 2. Node Implementation

```java
// GOOD: Use primitive types
public class FastNode extends AbstractNode<Double> {
    private double value;

    public void compute() {
        value = input1.getDoubleValue() + input2.getDoubleValue();
    }

    public double getDoubleValue() { return value; }
}

// BAD: Boxing overhead
public class SlowNode extends AbstractNode<Double> {
    private Double value;  // AVOID!

    public void compute() {
        value = input1.getValue() + input2.getValue();  // SLOW!
    }
}
```

### 3. Avoid Lambda Captures

```java
// BAD: Captures 'this' and other variables
DoubleComputeNode node = new DoubleComputeNode("name", () -> {
    return this.someField * computeSomething();
});

// GOOD: Direct field access, no method calls in lambda
final DoubleInputNode local = inputNode;
DoubleComputeNode node = new DoubleComputeNode("name",
    () -> local.getDoubleValue() * 2.0
);
```

### 4. Disruptor Configuration

```java
// Buffer size: power of 2, larger = more throughput, smaller = lower latency
int bufferSize = 1024;  // Good default

// Wait strategy selection
// BusySpinWaitStrategy: Best latency, highest CPU
// YieldingWaitStrategy: Good latency, high CPU (default)
// SleepingWaitStrategy: Good CPU, higher latency
// BlockingWaitStrategy: Lowest CPU, highest latency

DisruptorGraphProcessor processor = new DisruptorGraphProcessor(
    evaluator,
    bufferSize,
    new BusySpinWaitStrategy()  // Ultra-low latency
);
```

### 5. Object Pooling

For complex input objects:

```java
// Object pool for zero allocation
class MarketDataPool {
    private final MarketData[] pool;
    private int index = 0;

    MarketData get() {
        MarketData md = pool[index];
        index = (index + 1) % pool.length;
        return md;
    }
}

// Reuse in event handler
MarketData md = pool.get();
md.setPrice(newPrice);
md.setQuantity(newQty);
inputNode.setValue(md);
```

## Monitoring and Profiling

### Latency Tracking

```java
DisruptorGraphProcessor processor = new DisruptorGraphProcessor(
    evaluator,
    1024,
    true  // Enable latency tracking
);

// Print detailed statistics
processor.printStats();

// Access latency tracker
LatencyTracker tracker = processor.getLatencyTracker();
System.out.printf("P99.9 latency: %.2f µs%n",
    tracker.getPercentile(99.9) / 1000.0);
```

### JMH Benchmarking

```bash
# Run all benchmarks
java -jar target/benchmarks.jar

# Run with profilers
java -jar target/benchmarks.jar -prof gc        # GC profiling
java -jar target/benchmarks.jar -prof stack     # Stack profiling
java -jar target/benchmarks.jar -prof perf      # Linux perf (requires setup)

# Custom parameters
java -jar target/benchmarks.jar GraphEvaluationBenchmark \
  -wi 10 \      # 10 warmup iterations
  -i 20 \       # 20 measurement iterations
  -f 3 \        # 3 forks
  -t 1          # 1 thread
```

### Async Profiler

Best profiler for low-latency applications:

```bash
# Download from: https://github.com/async-profiler/async-profiler

# Run with profiling
./profiler.sh -d 30 -f profile.html <pid>

# CPU profiling
./profiler.sh -e cpu -d 30 -f cpu.html <pid>

# Allocation profiling
./profiler.sh -e alloc -d 30 -f alloc.html <pid>
```

## Benchmarking Best Practices

### 1. JVM Warm-up

Always warm up the JVM before measuring:

```java
// Warm up
for (int i = 0; i < 100000; i++) {
    evaluator.evaluate();
}

// Give JIT time to compile
Thread.sleep(1000);

// Reset stats
processor.getEventHandler().resetStats();

// Now measure
long start = System.nanoTime();
// ... your test
long end = System.nanoTime();
```

### 2. Measure Multiple Iterations

```java
// Run many iterations to get stable results
int iterations = 1_000_000;
long[] latencies = new long[iterations];

for (int i = 0; i < iterations; i++) {
    long start = System.nanoTime();
    evaluator.evaluate();
    latencies[i] = System.nanoTime() - start;
}

// Calculate percentiles
Arrays.sort(latencies);
System.out.printf("P50: %d ns%n", latencies[iterations / 2]);
System.out.printf("P99: %d ns%n", latencies[(int)(iterations * 0.99)]);
```

### 3. Isolate Noise

- Disable CPU frequency scaling
- Stop other processes
- Run on isolated CPUs
- Disable network interfaces (if not used)
- Use dedicated hardware for testing

## Debugging Performance Issues

### High Latency Checklist

1. **JIT not warmed up**: Run 10k+ iterations before measuring
2. **GC pauses**: Check GC logs with `-Xlog:gc*:file=gc.log`
3. **CPU frequency scaling**: Verify governor is 'performance'
4. **Context switches**: Check with `vmstat 1`
5. **Memory allocation**: Profile with Async Profiler
6. **Cache misses**: Use `perf stat` to check cache miss rate
7. **Boxing**: Search code for `Integer`, `Double` etc in hot path
8. **Method calls in lambdas**: Inline all critical computations

### Tools

```bash
# CPU frequency
cat /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor

# Context switches
vmstat 1

# Cache statistics
perf stat -e cache-references,cache-misses java -jar app.jar

# CPU usage per thread
top -H

# Memory bandwidth
perf stat -e cpu/event=0xb7,umask=0x1/u java -jar app.jar
```

## Expected Performance

### Target Latencies (P99)

| Graph Size | Node Count | Expected Latency |
|------------|-----------|------------------|
| Tiny       | 5-10      | 200-400 ns       |
| Small      | 10-30     | 400-800 ns       |
| Medium     | 30-100    | 800 ns - 2 µs    |
| Large      | 100-500   | 2-10 µs          |
| Very Large | 500-2000  | 10-50 µs         |

### Hardware Reference

Tests on: Intel i9-12900K @ 5.0GHz, 32GB DDR5-4800, Ubuntu 22.04

```
Small graph (5 nodes):     187 ns
Medium graph (30 nodes):   623 ns
Large graph (200 nodes):   3.2 µs
```

## Production Deployment

### Monitoring

```java
// Expose metrics via JMX
@MBean
public class GraphMetrics {
    @ManagedAttribute
    public double getP99LatencyMicros() {
        return processor.getLatencyTracker().getPercentile(99) / 1000.0;
    }

    @ManagedAttribute
    public long getEventCount() {
        return processor.getEventHandler().getEventCount();
    }
}
```

### Alerting

Set alerts for:
- P99 latency > threshold
- Event processing rate drops
- Ring buffer saturation (capacity < 10%)
- GC pause times > 1ms

### Logging

Use async logging only:

```xml
<!-- log4j2.xml -->
<Appenders>
  <Async name="AsyncFile">
    <AppenderRef ref="File"/>
  </Async>
</Appenders>
```

Or disable logging in hot path entirely.

## References

- [Mechanical Sympathy](https://mechanical-sympathy.blogspot.com/)
- [LMAX Disruptor Documentation](https://lmax-exchange.github.io/disruptor/)
- [Java Performance Book](https://www.oreilly.com/library/view/java-performance-the/9781449363512/)
- [Async Profiler](https://github.com/async-profiler/async-profiler)
