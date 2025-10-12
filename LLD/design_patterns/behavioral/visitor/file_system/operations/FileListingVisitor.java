package behavioral.visitor.file_system.operations;

import behavioral.visitor.file_system.structure.File;
import behavioral.visitor.file_system.structure.FileSystemElement;
import behavioral.visitor.file_system.structure.Folder;

public class FileListingVisitor implements Visitor {

    int indentation = 0;
    @Override
    public void visit(File file) {
        indent();
        System.out.println("- " + file.getName() + " (" + file.getSize() + " KB)");
    }


    @Override
    public void visit(Folder folder) {
        indent();
        System.out.println("+ " + folder.getName());
        indentation++;
        for (FileSystemElement element : folder.getFileSystemElements()) {
            element.accept(this);
        }
        indentation--;
    }


    public void indent(){
        for (int i = 0; i < indentation; i++) {
            System.out.print("  ");
        }
    }

    public void reset() {
        indentation = 0;
    }
}
