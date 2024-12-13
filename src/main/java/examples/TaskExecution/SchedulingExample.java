package examples.TaskExecution;

import java.util.concurrent.*;

public class SchedulingExample {

    // the corePoolSize of 1 is ok in this example because we are just printing for multiple tasks.
    // BUT when tasks are long-running, then this will become a proiblem since tasks will be queueed and executed sequentially
    // SO corePoolSize should be bigger when handling tasks concurrently

    public static void main(String[] args) {
        ScheduledExecutorService schdeduler = Executors.newScheduledThreadPool(1);
        schdeduler.scheduleAtFixedRate(()-> System.out.println("testing"), 1, 1, TimeUnit.SECONDS);
        schdeduler.scheduleAtFixedRate(()-> System.out.println("testing2"), 1, 1, TimeUnit.SECONDS);
        schdeduler.scheduleAtFixedRate(()-> System.out.println("testing3"), 1, 1, TimeUnit.SECONDS);
    }
}
