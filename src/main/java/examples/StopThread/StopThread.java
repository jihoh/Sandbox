package examples.StopThread;

public class StopThread {

    public static void main(String[] args) throws InterruptedException {
        MyThread thread = new MyThread();
        thread.start();
        Thread.sleep(5000);
        thread.interrupt();
        for(int i = 0; i < 100; i++){
            Thread.sleep(1000);
            System.out.println("Running in main");
        }
    }
}

class MyThread extends Thread{

    public MyThread(){
        super("MyThread");
    }

    @Override
    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            System.out.println(Thread.currentThread().getName() + ": Running My Thread");
            try {
                Thread.sleep(1000000);
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " is interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println(Thread.currentThread().getName() + " is cancelled");
    }
}