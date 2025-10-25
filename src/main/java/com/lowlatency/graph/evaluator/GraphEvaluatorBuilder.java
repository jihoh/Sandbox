package com.lowlatency.graph.evaluator;

import com.lowlatency.graph.csr.CSRGraph;
import com.lowlatency.graph.csr.GraphBuilder;
import com.lowlatency.graph.node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating GraphEvaluator instances.
 *
 * Workflow:
 * 1. Create nodes
 * 2. Add nodes to builder
 * 3. Define dependencies (edges)
 * 4. Build evaluator
 *
 * Example:
 * <pre>
 * GraphEvaluatorBuilder builder = new GraphEvaluatorBuilder();
 * int priceId = builder.addNode(priceNode);
 * int qtyId = builder.addNode(qtyNode);
 * int notionalId = builder.addNode(notionalNode);
 * builder.addDependency(notionalId, priceId);
 * builder.addDependency(notionalId, qtyId);
 * GraphEvaluator evaluator = builder.build();
 * </pre>
 */
public final class GraphEvaluatorBuilder {

    private final List<Node<?>> nodes;
    private final Map<String, Integer> nameToId;
    private final List<Dependency> dependencies;

    public GraphEvaluatorBuilder() {
        this.nodes = new ArrayList<>();
        this.nameToId = new HashMap<>();
        this.dependencies = new ArrayList<>();
    }

    /**
     * Adds a node to the graph.
     *
     * @param node the node to add
     * @return the node ID assigned
     */
    public int addNode(Node<?> node) {
        int nodeId = nodes.size();
        node.setNodeId(nodeId);
        nodes.add(node);

        if (node.getName() != null) {
            nameToId.put(node.getName(), nodeId);
        }

        return nodeId;
    }

    /**
     * Adds a dependency edge: dependent -> dependency.
     * Means "dependent" depends on "dependency".
     *
     * @param dependentId the node that depends on another
     * @param dependencyId the node that is depended upon
     */
    public GraphEvaluatorBuilder addDependency(int dependentId, int dependencyId) {
        if (dependentId < 0 || dependentId >= nodes.size()) {
            throw new IllegalArgumentException("Invalid dependent ID: " + dependentId);
        }
        if (dependencyId < 0 || dependencyId >= nodes.size()) {
            throw new IllegalArgumentException("Invalid dependency ID: " + dependencyId);
        }

        dependencies.add(new Dependency(dependencyId, dependentId));
        return this;
    }

    /**
     * Adds a dependency by node names.
     */
    public GraphEvaluatorBuilder addDependency(String dependentName, String dependencyName) {
        Integer dependentId = nameToId.get(dependentName);
        Integer dependencyId = nameToId.get(dependencyName);

        if (dependentId == null) {
            throw new IllegalArgumentException("Node not found: " + dependentName);
        }
        if (dependencyId == null) {
            throw new IllegalArgumentException("Node not found: " + dependencyName);
        }

        return addDependency(dependentId, dependencyId);
    }

    /**
     * Builds the graph evaluator.
     *
     * @return configured GraphEvaluator
     * @throws IllegalStateException if graph contains cycles
     */
    public GraphEvaluator build() {
        int numNodes = nodes.size();

        // Build CSR graph
        GraphBuilder graphBuilder = new GraphBuilder(numNodes);
        for (Dependency dep : dependencies) {
            graphBuilder.addEdge(dep.from, dep.to);
        }

        CSRGraph csrGraph = graphBuilder.build();

        // Convert node list to array
        Node<?>[] nodeArray = nodes.toArray(new Node<?>[0]);

        return new GraphEvaluator(csrGraph, nodeArray);
    }

    /**
     * Gets a node ID by name.
     */
    public Integer getNodeId(String name) {
        return nameToId.get(name);
    }

    /**
     * Returns the number of nodes added so far.
     */
    public int getNumNodes() {
        return nodes.size();
    }

    /**
     * Returns the number of dependencies added so far.
     */
    public int getNumDependencies() {
        return dependencies.size();
    }

    private static class Dependency {
        final int from;
        final int to;

        Dependency(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }
}
