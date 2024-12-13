package examples;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * A mutex is a special case of a semaphore, used to ensure mutual exclusion.
 * You can create a mutex using a Semaphore with only one permit.
 * In this example, we'll create a mutex to protect access to a shared counter:
 */
public class MutexExample {
    private static int counter = 0;
    private static Semaphore mutex = new Semaphore(1); // Mutex with 1 permit

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        Runnable worker = () -> {
            try {
                mutex.acquire(); // Acquire the mutex
                System.out.println(Thread.currentThread().getName() + " acquired the mutex.");
                int currentCounter = counter;
                Thread.sleep(1000); // Simulate some work
                counter = currentCounter + 1;
                System.out.println(Thread.currentThread().getName() + " incremented the counter to " + counter);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(Thread.currentThread().getName() + " released the mutex.");
                mutex.release(); // Release the mutex
            }
        };

        for (int i = 0; i < 4; i++) {
            executor.submit(worker);
        }

        executor.shutdown();
    }
}

