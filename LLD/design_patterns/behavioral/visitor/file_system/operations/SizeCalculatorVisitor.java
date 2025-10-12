package behavioral.visitor.file_system.operations;

import behavioral.visitor.file_system.structure.File;
import behavioral.visitor.file_system.structure.FileSystemElement;
import behavioral.visitor.file_system.structure.Folder;

public class SizeCalculatorVisitor implements Visitor {
    private int size;

    public SizeCalculatorVisitor() {
        this.size = 0;
    }
    @Override
    public void visit(File file) {
        size += file.getSize();
    }

    @Override
    public void visit(Folder folder) {
        for (FileSystemElement element: folder.getFileSystemElements()){
            element.accept(this);
        }
    }

    public int getSize() {
        return size;
    }

    public void reset() {
        size = 0;
    }
}
