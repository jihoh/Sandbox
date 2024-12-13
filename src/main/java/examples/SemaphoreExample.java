package examples;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * A semaphore can be used to limit the number of threads accessing a shared resource simultaneously.
 * In this example, we'll create a semaphore with 3 permits and 6 threads trying to acquire those permits:
 */
public class SemaphoreExample {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        Semaphore semaphore = new Semaphore(3);

        Runnable task = () -> {
          try {
              semaphore.acquire(); // Acquire a permit
              System.out.println(Thread.currentThread().getName() + " acquired a permit.");
              Thread.sleep(2000); // Simulate some work
          } catch (InterruptedException e) {
              e.printStackTrace();
          } finally {
              System.out.println(Thread.currentThread().getName() + " released a permit.");
              semaphore.release(); // Release a permit
          }
        };

        for(int i = 0; i < 6; i++) {
            executorService.submit(task);
        }

        executorService.shutdown();
    }
}
