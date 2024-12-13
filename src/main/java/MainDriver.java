import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class MainDriver {
    public static void main(String[] args) throws InterruptedException {
        AtomicInteger i = new AtomicInteger(0);
        Thread.ofPlatform().start(() -> {
            while(!Thread.interrupted()) {
                System.out.println(i.getAndIncrement() + "s");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).setDaemon(true);
        Thread t = Thread.ofPlatform().start(() -> {
            while(!Thread.interrupted()) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        TimeUnit.SECONDS.sleep(5);
        t.interrupt(); // sleep interrupted
        System.out.println("OVER");
    }

}