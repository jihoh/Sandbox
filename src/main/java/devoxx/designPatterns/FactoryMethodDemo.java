package devoxx.designPatterns;

/*
    Factory Method using default methods

    What is the worst keyword in Java from polymorphism point of view? new

    when using new, you are tight couping the type

    Abstract Factory vs Factory Method
    - Factory Method: A class or an interface relies on a derived class to provide the implementation whereas
        the base provides the common behavior. Uses inheritance as a design tool

 */
public class FactoryMethodDemo {
    public static void call(Person person) {
        person.play();
    }

    public static void main(String[] args) {
        call(new DogPerson());
        call(new CatLover());
    }

}

interface Person {
    Pet getPet();
    default void play() {
        System.out.println("playing with " + getPet());
    }
}
class DogPerson implements Person {
    private Dog dog = new Dog();
    @Override
    public Pet getPet() {
        return dog;
    }
}
class CatLover implements Person {
    private Cat cat = new Cat();

    @Override
    public Pet getPet() {
        return cat;
    }
}


interface Pet {}
class Dog implements Pet{};
class Cat implements Pet{};