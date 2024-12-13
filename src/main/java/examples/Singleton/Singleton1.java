package examples.Singleton;

public class Singleton1 {

    private static Singleton1 INSTANCE = new Singleton1();

    public Singleton1 getInstance(){
        return INSTANCE;
    }
}
