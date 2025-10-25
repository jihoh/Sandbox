package com.lowlatency.graph.hybrid.compiler;

import com.lowlatency.graph.hybrid.core.Calculator;
import com.lowlatency.graph.hybrid.core.HybridCompiledGraph;
import com.lowlatency.graph.hybrid.core.NodeDefinition;

import java.util.*;

/**
 * Compiles NodeDefinitions into an optimized HybridCompiledGraph.
 *
 * Compilation process:
 * 1. Validate: Check for duplicates, unknown dependencies, cycles
 * 2. Assign IDs: Map node names to integer IDs
 * 3. Build topology: Create CSR (Compressed Sparse Row) representation
 * 4. Topological sort: Compute evaluation order using Kahn's algorithm
 * 5. Arity checking: Validate operations against calculator registry
 * 6. Assemble: Create final HybridCompiledGraph
 *
 * This compilation happens once at startup, so we optimize for correctness
 * and error messages rather than compilation speed.
 */
public final class GraphCompiler {

    private final CalculatorRegistry registry;

    public GraphCompiler(CalculatorRegistry registry) {
        this.registry = registry;
    }

    /**
     * Compiles a list of node definitions into an optimized graph.
     *
     * @param definitions list of node definitions
     * @return compiled, immutable graph ready for evaluation
     * @throws GraphCompilationException if graph is invalid
     */
    public HybridCompiledGraph compile(List<NodeDefinition> definitions) {
        if (definitions.isEmpty()) {
            throw new GraphCompilationException("Cannot compile empty graph");
        }

        final int nodeCount = definitions.size();

        // Pass 1: Assign unique integer IDs and validate no duplicates
        Map<String, Integer> nameToId = new HashMap<>();
        String[] idToName = new String[nodeCount];

        for (int i = 0; i < nodeCount; i++) {
            NodeDefinition def = definitions.get(i);
            Integer existingId = nameToId.put(def.getName(), i);
            if (existingId != null) {
                throw new GraphCompilationException(
                    "Duplicate node name: '" + def.getName() + "' at indices " + existingId + " and " + i);
            }
            idToName[i] = def.getName();
        }

        // Pass 2: Build parent CSR and validate dependencies
        int[] parentCounts = new int[nodeCount];
        List<Integer> tempParentValues = new ArrayList<>();
        int[] parentIndexPointers = new int[nodeCount + 1];
        Map<String, Integer> inputNodeIds = new HashMap<>();
        int inputCount = 0;
        int computeCount = 0;

        parentIndexPointers[0] = 0;

        for (int i = 0; i < nodeCount; i++) {
            NodeDefinition def = definitions.get(i);
            List<String> parents = def.getParents();
            int numParents = parents.size();
            parentCounts[i] = numParents;

            // Track input vs compute nodes
            if (def.isInput()) {
                inputNodeIds.put(def.getName(), i);
                inputCount++;
            } else {
                computeCount++;
                // Validate operation exists and arity matches
                validateOperation(def, numParents);
            }

            // Add parent IDs to flat array
            for (String parentName : parents) {
                Integer parentId = nameToId.get(parentName);
                if (parentId == null) {
                    throw new GraphCompilationException(
                        "Node '" + def.getName() + "' references unknown parent: '" + parentName + "'");
                }
                tempParentValues.add(parentId);
            }

            parentIndexPointers[i + 1] = tempParentValues.size();
        }

        // Pass 3: Build children CSR (reverse of parent graph)
        List<List<Integer>> childrenAdjList = new ArrayList<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            childrenAdjList.add(new ArrayList<>());
        }

        for (int childId = 0; childId < nodeCount; childId++) {
            int start = parentIndexPointers[childId];
            int end = parentIndexPointers[childId + 1];
            for (int i = start; i < end; i++) {
                int parentId = tempParentValues.get(i);
                childrenAdjList.get(parentId).add(childId);
            }
        }

        // Flatten children adjacency list to CSR
        List<Integer> tempChildValues = new ArrayList<>();
        int[] childIndexPointers = new int[nodeCount + 1];
        childIndexPointers[0] = 0;

        for (int i = 0; i < nodeCount; i++) {
            tempChildValues.addAll(childrenAdjList.get(i));
            childIndexPointers[i + 1] = tempChildValues.size();
        }

