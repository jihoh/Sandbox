package examples.producer_consumer;

public class ProducerConsumer {

    private static final Object lock = new Object();

    private static int[] buffer;
    private static int count;

    static class Producer {
        void produce() {
            synchronized (lock) {
                while (isFull(buffer)) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                buffer[count++] = 1;
                lock.notify();
            }
        }
    }

    static class Consumer {
        void consume() {
            synchronized (lock) {
                while (isEmpty(buffer)) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                buffer[--count] = 0;
                lock.notify();
            }
        }
    }

    static boolean isEmpty(int[] buffer) {
        return count == 0;
    }

    static boolean isFull(int[] buffer) {
        return count == buffer.length;
    }

    public static void main(String[] args) throws InterruptedException {
        buffer = new int[10];
        count = 0;

        Consumer consumer = new Consumer();
        Producer producer = new Producer();

        Runnable produceTask = () -> {
            for (int i = 0; i < 50; i++) {
                producer.produce();
            }
            System.out.println("Done producing");
        };

        Runnable consumeTask = () -> {
            for (int i = 0; i < 50; i++) {
                consumer.consume();
            }
            System.out.println("Done consuming");
        };

        Thread consumerThread = new Thread(consumeTask);
        Thread producerThread = new Thread(produceTask);

        consumerThread.start();
        producerThread.start();

        consumerThread.join();
        producerThread.join();

        System.out.println("Data in buffer: " + count);
    }
}
