package devoxx.streams;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class MainDriver {

    // implement some race condition logic
    public static LongAdder count = new LongAdder();
    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[5];
        Runnable task = () -> {
            long localCount = 0;
            for (int j = 0; j < 100_000_000; j++) {
                localCount++;
            }
            count.add(localCount);
        };

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(task);
            threads[i].start();
        }
        for (int k = 0; k < threads.length; k++) {
            threads[k].join();
        }

        System.out.println(count);
    }
}
