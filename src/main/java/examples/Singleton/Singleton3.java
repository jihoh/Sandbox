package examples.Singleton;

/**
 * This approach has serialization and thread-safety guaranteed by the enum implementation itself,
 * which ensures internally that only the single instance is available, correcting the problems
 * pointed out in the class-based implementation.
 */
public enum Singleton3 {

    INSTANCE;

    public Singleton3 getInstance(){
        return INSTANCE;
    }
}
