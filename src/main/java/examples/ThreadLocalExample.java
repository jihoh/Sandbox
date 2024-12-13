package examples;

/**
 * ThreadLocal is a Java class that allows you to create thread-local variables, which are variables that are specific to each thread executing a piece of code.
 * Each thread accessing a ThreadLocal variable will have its own, independently initialized copy of the variable.
 * This can be useful when you want to maintain separate state for each thread, especially in a multi-threaded environment.
 *
 * ThreadLocal provides a way to achieve thread-safety without requiring synchronization, as each thread has its own separate copy of the variable.
 * This can lead to better performance in some cases when compared to using synchronized methods or blocks.
 */
public class ThreadLocalExample {

    // Create a ThreadLocal variable of type Integer, initialized to 0
    private static final ThreadLocal<Integer> threadLocalCounter = ThreadLocal.withInitial(() -> 0);

    public static void main(String[] args) {
        // Create three threads that each increment the counter 5 times
        Runnable task = () -> {
            for (int i = 0; i < 5; i++) {
                int currentValue = threadLocalCounter.get();
                threadLocalCounter.set(currentValue + 1);
                System.out.println(Thread.currentThread().getName() + ": " + threadLocalCounter.get());
            }
        };

        Thread t1 = new Thread(task, "Thread-1");
        Thread t2 = new Thread(task, "Thread-2");
        Thread t3 = new Thread(task, "Thread-3");

        t1.start();
        t2.start();
        t3.start();
    }
}
