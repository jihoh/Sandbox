package com.lowlatency.graph.hybrid.compiler;

import com.lowlatency.graph.hybrid.core.Calculator;
import com.lowlatency.graph.hybrid.core.HybridCompiledGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for dynamically associating operation names with Calculator implementations.
 *
 * Features from SingleThreadedEngine:
 * - Extensible: add new operations without changing core engine
 * - Arity validation: ensure correct number of inputs at compile time
 * - Clean separation: operations defined separately from graph structure
 *
 * Enhanced features:
 * - Builder pattern for fluent API
 * - Pre-registered common operations
 * - Better error messages
 * - Operation metadata and documentation
 */
public final class CalculatorRegistry {

    /** Special arity value indicating variable number of inputs */
    public static final int VARIADIC = -1;

    /** Metadata for each registered operation */
    private record OperationMetadata(
        Calculator calculator,
        int arity,
        String description
    ) {}

    private final Map<String, OperationMetadata> operations;

    public CalculatorRegistry() {
        this.operations = new HashMap<>();
    }

    // ========== Registration API ==========

    /**
     * Registers a calculator that accepts a variable number of inputs.
     *
     * @param operationName unique operation identifier (e.g., "SUM", "MAX")
     * @param calculator the computation lambda/function
     * @param description human-readable description
     */
    public CalculatorRegistry registerVariadic(String operationName,
                                                Calculator calculator,
                                                String description) {
        if (operations.containsKey(operationName)) {
            throw new IllegalArgumentException("Operation already registered: " + operationName);
        }
        operations.put(operationName,
            new OperationMetadata(calculator, VARIADIC, description));
        return this;
    }

    /**
     * Registers a calculator that accepts a variable number of inputs.
     */
    public CalculatorRegistry registerVariadic(String operationName, Calculator calculator) {
        return registerVariadic(operationName, calculator, "");
    }

    /**
     * Registers a calculator that accepts a fixed number of inputs.
     *
     * @param operationName unique operation identifier (e.g., "SUB", "DIV")
     * @param arity exact number of parent nodes required
     * @param calculator the computation lambda/function
     * @param description human-readable description
     */
    public CalculatorRegistry registerFixed(String operationName,
                                            int arity,
                                            Calculator calculator,
                                            String description) {
        if (arity < 0) {
            throw new IllegalArgumentException(
                "Fixed arity must be non-negative. Use registerVariadic for variable arity.");
        }
        if (operations.containsKey(operationName)) {
            throw new IllegalArgumentException("Operation already registered: " + operationName);
        }
        operations.put(operationName,
            new OperationMetadata(calculator, arity, description));
        return this;
    }

    /**
     * Registers a calculator that accepts a fixed number of inputs.
     */
    public CalculatorRegistry registerFixed(String operationName, int arity, Calculator calculator) {
        return registerFixed(operationName, arity, calculator, "");
    }

    // ========== Query API ==========

    /**
     * Retrieves the calculator for an operation.
     *
     * @throws IllegalArgumentException if operation not registered
     */
    public Calculator getCalculator(String operationName) {
        OperationMetadata meta = operations.get(operationName);
        if (meta == null) {
            throw new IllegalArgumentException(
                "No calculator registered for operation: " + operationName +
                ". Available operations: " + operations.keySet());
        }
        return meta.calculator();
    }

    /**
     * Retrieves the expected arity for an operation.
     *
     * @return exact arity, or VARIADIC (-1) if variable
     * @throws IllegalArgumentException if operation not registered
     */
    public int getArity(String operationName) {
        OperationMetadata meta = operations.get(operationName);
        if (meta == null) {
            throw new IllegalArgumentException(
                "No operation registered: " + operationName);
        }
        return meta.arity();
    }

    /**
     * Checks if an operation is registered.
     */
    public boolean hasOperation(String operationName) {
        return operations.containsKey(operationName);
    }

    /**
     * Returns all registered operation names.
     */
    public Set<String> getOperationNames() {
        return operations.keySet();
    }

    /**
     * Returns the description of an operation.
     */
    public String getDescription(String operationName) {
        OperationMetadata meta = operations.get(operationName);
        return meta != null ? meta.description() : "";
    }

    // ========== Pre-built Standard Operations ==========

