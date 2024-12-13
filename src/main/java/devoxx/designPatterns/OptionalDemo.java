package devoxx.designPatterns;

import java.util.Optional;

public class OptionalDemo {

    // null is a smell
    // a good design reads like a story and not like a puzzle
    // make the code obvious
    // Effective Java: do not return a null, instead return an empty collection
    // What if we have a single value?
    // In the past we returned null. Now we should return Optional<T>

    // If a method will always have a single value as a result, please DO NOT use Optional
    // If a method may or may not have a single value as a result, then use Optional
    // If the result is a collection, then don't use Optional - just return empty collection

    // Don't use Optional<T> as a parameter to methods - if needed, use overloading instead
    // There is little reason to use Optional as a field

    // In general.. don't do null checks for every parameter they receive.. that is paranoia
    // Don't be that scared in the code.

    public static Optional<String> getName() {
        return Optional.empty();
    }

    public static void main(String[] args) {
        var result = getName();

        System.out.println(result.orElse("not found"));

        // do not do this.. get() does not reveal it's intention
        System.out.println(result.get());

        // if at all you need to use get, then use orElseThrow instead
        System.out.println(result.orElseThrow());
    }
}
