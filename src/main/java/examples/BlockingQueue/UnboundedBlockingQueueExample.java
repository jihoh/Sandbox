package examples.BlockingQueue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Predicate;

public class UnboundedBlockingQueueExample {

    public static void main(String[] args) {

    }

}

// unbounded has no size. wait when empty, but no full condition.
class UnboundedBlockingQueue<T> {

    private final Queue<T> queue = new LinkedList<>();

    public void put(T t){
        synchronized (queue){
            queue.add(t);
            queue.notifyAll(); // ALWAYS when putting.. you need this.
        }
    }

    public T take() throws InterruptedException {
        synchronized (queue){
            while(queue.isEmpty()){
                queue.wait();
            }
            return queue.poll();
        }
    }
}
