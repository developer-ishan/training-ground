package creational.abstract_factory.components;

public class Component {
    private final String id;
    public Component(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
}
