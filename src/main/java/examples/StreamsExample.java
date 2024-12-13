package examples;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StreamsExample {

    public static void main(String[] args) {

        // map
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);
        System.out.println(list);

        List<Integer> result = list.stream()
                .filter(x -> x%2 == 0)
                .map(x -> x*2)
                .collect(Collectors.toList());
        System.out.println(result);

        // flatmap
        List<List<Integer>> nestedList = Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList(4, 5, 6), Arrays.asList(7, 8, 9));
        System.out.println(nestedList);

        List<Integer> flattenedList = nestedList.stream().flatMap(x->x.stream()).filter(y -> y % 2==0).collect(Collectors.toList());
        System.out.println(flattenedList);

        // reduce
        Optional<Integer> x = list.stream().reduce(Math::max);
        Optional<Integer> y = list.stream().reduce((a, b) -> a > b ? a : b);
        System.out.println(x.get());
        System.out.println(y.get());
    }
}
