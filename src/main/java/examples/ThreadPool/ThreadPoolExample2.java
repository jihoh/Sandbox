package examples.ThreadPool;

import java.util.LinkedList;
import java.util.Queue;

public class ThreadPoolExample2 {

    public static void main(String[] args) {
        ThreadPool2 threadPool = new ThreadPool2(10);
        for (int i = 0; i < 1_000; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                System.out.println(Thread.currentThread().getName() + ": " + finalI);
            });
        }
    }
}

class ThreadPool2 {

    private final Queue<Runnable> queue;
    private final Worker[] workers;

    public ThreadPool2(int count) {
        this.queue = new LinkedList<>();
        this.workers = new Worker[count];
        for (int i = 0; i < count; i++) {
            workers[i] = new Worker("Worker-" + i);
            workers[i].start();
        }
    }

    public void execute(Runnable task) {
        synchronized (queue) {
            queue.add(task);
            queue.notifyAll();
        }
    }

    private class Worker extends Thread {

        public Worker(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    Runnable task = queue.remove();
                    task.run();
                }
            }
        }
    }
}
