package w3resource;

import java.util.LinkedList;
import java.util.Queue;

//2. Write a Java program to create a producer-consumer scenario using the wait() and notify() methods for thread synchronization.
public class Two {

    private final static int BUFFER_SIZE = 5;

    private final static Queue<Integer> buffer = new LinkedList<>();

    public static void main(String[] args) {

        Runnable produce = () -> {
            int val = 0;
            while (true) {
                synchronized (buffer) {
                    while (buffer.size() == BUFFER_SIZE) {
                        try {
                            buffer.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    System.out.println("Produced: " + val);
                    buffer.add(val++);
                    buffer.notifyAll();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        };

        Runnable consume = () -> {
            while (true) {
                synchronized (buffer) {
                    while (buffer.isEmpty()) {
                        try {
                            buffer.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    var x = buffer.poll();
                    System.out.println("Consumed: " + x);
                    buffer.notifyAll();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        };

        Thread producer = new Thread(produce);
        Thread consumer = new Thread(consume);
        producer.start();
        consumer.start();
    }

}
