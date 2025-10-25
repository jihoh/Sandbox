package com.lowlatency.graph.hybrid.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable definition of a single node in the computation graph.
 *
 * This is the user-facing API for defining graph structure.
 * NodeDefinitions are compiled into the highly optimized HybridCompiledGraph.
 *
 * Design: Clean separation between graph definition (this class) and
 * runtime representation (HybridCompiledGraph).
 */
public final class NodeDefinition {

    private final String name;
    private final NodeType type;
    private final String operation;
    private final List<String> parents;
    private final double initialValue;

    /**
     * Node type: INPUT or COMPUTE
     */
    public enum NodeType {
        /** Input node - value set externally */
        INPUT,
        /** Computation node - value computed from parents */
        COMPUTE
    }

    private NodeDefinition(String name, NodeType type, String operation,
                          List<String> parents, double initialValue) {
        this.name = name;
        this.type = type;
        this.operation = operation;
        this.parents = Collections.unmodifiableList(new ArrayList<>(parents));
        this.initialValue = initialValue;
    }

    // ========== Factory Methods ==========

    /**
     * Creates an input node definition with initial value.
     */
    public static NodeDefinition input(String name, double initialValue) {
        return new NodeDefinition(name, NodeType.INPUT, "INPUT",
            Collections.emptyList(), initialValue);
    }

    /**
     * Creates an input node definition with default value 0.0.
     */
    public static NodeDefinition input(String name) {
        return input(name, 0.0);
    }

    /**
     * Creates a computation node definition.
     *
     * @param name unique node name
     * @param operation operation name (must be registered in CalculatorRegistry)
     * @param parents list of parent node names this node depends on
     */
    public static NodeDefinition compute(String name, String operation, String... parents) {
        return new NodeDefinition(name, NodeType.COMPUTE, operation,
            List.of(parents), 0.0);
    }

    /**
     * Creates a computation node definition with parent list.
     */
    public static NodeDefinition compute(String name, String operation, List<String> parents) {
        return new NodeDefinition(name, NodeType.COMPUTE, operation,
            parents, 0.0);
    }

    // ========== Getters ==========

    public String getName() {
        return name;
    }

    public NodeType getType() {
        return type;
    }

    public String getOperation() {
        return operation;
    }

    public List<String> getParents() {
        return parents;
    }

    public double getInitialValue() {
        return initialValue;
    }

    public boolean isInput() {
        return type == NodeType.INPUT;
    }

    public boolean isCompute() {
        return type == NodeType.COMPUTE;
    }

    // ========== Object methods ==========

    @Override
    public String toString() {
        if (isInput()) {
            return String.format("Input[%s = %.2f]", name, initialValue);
        } else {
            return String.format("Compute[%s = %s(%s)]",
                name, operation, String.join(", ", parents));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeDefinition)) return false;
        NodeDefinition other = (NodeDefinition) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
