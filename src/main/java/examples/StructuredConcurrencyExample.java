package examples;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StructuredConcurrencyExample {

    public static void main(String[] args) {
        // Create an ExecutorService with a fixed thread pool

        try(ExecutorService executorService = Executors.newFixedThreadPool(4)){
            // Create and submit tasks using CompletableFuture
            CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
                System.out.println(Thread.currentThread().getName() + "Task 1 started");
                // Perform some computation...
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread().getName() + "Task 1 completed");
            }, executorService);

            CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
                System.out.println(Thread.currentThread().getName() + "Task 2 started");
                // Perform some computation...
                System.out.println(Thread.currentThread().getName() + "Task 2 completed");
            }, executorService);

            // Wait for all tasks to complete before continuing
            CompletableFuture.allOf(task1, task2).join();

            System.out.println("All tasks completed");
        }
    }
}
