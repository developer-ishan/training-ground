package behavioral.iterator;

public interface Iteratable<Type> {
    Type next();
    boolean hasNext();
}
