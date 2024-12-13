package java21;

import java.util.Deque;
import java.util.concurrent.TimeUnit;

public class MainDriver {

    public static void main(String[] args) {
        Thread t1 = Thread.ofPlatform().start(() -> {
            int i = 0;
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println(i++ + "sec");
                    TimeUnit.SECONDS.sleep(1);  // 2. when here and interrupted, then immediately InterruptedException and sets interrupted to "false"
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 3. set the interrupted to true
                }
            }
        });

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Thread t2 = Thread.ofPlatform().start(t1::interrupt); // 1. when you call interrupt on thread, interrupted status is true
        System.out.println("Complete");


    }
}


