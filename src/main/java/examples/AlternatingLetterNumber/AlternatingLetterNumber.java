package examples.AlternatingLetterNumber;

import java.util.concurrent.Semaphore;

/**
 * 9. Alternating Printing of Letters and Numbers
 * Problem Statement:
 * Create two threads in Java: one prints letters (A, B, C, ...) and the other prints numbers (1, 2, 3, ...).
 * The output should alternate between letters and numbers in sequence (A1B2C3...).
 */
public class AlternatingLetterNumber {

    public static void main(String[] args) throws InterruptedException {

        final Semaphore letterSemaphore = new Semaphore(1); // multi semaphore strategy is awesome for acting as switch between threads
        final Semaphore numberSemaphore = new Semaphore(0);

        char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        Runnable printLetters = () -> {
            for(char c : letters){
                try {
                    letterSemaphore.acquire();
                    System.out.print(c);
                    numberSemaphore.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        };

        Runnable printNumbers = () -> {
            for(int i = 1; i <= letters.length; i++){
                try {
                    numberSemaphore.acquire();
                    System.out.println(i);
                    letterSemaphore.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        };

        Thread.ofPlatform().start(printLetters);
        Thread.ofPlatform().start(printNumbers);
    }
}
