package examples.ThreadSafeCounter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainDriver {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(5);

        ThreadSafeCounter counter = new ThreadSafeCounter();

        for(int i = 0; i < 1_000_000; i++){
            pool.submit(counter::getAndIncrement);
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println(counter.get());
    }
}
