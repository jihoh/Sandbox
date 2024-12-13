package w3resource;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//3. Write a Java program that uses the ReentrantLock class to synchronize access to a shared resource among multiple threads.
public class Three {

    private static final Lock lock = new ReentrantLock();

    private static int counter = 0;

    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws InterruptedException {
        for(int i = 0; i < 10000; i++){
            pool.submit(Three::incrementCounter);
        }
        pool.shutdown();
        if(!pool.awaitTermination(10, TimeUnit.SECONDS)){
            pool.shutdownNow();
            System.out.println("Time taken too long");
            return;
        }

        System.out.println("Final counter value: " + counter);
    }

    private static void incrementCounter(){
        lock.lock();
        try{
            counter++;
        }finally{
            lock.unlock();
        }
    }
}
