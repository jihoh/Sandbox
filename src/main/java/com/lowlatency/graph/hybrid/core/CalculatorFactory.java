package com.lowlatency.graph.hybrid.core;

/**
 * Factory for creating Calculator instances.
 *
 * This is needed for stateful calculators where each node needs its own
 * calculator instance (to avoid sharing state between nodes).
 *
 * For stateless calculators, the factory can return the same instance.
 * For stateful calculators, the factory creates a new instance each time.
 *
 * Example:
 * <pre>
 * // Stateless - shared instance
 * CalculatorFactory addFactory = () -> ADD_CALCULATOR;
 *
 * // Stateful - new instance per node
 * CalculatorFactory smaFactory = () -> new SMACalculator(10);
 * </pre>
 */
@FunctionalInterface
public interface CalculatorFactory {

    /**
     * Creates a new Calculator instance.
     *
     * @return a Calculator (may be shared or unique depending on statefulness)
     */
    Calculator create();

    /**
     * Returns whether this factory produces stateful calculators.
     * Default: false (stateless)
     */
    default boolean isStateful() {
        return false;
    }

    /**
     * Creates a factory for a stateless calculator (singleton pattern).
     */
    static CalculatorFactory stateless(Calculator calculator) {
        return new CalculatorFactory() {
            @Override
            public Calculator create() {
                return calculator;
            }

            @Override
            public boolean isStateful() {
                return false;
            }
        };
    }

    /**
     * Creates a factory for a stateful calculator (new instance each time).
     */
    static CalculatorFactory stateful(CalculatorFactory factory) {
        return new CalculatorFactory() {
            @Override
            public Calculator create() {
                return factory.create();
            }

            @Override
            public boolean isStateful() {
                return true;
            }
        };
    }
}
