package sample;

import java.util.Arrays;

public enum Error {

    A("a"), B("b"), C("c");

    public final String small;

    private Error(String letter) {
        this.small = letter;
    }

    public static boolean isError(String letter) {
        return Arrays.stream(Error.values()).anyMatch(x -> x.small.equals(letter));
    }
}
