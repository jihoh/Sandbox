package examples.BlockingQueue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBlockingQueueExample {

    public static void main(String[] args) {
        BoundedBlockingQueue<Integer> blockingQueue = new BoundedBlockingQueue<>(5);

        Runnable produce = () -> {
            for(int i = 0; i < 10; i++){
                try {
                    blockingQueue.enqueue(i);
                    System.out.println(Thread.currentThread().getName() + " produced " + i);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Runnable consume = () -> {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    int i = blockingQueue.dequeue();
                    System.out.println(Thread.currentThread().getName() + " consumed " + i);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Thread.ofPlatform().start(produce).setName("Producer1");
        Thread.ofPlatform().start(produce).setName("Producer2");
        Thread.ofPlatform().start(produce).setName("Producer3");

        Thread.ofPlatform().start(consume).setName("Consumer1");
        Thread.ofPlatform().start(consume).setName("Consumer2");
    }
}

class BoundedBlockingQueue<T> {

    private final Lock lock = new ReentrantLock();
    private final Condition isNotEmpty = lock.newCondition();
    private final Condition isNotFull = lock.newCondition();

    private final Queue<T> queue;

    private final int capacity;

    public BoundedBlockingQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new LinkedList<>();
    }

    public void enqueue(T t) throws InterruptedException { // let the caller deal with it
        lock.lock();   // previously i accidentally did synchronized(lock), which gives IllegalMonitorStateException. can't mix
        try{
            while(queue.size() == capacity){
                isNotFull.await();
            }
            queue.add(t);
            isNotEmpty.signalAll();
        }finally{
            lock.unlock();
        }
    }

    public T dequeue() throws InterruptedException {
        lock.lock();
        try{
            while(queue.isEmpty()){
                isNotEmpty.await();
            }
            T t = queue.poll();
            isNotFull.signalAll();
            return t;
        }finally{
            lock.unlock();
        }
    }
}
