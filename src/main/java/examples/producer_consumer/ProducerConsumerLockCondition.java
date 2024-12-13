package examples.producer_consumer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumerLockCondition {

    private final static Queue<Integer> queue = new LinkedList<>();

    private final static Lock lock = new ReentrantLock();
    private final static Condition notFull = lock.newCondition();
    private final static Condition notEmpty = lock.newCondition();

    public static void main(String[] args) {

        Runnable produce = () -> {
            for (int i = 0; i < 1_000; i++) {
                lock.lock();
                try {
                    while (queue.size() == 10) {
                        notFull.await();
                    }
                    queue.add(i);
                    notEmpty.signalAll();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
        };

        Runnable consume = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                lock.lock();
                try {
                    while (queue.isEmpty()) {
                        notEmpty.await();
                    }
                    var x = queue.remove();
                    System.out.println(x);
                    notFull.signalAll();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
        };

        new Thread(produce).start();
        new Thread(consume).start();
    }

}
