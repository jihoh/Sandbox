package examples;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CountDownLatch:
 *
 * A CountDownLatch is a synchronization construct that allows one or more threads to wait until a set of operations being performed by other threads completes.
 * It uses a count that is initialized during the construction of the CountDownLatch. Each time a task finishes, it calls countDown() to reduce the count by one.
 * Once the count reaches zero, all waiting threads are released and can proceed.
 * A CountDownLatch is a one-time use construct. Once the count reaches zero, it cannot be reset or reused.
 * There is no barrier action associated with a CountDownLatch.
 *
 * Diff between CountDownLatch and CyclicBarrier
 *
 * Here is a summary of the differences:
 *
 * Reusability: CyclicBarrier is reusable, while CountDownLatch is not.
 * Purpose: CyclicBarrier is used to synchronize a group of threads to wait for each other, while CountDownLatch is used for waiting until a set of operations completes.
 * Barrier action: CyclicBarrier supports an optional barrier action, while CountDownLatch does not.
 *
 */
public class CountDownLatchExample {

    public static void main(String[] args) throws InterruptedException {

        int count = 3;
        // Create a CountDownLatch with a count of 3
        CountDownLatch countDownLatch = new CountDownLatch(count);

        // Create an ExecutorService with a fixed thread pool of 3 threads
        ExecutorService executorService = Executors.newFixedThreadPool(count);

        // Submit 3 tasks to the ExecutorService
        for(int i = 0; i < count; i++) {
            executorService.submit(new Task(countDownLatch));
        }

        // Shut down the ExecutorService gracefully after all tasks are submitted
        executorService.shutdown();

        System.out.println("All tasks submitted, waiting for them to complete...");

        // Wait for the latch count to reach 0
        boolean result = countDownLatch.await(10, TimeUnit.SECONDS);
        if(result) {
            System.out.println("All tasks completed.");
        } else {
            System.out.println("Timed out waiting for tasks to be completed");
        }
    }

    static class Task implements Runnable {

        private final CountDownLatch countDownLatch;

        public Task(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            System.out.println("Task started: " + Thread.currentThread().getName());

            try {
                // Simulate a time-consuming operation
                Thread.sleep((long) (Math.random() * 5000));
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Task finished: " + Thread.currentThread().getName());

            // Decrement the latch count
            countDownLatch.countDown();
        }
    }
}
