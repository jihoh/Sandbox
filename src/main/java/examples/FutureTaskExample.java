package examples;

import java.util.concurrent.*;

/**
 * In this example, we create a Callable that computes the sum of the numbers from 1 to 10.
 * We then create a FutureTask to wrap the Callable and start the computation in a new thread.
 * The main thread can continue doing other work while the computation runs in the background.
 * Once the result is ready, we use the futureTask.get() method to retrieve it.
 */
public class FutureTaskExample {

    public static void main(String[] args) {
        // Create a Callable to perform a computation
        Callable<Integer> callable = () -> {
            System.out.println("Starting computation...");
            int sum = 0;
            for (int i = 1; i <= 10; i++) {
                sum += i;
                TimeUnit.SECONDS.sleep(1);
            }
            return sum;
        };

        // Create a FutureTask wrapping the Callable
        FutureTask<Integer> futureTask = new FutureTask<>(callable);

        // Start the computation in a new thread
        Thread computationThread = new Thread(futureTask);
        computationThread.start();

        try {
            // Do some other work while the computation is running
            System.out.println("Doing other work...");

            // Retrieve the result of the computation when it's ready
            Integer result = futureTask.get(5, TimeUnit.SECONDS);
            /*
            Yes, when you call futureTask.get() and the result is not ready yet, the method will block the current thread
            and wait until the result is available. In the example I provided, if the computation takes longer to finish,
            the main thread will block on the futureTask.get() call and wait for the result to become available before it can proceed.

            If you want to avoid blocking, you can use the get(long timeout, TimeUnit unit) method, which allows you to
            specify a timeout for waiting for the result. If the result is not available within the specified timeout,
            a TimeoutException will be thrown. This allows you to handle the situation when the result is not available
            in a timely manner, and you can decide how to proceed in that case.
             */
            // futureTask.cancel();
            // futureTask.isCancelled()
            // futureTask.isDone();
            System.out.println("Computation result: " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