        // Pass 4: Topological sort using Kahn's algorithm (validates no cycles)
        int[] fullTopoOrder = topologicalSort(nodeCount, parentCounts, tempChildValues, childIndexPointers);

        // Pass 5: Build calculator jump table and computation-only topological order
        Calculator[] calculators = new Calculator[nodeCount];
        List<Integer> computeOnlyOrder = new ArrayList<>();

        for (int nodeId : fullTopoOrder) {
            NodeDefinition def = definitions.get(nodeId);
            if (def.isCompute()) {
                calculators[nodeId] = registry.getCalculator(def.getOperation());
                computeOnlyOrder.add(nodeId);
            }
        }

        // Pass 6: Initialize values array with initial values
        double[] values = new double[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            values[i] = definitions.get(i).getInitialValue();
        }

        // Pass 7: Assemble final compiled graph
        return new HybridCompiledGraph(
            nodeCount,
            inputCount,
            computeCount,
            idToName,
            nameToId,
            inputNodeIds,
            values,
            null, // objectValues not used yet
            calculators,
            parentCounts,
            tempParentValues.stream().mapToInt(Integer::intValue).toArray(),
            parentIndexPointers,
            tempChildValues.stream().mapToInt(Integer::intValue).toArray(),
            childIndexPointers,
            computeOnlyOrder.stream().mapToInt(Integer::intValue).toArray(),
            fullTopoOrder,
            tempParentValues.size() // edge count
        );
    }

    /**
     * Validates that an operation is registered and has correct arity.
     */
    private void validateOperation(NodeDefinition def, int actualArity) {
        String operation = def.getOperation();

        if (!registry.hasOperation(operation)) {
            throw new GraphCompilationException(
                "Node '" + def.getName() + "' uses unregistered operation: '" + operation + "'. " +
                "Available operations: " + registry.getOperationNames());
        }

        int expectedArity = registry.getArity(operation);

        // Check arity only if operation has fixed arity
        if (expectedArity != CalculatorRegistry.VARIADIC && actualArity != expectedArity) {
            throw new GraphCompilationException(String.format(
                "Node '%s': operation '%s' expects %d inputs but found %d. Parents: %s",
                def.getName(), operation, expectedArity, actualArity, def.getParents()));
        }
    }

    /**
     * Performs topological sort using Kahn's algorithm.
     * Detects cycles and returns nodes in dependency order.
     *
     * @return array of node IDs in topological order
     * @throws GraphCompilationException if cycle detected
     */
    private int[] topologicalSort(int nodeCount, int[] parentCounts,
                                   List<Integer> childValues, int[] childIndexPointers) {

        // Copy parent counts (in-degree) so we can modify
        int[] inDegree = Arrays.copyOf(parentCounts, nodeCount);

        // Queue of nodes with no dependencies
        Deque<Integer> queue = new ArrayDeque<>();

        // Initialize queue with all nodes that have in-degree 0
        for (int i = 0; i < nodeCount; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
            }
        }

        // Process nodes in topological order
        List<Integer> result = new ArrayList<>();

        while (!queue.isEmpty()) {
            int nodeId = queue.poll();
            result.add(nodeId);

            // Reduce in-degree of all children
            int childStart = childIndexPointers[nodeId];
            int childEnd = childIndexPointers[nodeId + 1];

            for (int i = childStart; i < childEnd; i++) {
                int childId = childValues.get(i);
                inDegree[childId]--;

                if (inDegree[childId] == 0) {
                    queue.offer(childId);
                }
            }
        }

        // If we didn't process all nodes, there's a cycle
        if (result.size() != nodeCount) {
            List<String> cycleNodes = new ArrayList<>();
            for (int i = 0; i < nodeCount; i++) {
                if (inDegree[i] > 0) {
                    cycleNodes.add(String.format("%d", i));
                }
            }
            throw new GraphCompilationException(
                "Cycle detected in graph. Nodes involved: " + cycleNodes +
                ". Processed " + result.size() + " out of " + nodeCount + " nodes.");
        }

        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Exception thrown during graph compilation.
     */
    public static class GraphCompilationException extends RuntimeException {
        public GraphCompilationException(String message) {
            super(message);
        }

        public GraphCompilationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