    /**
     * Creates a registry with commonly used mathematical operations.
     */
    public static CalculatorRegistry createStandard() {
        CalculatorRegistry registry = new CalculatorRegistry();

        // Variadic operations
        registry.registerVariadic("SUM", (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            int end = graph.getParentEndIndex(nodeId);
            double sum = 0.0;
            for (int i = start; i < end; i++) {
                sum += graph.values[graph.parentValues[i]];
            }
            return sum;
        }, "Sums all parent values");

        registry.registerVariadic("PRODUCT", (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            int end = graph.getParentEndIndex(nodeId);
            double product = 1.0;
            for (int i = start; i < end; i++) {
                product *= graph.values[graph.parentValues[i]];
            }
            return product;
        }, "Multiplies all parent values");

        registry.registerVariadic("MIN", (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            int end = graph.getParentEndIndex(nodeId);
            if (start == end) return Double.NaN;
            double min = Double.POSITIVE_INFINITY;
            for (int i = start; i < end; i++) {
                min = Math.min(min, graph.values[graph.parentValues[i]]);
            }
            return min;
        }, "Returns minimum of all parent values");

        registry.registerVariadic("MAX", (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            int end = graph.getParentEndIndex(nodeId);
            if (start == end) return Double.NaN;
            double max = Double.NEGATIVE_INFINITY;
            for (int i = start; i < end; i++) {
                max = Math.max(max, graph.values[graph.parentValues[i]]);
            }
            return max;
        }, "Returns maximum of all parent values");

        registry.registerVariadic("AVG", (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            int end = graph.getParentEndIndex(nodeId);
            int count = end - start;
            if (count == 0) return Double.NaN;
            double sum = 0.0;
            for (int i = start; i < end; i++) {
                sum += graph.values[graph.parentValues[i]];
            }
            return sum / count;
        }, "Returns average of all parent values");

        // Binary operations
        registry.registerFixed("ADD", 2, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return graph.values[graph.parentValues[start]] +
                   graph.values[graph.parentValues[start + 1]];
        }, "Adds two values: a + b");

        registry.registerFixed("SUB", 2, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return graph.values[graph.parentValues[start]] -
                   graph.values[graph.parentValues[start + 1]];
        }, "Subtracts two values: a - b");

        registry.registerFixed("MUL", 2, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return graph.values[graph.parentValues[start]] *
                   graph.values[graph.parentValues[start + 1]];
        }, "Multiplies two values: a * b");

        registry.registerFixed("DIV", 2, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return graph.values[graph.parentValues[start]] /
                   graph.values[graph.parentValues[start + 1]];
        }, "Divides two values: a / b");

        registry.registerFixed("POW", 2, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return Math.pow(
                graph.values[graph.parentValues[start]],
                graph.values[graph.parentValues[start + 1]]
            );
        }, "Raises to power: a^b");

        registry.registerFixed("MOD", 2, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return graph.values[graph.parentValues[start]] %
                   graph.values[graph.parentValues[start + 1]];
        }, "Modulo operation: a % b");

        // Unary operations
        registry.registerFixed("SQRT", 1, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return Math.sqrt(graph.values[graph.parentValues[start]]);
        }, "Square root");

        registry.registerFixed("ABS", 1, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return Math.abs(graph.values[graph.parentValues[start]]);
        }, "Absolute value");

        registry.registerFixed("NEG", 1, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return -graph.values[graph.parentValues[start]];
        }, "Negation: -a");

        registry.registerFixed("SIN", 1, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return Math.sin(graph.values[graph.parentValues[start]]);
        }, "Sine function");

        registry.registerFixed("COS", 1, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return Math.cos(graph.values[graph.parentValues[start]]);
        }, "Cosine function");

        registry.registerFixed("LOG", 1, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return Math.log(graph.values[graph.parentValues[start]]);
        }, "Natural logarithm");

        registry.registerFixed("EXP", 1, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            return Math.exp(graph.values[graph.parentValues[start]]);
        }, "Exponential: e^a");

        // Ternary operations
        registry.registerFixed("CLAMP", 3, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            double value = graph.values[graph.parentValues[start]];
            double min = graph.values[graph.parentValues[start + 1]];
            double max = graph.values[graph.parentValues[start + 2]];
            return Math.max(min, Math.min(max, value));
        }, "Clamps value between min and max");

        registry.registerFixed("LERP", 3, (nodeId, graph) -> {
            int start = graph.getParentStartIndex(nodeId);
            double a = graph.values[graph.parentValues[start]];
            double b = graph.values[graph.parentValues[start + 1]];
            double t = graph.values[graph.parentValues[start + 2]];
            return a + (b - a) * t;
        }, "Linear interpolation: a + (b-a)*t");

        return registry;
    }

    /**
     * Prints all registered operations.
     */
    public void printOperations() {
        System.out.println("=== Registered Operations ===");
        operations.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String name = entry.getKey();
                OperationMetadata meta = entry.getValue();
                String arityStr = meta.arity() == VARIADIC ? "variadic" : String.valueOf(meta.arity());
                System.out.printf("%-12s arity=%-8s %s%n", name, arityStr, meta.description());
            });
    }
}
