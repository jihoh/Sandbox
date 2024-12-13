package devoxx.designPatterns;

/*
    Strategy pattern
      - we want to vary a small part of an algorithm while keeping the rest of the algorithm the same

      Language design is program design

      Design Patterns often kick in to fill the gaps of a programming language

      A more power a language is, the less we talk about design patterns as these naturally become the features of the language

      In the past, how did we use strategy?
      We created an interface and then a bunch of classes
      Then wire them together often use factories

      Lambdas are lightweight strategies

      Strategies are often a single method or function
      So, functional interfaces and lambdas work really well
 */

import java.util.List;
import java.util.function.Predicate;

public class LightweightPatternDemo {

    public static int totalValues(List<Integer> numbers, Predicate<Integer> selector) {
        return numbers.stream()
                .filter(selector)
                .mapToInt(e -> e)
                .sum();
    }

    public static boolean isOdd(int number) {
        return number %2 != 0;
    }

    public static boolean isEven(int number) {
        return number %2 == 0;
    }

    public static void main(String[] args) {
        var numbers = List.of(1, 2, 3,4 , 5, 6, 7, 8, 9, 10);
        System.out.println(totalValues(numbers, ignore -> true));
        System.out.println(totalValues(numbers, LightweightPatternDemo::isOdd));
        System.out.println(totalValues(numbers, LightweightPatternDemo::isEven));
    }
}
