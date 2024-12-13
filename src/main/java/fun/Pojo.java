package fun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pojo {

    String name = "jiho";

    Object x = List.of("a", "b", "c");

    int[] k = {1,2,3};


    @Override
    public String toString() {
        return "Pojo{" +
                "name='" + name + '\'' +
                ", x=" + x +
                ", k=" + Arrays.toString(k) +
                '}';
    }
}
