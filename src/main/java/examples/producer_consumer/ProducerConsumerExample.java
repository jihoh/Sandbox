package examples.producer_consumer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class Producer implements Runnable {
    private final BlockingQueue<String> queue;

    Producer(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                String item = "Item " + i;
                queue.put(item);
                System.out.println("Produced: " + item);
                Thread.sleep(100);
            }
            // Signal the consumer to stop by adding a "poison pill"
            queue.put("STOP");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Consumer implements Runnable {
    private final BlockingQueue<String> queue;

    Consumer(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String item = queue.take();
                if ("STOP".equals(item)) {
                    // Stop consuming when encountering the "poison pill"
                    break;
                }
                System.out.println("Consumed: " + item);
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class ProducerConsumerExample {
    public static void main(String[] args) {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);

        Producer producer = new Producer(queue);
        Consumer consumer = new Consumer(queue);

        new Thread(producer).start();
        new Thread(consumer).start();
    }
}