package examples.AutoLock;

import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {

    private final AutoLock lock = new AutoLock(new ReentrantLock());

    private int balance;

    public BankAccount(int balance) {
        this.balance = balance;
    }

    public void deposit(int amount){
        try(AutoLock al = lock.lock()){
            balance += amount;
        }
    }

    public void withdraw(int amount){
        deposit(-amount);
    }

    public int getBalance(){
        try(AutoLock al = lock.lock()){
            return balance;
        }
    }
}
