package behavioral.visitor.file_system.operations;

import behavioral.visitor.file_system.structure.File;
import behavioral.visitor.file_system.structure.Folder;

public interface Visitor {
    public void visit(File file);
    public void visit(Folder folder);
}
