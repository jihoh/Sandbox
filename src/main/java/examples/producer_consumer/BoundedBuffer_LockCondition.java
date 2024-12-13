package examples.producer_consumer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer_LockCondition<E> {

    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    private final Queue<E> queue = new LinkedList<>();

    private final int capacity;

    public BoundedBuffer_LockCondition(int capacity){
        this.capacity = capacity;
    }

    public void put(E x) throws InterruptedException {
        lock.lock();
        try {
            while(queue.size() == capacity) { // while instead of if. if wake up spuriously, recheck condition
                notFull.await();
            }
            queue.add(x);
            notEmpty.signal(); // better performance than signalAll
        }finally{
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {
        lock.lock();
        try{
            while(queue.isEmpty()){
                notEmpty.await();
            }
            E x = queue.poll();
            notFull.signal();
            return x;
        }finally {
            lock.unlock();
        }
    }
}
