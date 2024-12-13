package examples.Singleton;

public class Singleton2 {

    private static Singleton2 INSTANCE;

    public static Singleton2 getInstance(){
        if(INSTANCE == null){
            synchronized (Singleton2.class){
                if(INSTANCE == null){
                    INSTANCE = new Singleton2();
                }
            }
        }
        return INSTANCE;
    }
}
