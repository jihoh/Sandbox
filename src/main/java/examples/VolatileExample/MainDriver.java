package examples.VolatileExample;


/**
 * If not synchronized using volatile, then we can see three possible out puts
 * 1. 42 - as expected
 * 2. 0 - because of instruction reordering
 * 3. no output - because ready was cached and never prints
 */

public class MainDriver {

    private static int val; // this does not need to be volatile
    private static volatile boolean ready;

    public static void main(String[] args) {
        Thread.ofPlatform().start(() -> {
            while (!ready) Thread.yield();
            System.out.println(val);
        });
        // if no volatile, the below can be reordered. meaning, val can also print 0
        val = 42;
        ready = true;
    }
}
