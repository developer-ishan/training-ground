package behavioral.visitor.file_system.structure;

import behavioral.visitor.file_system.operations.Visitor;

public interface FileSystemElement {
    void accept(Visitor visitor);
}
