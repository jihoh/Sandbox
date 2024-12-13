package examples;

import java.util.concurrent.atomic.AtomicInteger;

public class UniqueThreadIdGenerator {

    private static final AtomicInteger nextId = new AtomicInteger(0);

    private static final ThreadLocal<Integer> threadId = ThreadLocal.withInitial(nextId::getAndIncrement);

    public static int getCurrentThreadId(){
        return threadId.get();
    }
}
