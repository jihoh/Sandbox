package devoxx.designPatterns;

import java.util.function.Supplier;

import static devoxx.designPatterns.LazinessLambdaDemo.compute;

/*
    Lazy evaluation is to functional programming as polymorphism is to OOP

    Eager evaluation vs. short circuit

    myFunction1(Type value) - eager
    myFunction2(Supplier<Type> supplier) - lazy

    When do we pass value vs a functional interface to a method?
    - One consideration is lazy evaluation

 */
public class LazinessLambdaDemo {

    public static int compute(int x) {
        return x * 100;
    }

    public static void main(String[] args) {
        int value = 4;

        Eager<Integer> eager = new Eager<>(value);
        int val = compute(eager.get()); // eager evaluation done here

        Lazy<Integer> lazy = new Lazy<>(() -> compute(value)); // the evaluation is deferred, lazy evaluation

        if(val > 4 && lazy.get() > 100) { // the lazy evaluation is done here
            System.out.println("path 1");
        } else {
            System.out.println("path 2");
        }
    }

}

class Lazy<T> {
    private T instance;
    private Supplier<T> supplier;
    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }
    public T get() {
        if(instance == null) {
            return supplier.get();
        }
        return instance;
    }
}

class Eager<T> {
    private T instance;
    public Eager(T value) {
        instance = value;
    }
    public T get() {
        return instance;
    }
}
