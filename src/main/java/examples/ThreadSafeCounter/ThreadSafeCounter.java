package examples.ThreadSafeCounter;

public class ThreadSafeCounter {

    private int count; // doesn't need to be volatile. synchronized ensures visibility and atomicity

    public ThreadSafeCounter(){
        count = 0;
    }

    public synchronized int incrementAndGet(){
        return ++count;
    }

    public synchronized int getAndIncrement(){
        return count++;
    }

    public synchronized int get(){
        return count;
    }

}