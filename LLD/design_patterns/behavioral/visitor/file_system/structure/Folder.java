package behavioral.visitor.file_system.structure;

import behavioral.visitor.file_system.operations.Visitor;

import java.util.ArrayList;
import java.util.List;

public class Folder implements FileSystemElement{
    private final String name;
    private final ArrayList<FileSystemElement> fileSystemElements;

    public Folder(String name) {
        this.name = name;
        this.fileSystemElements = new ArrayList<>();
    }
    public String getName() {
        return name;
    }
    public ArrayList<FileSystemElement> getFileSystemElements() {
        return fileSystemElements;
    }
    public void add(FileSystemElement fileSystemElement) {
        fileSystemElements.add(fileSystemElement);
    }
    public void add(List<FileSystemElement> fileSystemElements) {
        this.fileSystemElements.addAll(fileSystemElements);
    }
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
