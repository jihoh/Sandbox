package examples.producer_consumer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class BoundedBuffer_WaitNotify<E> {

    private final Object notEmpty = new Object();
    private final Object notFull = new Object();

    private final Queue<E> queue = new LinkedList<>();

    private final int capacity;
    private final AtomicInteger count;

    public BoundedBuffer_WaitNotify(int capacity){
        this.capacity = capacity;
        this.count = new AtomicInteger(0);
    }

    public void put(E x) throws InterruptedException {
        while(count.get() == capacity) {
            synchronized (notFull) {
                notFull.wait();
            }
        }

        synchronized (this){
            queue.add(x);
        }
        count.getAndIncrement();

        synchronized (notEmpty) {
            notEmpty.notify();
        }
    }

    public E take() throws InterruptedException {
        while(count.get() == 0){
            synchronized (notEmpty){
                notEmpty.wait();
            }
        }
        E x = queue.poll();
        count.decrementAndGet();
        synchronized (notFull) {
            notFull.notify();
        }
        return x;
    }
}
