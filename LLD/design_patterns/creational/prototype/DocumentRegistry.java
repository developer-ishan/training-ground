package creational.prototype;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DocumentRegistry {
    private final Map<String, Document> registry = new HashMap<>();

    public void register(String type, Document document) {
        registry.put(type, document);
    }

    public Document getClonedDocument(String type) {
        return Optional.ofNullable(registry.get(type))
                .map(Document::clone)
                .orElse(null);
    }
}
