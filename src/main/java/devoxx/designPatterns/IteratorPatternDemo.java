package devoxx.designPatterns;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Functional pipeline offers internal iterators
// is less complex, easy to modify, easy to understand..
// but it is very important that we make the functional pipeline pure.
// avoid shared mutable variables

// A pure function:
// 1. Idempotent - it returns the same result for the same input and does not have any side-effects
//  1) It does not change any state that is visible outside
//  2) IT does not depend on anything outside that may possibly change

// Functional programming relies ton lazy evaluation for efficiency.
// Lazy evaluation and parallel execution rely on immutability and purity of functions for correctness

// FP emphasizes immutability and purity, not because it is fashionable, but because it is essential

public class IteratorPatternDemo {

    public static void main(String[] args) {


        List<String> names = List.of("Sam", "Mary", "Bill", "James");
        var result2 = new ArrayList<String>();
        names.stream()
                .filter(name -> name.length() == 4)
                .map(String::toString)
                // .map(name -> performImpureOperation(name)) // AVOID
                .forEach(result2::add); // BAD IDEA
        // The functional pipeline is NOTE pure
        // We are doing "shared" mutability
        // The result may be unpredictable if we ever change this code to run in parallel or by adding .parallel() or
        // by changing .stream() to .parallelStream

        // DO THIS INSTEAD
//        List<String> result2 = names.stream()
//                .filter(name -> name.length() == 4)
//                .map(String::toString) // This map is redundant if 'names' is already a List<String>
//                // .map(name -> performImpureOperation(name)) // Avoid if impure
//                .collect(Collectors.toList()); // Collecting results in a new list
    }
}
