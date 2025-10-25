package com.lowlatency.graph.hybrid.compiler;

import com.lowlatency.graph.hybrid.core.HybridCompiledGraph;
import com.lowlatency.graph.hybrid.core.NodeDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder API for constructing computation graphs.
 *
 * Example usage:
 * <pre>
 * GraphBuilder builder = new GraphBuilder()
 *     .addInput("x", 0.0)
 *     .addInput("y", 0.0)
 *     .addCompute("sum", "SUM", "x", "y")
 *     .addCompute("product", "MUL", "x", "y");
 *
 * HybridCompiledGraph graph = builder.compile(registry);
 * </pre>
 *
 * This provides a more type-safe and readable API compared to manual
 * NodeDefinition construction.
 */
public final class GraphBuilder {

    private final List<NodeDefinition> definitions;

    public GraphBuilder() {
        this.definitions = new ArrayList<>();
    }

    /**
     * Adds an input node with a name and initial value.
     */
    public GraphBuilder addInput(String name, double initialValue) {
        definitions.add(NodeDefinition.input(name, initialValue));
        return this;
    }

    /**
     * Adds an input node with default initial value of 0.0.
     */
    public GraphBuilder addInput(String name) {
        definitions.add(NodeDefinition.input(name));
        return this;
    }

    /**
     * Adds a computation node.
     *
     * @param name unique node name
     * @param operation operation name (must be registered)
     * @param parents names of parent nodes this depends on
     */
    public GraphBuilder addCompute(String name, String operation, String... parents) {
        definitions.add(NodeDefinition.compute(name, operation, parents));
        return this;
    }

    /**
     * Adds a computation node with parent list.
     */
    public GraphBuilder addCompute(String name, String operation, List<String> parents) {
        definitions.add(NodeDefinition.compute(name, operation, parents));
        return this;
    }

    /**
     * Adds a pre-built node definition.
     */
    public GraphBuilder addNode(NodeDefinition definition) {
        definitions.add(definition);
        return this;
    }

    /**
     * Adds multiple node definitions.
     */
    public GraphBuilder addNodes(List<NodeDefinition> defs) {
        definitions.addAll(defs);
        return this;
    }

    /**
     * Returns the list of node definitions built so far.
     */
    public List<NodeDefinition> getDefinitions() {
        return new ArrayList<>(definitions);
    }

    /**
     * Returns the number of nodes defined.
     */
    public int size() {
        return definitions.size();
    }

    /**
     * Compiles the graph using the provided registry.
     *
     * @param registry calculator registry
     * @return compiled graph ready for evaluation
     */
    public HybridCompiledGraph compile(CalculatorRegistry registry) {
        GraphCompiler compiler = new GraphCompiler(registry);
        return compiler.compile(definitions);
    }

    /**
     * Compiles the graph using the standard calculator registry.
     */
    public HybridCompiledGraph compile() {
        return compile(CalculatorRegistry.createStandard());
    }

    /**
     * Clears all node definitions.
     */
    public GraphBuilder clear() {
        definitions.clear();
        return this;
    }

    @Override
    public String toString() {
        return String.format("GraphBuilder[nodes=%d]", definitions.size());
    }
}
