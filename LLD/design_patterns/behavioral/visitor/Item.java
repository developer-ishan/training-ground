package behavioral.visitor;

public interface Item {
    void accept(Visitor visitor);
}
