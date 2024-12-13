package examples;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CyclicBarrier:
 *
 * A CyclicBarrier allows a group of threads to wait for each other to reach a common barrier point.
 * Once all threads reach the barrier, they can proceed with the next part of their tasks.
 * The barrier is reusable, meaning it can be used multiple times in a cyclic manner by resetting its internal count.
 * It has an optional barrier action that can be executed when all threads reach the barrier.
 * A CyclicBarrier can be reset or broken, which can release all waiting threads.
 */

public class CyclicBarrierExample {

    public static void main(String[] args) {
        int numberOfThreads = 5;
        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads, () -> System.out.println("All threads reached the barrier. Continue processing."));

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(new Thread(new Task(barrier), "Thread " + (i + 1)));
        }

        executorService.shutdown();
    }

    static class Task implements Runnable {
        private final CyclicBarrier barrier;

        public Task(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.println(threadName + " has started.");

            try {
                Thread.sleep((long) (Math.random() * 1000)); // Simulate some processing time
                System.out.println(threadName + " has reached the barrier.");

                // Wait for other threads to reach the barrier
                barrier.await();

                System.out.println(threadName + " has crossed the barrier and continues processing.");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
}