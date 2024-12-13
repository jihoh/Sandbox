package w3resource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//6. Write a Java program that uses the CountDownLatch class to synchronize the start and finish of multiple threads.
public class Six {

    private static final CountDownLatch countDownLatch = new CountDownLatch(3);

    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            countDownLatch.countDown();
            try {
                Thread.sleep(10000);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("Done sleeping 2s");
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }}).start();
        new Thread(() -> {
            try {
                Thread.sleep(10000);
                System.out.println("Done sleeping 10s");
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }}).start();
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                System.out.println("Done sleeping 5s");
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }}).start();

        countDownLatch.await();

        System.out.println("Done");

    }
}
