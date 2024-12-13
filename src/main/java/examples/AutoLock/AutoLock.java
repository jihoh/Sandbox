package examples.AutoLock;

import java.util.concurrent.locks.Lock;

public class AutoLock implements AutoCloseable {

    private final Lock lock;

    public AutoLock(Lock lock) {
        this.lock = lock;
    }

    public AutoLock lock() {
        lock.lock();
        return this;
    }

    public AutoLock lockInterruptibly() throws InterruptedException {
        lock.lockInterruptibly();
        return this;
    }

    @Override
    public void close() {
        lock.unlock();
    }
}
