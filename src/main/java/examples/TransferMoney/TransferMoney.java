package examples.TransferMoney;

public class TransferMoney {

    public static void main(String[] args) {

    }

    private static final Object tieLock = new Object();

    // rule is bigger hash synchronize first
    private static boolean transferMoney(Object from, Object to, double amount) {
        int fromHash = System.identityHashCode(from); // but if object has like a unique ID then use that
        int toHash = System.identityHashCode(to);
        if (fromHash > toHash) {
            synchronized (from){
                synchronized (to){
                    return doActualTransfer(from, to, amount);
                }
            }
        } else if (toHash > fromHash) {
            synchronized (to){
                synchronized (from){
                    return doActualTransfer(from, to, amount);
                }
            }
        } else {
            synchronized (tieLock){
                synchronized (from){
                    synchronized (to){
                        return doActualTransfer(from, to, amount);
                    }
                }
            }
        }
    }

    private static boolean doActualTransfer(Object from, Object to, double amount) {
        return true;
    }
}