package examples.DeadLock;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class DeadlockExample {

    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            try {
                m1();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(() -> {
            try {
                m2();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        Thread.sleep(5000);
        detectDeadLock();
    }

    private static void detectDeadLock() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadMXBean.findDeadlockedThreads();
        if (threadIds != null) {
            System.out.println("Deadlock detected!");
            ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds);
            System.out.println("Deadlocked threads:");
            for (ThreadInfo threadInfo : threadInfos) {
                System.out.println(threadInfo.getThreadName());
            }
        } else {
            System.out.println("No deadlock detected.");
        }
    }

    private static void m1() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " Trying to acquire lock1");
        synchronized (lock1) {
            System.out.println(Thread.currentThread().getName() + " Acquired l1");
            Thread.sleep(1000);
            synchronized (lock2) {
                System.out.println(Thread.currentThread().getName() + " Acquired l2");
            }
        }
        System.out.println(Thread.currentThread().getName() + " Released lock1");
    }

    private static void m2() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " Trying to acquire lock2");
        synchronized (lock2) {
            System.out.println(Thread.currentThread().getName() + " Acquired l2");
            Thread.sleep(2000);
            synchronized (lock1) {
                System.out.println(Thread.currentThread().getName() + " Acquired l1");
            }
        }
        System.out.println(Thread.currentThread().getName() + " Released lock2");
    }
}
