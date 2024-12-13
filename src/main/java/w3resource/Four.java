package w3resource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

//4. Write a Java program to demonstrate Semaphore usage for thread synchronization.
public class Four {

    private static final int NUM_PERMITS = 10;

    private static final Semaphore semaphore = new Semaphore(NUM_PERMITS);

    private static final ExecutorService pool = Executors.newFixedThreadPool(5);

    public static void main(String[] args) throws InterruptedException {
        Runnable task  = () -> {
            try {
                System.out.println(Thread.currentThread().getName() + " ACQUIRE");
                semaphore.acquire();
                Thread.sleep(1000);
                semaphore.release();
                System.out.println(Thread.currentThread().getName() + " RELEASE");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        for(int i = 0; i < 1000; i++){
            pool.submit(task);
        }
        pool.shutdown();
        while(!pool.awaitTermination(10, TimeUnit.SECONDS)){
            Thread.currentThread().interrupt();
            return;
        }

        System.out.println("Completed");
    }
}
