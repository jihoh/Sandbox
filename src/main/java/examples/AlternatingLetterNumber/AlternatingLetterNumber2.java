package examples.AlternatingLetterNumber;

public class AlternatingLetterNumber2 {

    private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static boolean isLetterTurn = true;

    private static final Object lock = new Object();

    public static void main(String[] args) {

        Runnable letterRunnable = () -> {
            for(char c : alphabet.toCharArray()){
                synchronized (lock){
                    while(!isLetterTurn){
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    System.out.print(c);
                    isLetterTurn = false;
                    lock.notifyAll();
                }
            }
        };

        Runnable numberRunnable = () -> {
            for(int i = 0; i < alphabet.length(); i++) {
                synchronized (lock) {
                    while(isLetterTurn){
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    System.out.println(i);
                    isLetterTurn = true;
                    lock.notifyAll();
                }
            }
        };

        Thread.ofPlatform().start(letterRunnable);
        Thread.ofPlatform().start(numberRunnable);
    }
}
