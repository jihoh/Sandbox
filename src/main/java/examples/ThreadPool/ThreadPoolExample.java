package examples.ThreadPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolExample {

    public static void main(String[] args) {
        ThreadPool pool = new ThreadPool(5);
        for(int i = 0; i < 1_000_000; i++){
            int finalI = i;
            pool.submit(() -> System.out.println(Thread.currentThread().getName() + ":  task: " + finalI));
        }
        pool.shutdown();
    }
}

/**
 * This has the queue of tasks and a set of threads
 */
class ThreadPool {

    private final BlockingQueue<Runnable> queue;
    private final Worker[] workers;
    private volatile boolean isShutdown = false;

    public ThreadPool(int n) {
        this.queue = new LinkedBlockingQueue<>();
        this.workers = new Worker[n];
        for(int i = 0; i < n; i++){
            workers[i] = new Worker();
            workers[i].start();
        }
    }

    public void submit(Runnable task) {
        if(!isShutdown){
            queue.add(task);
        }
    }

    public void shutdown(){
        isShutdown = true;
        while(!queue.isEmpty()){
         // wait until queue is empty
        }
        for(Worker worker : workers){
            worker.interrupt();
        }
    }

    private class Worker extends Thread {

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    Runnable task = queue.take();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}

