package examples.producer_consumer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProducerConsumerExample2 {

    public static void main(String[] args) {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

        Runnable produce = () -> {
            for(int i = 0; i < 10; i++){
                try {
                    queue.put(Integer.toString(i));
                    System.out.println(Thread.currentThread().getName() + " Produced: " + i);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Producer is interrupted");
                    break;
                }
            }
        };

        Runnable consume = () -> {
            while(true){
                try {
                    var x = queue.take();
                    System.out.println(Thread.currentThread().getName() + " Consumed: " + x);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Consumer is interrupted");
                    break;
                }
            }
        };

        for(int i = 0; i < 1; i++){
            new Thread(produce, "Producer-" + i).start();
        }
        for(int i = 0; i < 5; i++) {
            new Thread(consume, "Consumer-"+i).start();
        }
    }
}
