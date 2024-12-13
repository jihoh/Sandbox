package examples.FooBar;

import java.util.concurrent.atomic.AtomicBoolean;

public class FooBar {

    public static void main(String[] args) throws InterruptedException {

        int n = 3;
        AtomicBoolean fooTurn = new AtomicBoolean(true);

        Object lock = new Object();

        Runnable foo = () -> {
            synchronized (lock){
                for(int i = 0; i < n; i++){
                    while(!fooTurn.get()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.print("foo");
                    fooTurn.set(false);
                    lock.notifyAll();
                }
            }
        };

        Runnable bar = () -> {
            synchronized (lock){
                for(int i = 0; i < n; i++){
                    while(fooTurn.get()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.print("bar");
                    fooTurn.set(true);
                    lock.notifyAll();
                }
            }
        };

        Thread fooPrinter = new Thread(foo);
        Thread barPrinter = new Thread(bar);

        fooPrinter.start();
        barPrinter.start();

        fooPrinter.join();
        barPrinter.join();
    }
}
