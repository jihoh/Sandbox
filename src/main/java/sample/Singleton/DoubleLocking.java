package sample.Singleton;

public class DoubleLocking {

    // volatile keyword ensures that multiple threads handle the instance variable correctly
    private static volatile DoubleLocking INSTANCE;

    // Private constructor prevents instantiation from other classes
    private DoubleLocking() {
        // Initialize instance variables here if needed
    }

    // Public method to provide access to the singleton instance
    public static DoubleLocking getInstance() {
        if(INSTANCE == null) { // to avoid overhead of aquiring the lock every time getInstance() is called
            synchronized (DoubleLocking.class) { // ensures that only one thread initializes the instance
                if(INSTANCE == null) {
                    INSTANCE = new DoubleLocking();
                }
            }
        }
        return INSTANCE;
    }
}
