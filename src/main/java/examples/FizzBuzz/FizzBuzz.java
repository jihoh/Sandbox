package examples.FizzBuzz;

import java.time.Duration;
import java.time.Instant;

/**
 * Problem Statement:
 * Write a multithreaded version of the FizzBuzz problem.
 * Create four threads:
 *  one prints "Fizz" - when div by 3
 *  another prints "Buzz" - when div by 5
 *  a third prints "FizzBuzz" - when div by 3 and 5
 *  and the last prints the numbers - otherwise
 * Each thread should print its respective output when appropriate.
 */
public class FizzBuzz {

    static final int num = 30;

    static volatile int i = 1;

    static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {

        Thread.ofPlatform().start(new Fizz());
        Thread.ofPlatform().start(new Buzz());
        Thread.ofPlatform().start(new Fizzbuzz());
        Thread.ofPlatform().start(new Number());


    }

    static class Fizz implements Runnable {
        @Override
        public void run() {
            synchronized (lock){
                while(i <= 30){
                    if(i%3 == 0 && i%5 != 0){
                        System.out.println("Fizz");
                        i++;
                        lock.notifyAll();
                    }else{
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    static class Buzz implements Runnable {
        @Override
        public void run() {
            synchronized (lock){
                while(i <= 30){
                    if(i%5 == 0 && i%3 != 0){
                        System.out.println("Buzz");
                        i++;
                        lock.notifyAll();
                    }else{
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    static class Fizzbuzz implements Runnable {
        @Override
        public void run() {
            synchronized (lock){
                while(i <= 30){
                    if(i%3 == 0 && i%5 == 0){
                        System.out.println("FizzBuzz");
                        i++;
                        lock.notifyAll();
                    }else{
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    static class Number implements Runnable {
        @Override
        public void run() {
            synchronized (lock){
                while(i <= 30){
                    if(i%3 != 0 && i%5 != 0){
                        System.out.println(i);
                        i++;
                        lock.notifyAll();
                    }else{
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

}
