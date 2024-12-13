package sample.Singleton;

// enum constants are threadsafe
public enum EnumSingleton {
    INSTANCE;

    private int someValue;

    public int getSomeValue() {
        return someValue;
    }

    public void setSomeValue(int someValue) {
        this.someValue = someValue;
    }
}
