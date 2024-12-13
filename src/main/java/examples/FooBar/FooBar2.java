package examples.FooBar;

import java.util.concurrent.Semaphore;

public class FooBar2 {

    public static void main(String[] args) throws InterruptedException {

        int n = 2;

        Semaphore fooSemaphore = new Semaphore(1);
        Semaphore barSemaphore = new Semaphore(0);

        Runnable foo = () -> {
            for(int i = 0; i < n; i++){
                try {
                    fooSemaphore.acquire();
                    System.out.print("foo");
                    barSemaphore.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Runnable bar = () -> {
            for(int i = 0; i < n; i++){
                try {
                    barSemaphore.acquire();
                    System.out.print("bar");
                    fooSemaphore.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Thread fooThread = new Thread(foo);
        Thread barThread = new Thread(bar);
        fooThread.start();
        barThread.start();
        fooThread.join();
        barThread.join();

    }

}
