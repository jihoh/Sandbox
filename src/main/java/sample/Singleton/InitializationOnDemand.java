package sample.Singleton;

// lazy-initialized and thread-safe
public class InitializationOnDemand {

    private InitializationOnDemand() {

    }

    private static class Holder { // only get called once when Holder.INSTANCE is called
        private static final InitializationOnDemand INSTANCE = new InitializationOnDemand();
    }

    public static InitializationOnDemand getInstance() {
        return Holder.INSTANCE;
    }
}
