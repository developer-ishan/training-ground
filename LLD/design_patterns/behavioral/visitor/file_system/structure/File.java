package behavioral.visitor.file_system.structure;

import behavioral.visitor.file_system.operations.Visitor;

public class File implements FileSystemElement{
    private final int size;
    private final String name;
    public File(String name, int size) {
        this.name = name;
        this.size = size;
    }
    public int getSize() {
        return size;
    }
    public String getName() {
        return name;
    }
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
