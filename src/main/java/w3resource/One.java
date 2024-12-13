package w3resource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// 1. Write a Java program to create and start multiple threads that increment a shared counter variable concurrently.
public class One {

    public static void main(String[] args) {

        AtomicInteger counter = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(100);

        Runnable task = () -> {
            counter.getAndIncrement();
            System.out.println("Thread: " + Thread.currentThread().getName() + " counter: " + counter.get());
        };

        for(int i = 0; i < 10000; i++) {
            pool.submit(task, Thread.currentThread().getName()+i);
        }
        pool.shutdown(); // prev submitted tasks are executed but no new tasks accepted
        try {
            // we HAVE to wait for the pool to process all submitted tasks
            if(!pool.awaitTermination(10, TimeUnit.SECONDS)){
                pool.shutdownNow(); // Force shutdown if tasks are not completed
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("Final count: " + counter.get());

    }
}
