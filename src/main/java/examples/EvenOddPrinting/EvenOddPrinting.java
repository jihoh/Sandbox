package examples.EvenOddPrinting;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Title: Implementing Odd and Even Number Printing Using Multiple Threads
 *
 * Objective:
 * Write a Java program that uses two threads to print numbers sequentially.
 * One thread should print only the odd numbers, while the other thread prints only the even numbers.
 * The output should be in sequential order without any missing or duplicate numbers.
 *
 * Requirements:
 *
 * Use Java's concurrency mechanisms to achieve the desired output.
 * Ensure the two threads coordinate their execution to print the numbers in the correct sequence.
 * The main thread should wait for the other threads to complete before terminating.
 */
public class EvenOddPrinting {

    private static final Object lock = new Object();

    private static final AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {

        Thread oddThread = new Thread(new MyRunnable(x -> x%2==1), "Odd Thread");
        Thread evenThread = new Thread(new MyRunnable(x -> x%2==0), "Even Thread");

        oddThread.start();
        evenThread.start();

        oddThread.join();
        evenThread.join();
    }

    static class MyRunnable implements Runnable{

        private final Predicate<Integer> predicate;

        MyRunnable(Predicate<Integer> predicate){
            this.predicate = predicate;
        }

        @Override
        public void run() {
            synchronized (lock) {
                while (!Thread.currentThread().isInterrupted()) {
                    while (!predicate.test(counter.get())) {
                        try {
                            lock.wait();
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    System.out.println(Thread.currentThread().getName() + ": " + counter.get());
                    counter.incrementAndGet();
                    lock.notifyAll();
                }
            }
        }
    }
}
