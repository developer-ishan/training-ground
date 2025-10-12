package behavioral.visitor.file_system;

import behavioral.visitor.file_system.operations.FileListingVisitor;
import behavioral.visitor.file_system.operations.SizeCalculatorVisitor;
import behavioral.visitor.file_system.structure.File;
import behavioral.visitor.file_system.structure.Folder;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Level 0: root
        Folder root = new Folder("/");

        // Level 1
        Folder home = new Folder("home");
        Folder var = new Folder("var");
        Folder etc = new Folder("etc");

        // Level 2
        Folder user = new Folder("user");
        Folder logs = new Folder("logs");
        Folder conf = new Folder("conf");

        // Level 3
        Folder downloads = new Folder("downloads");
        Folder documents = new Folder("documents");
        Folder apache = new Folder("apache");

        // Level 4
        Folder pictures = new Folder("pictures");

        // Add files to folders (20 total)
        user.add(List.of(
                new File("file1.txt", 10),
                new File("file2.txt", 15),
                new File("file3.txt", 20)
        ));

        downloads.add(List.of(
                new File("movie.mp4", 1000),
                new File("game.iso", 1500)
        ));

        documents.add(List.of(
                new File("resume.docx", 50),
                new File("design.pdf", 75)
        ));

        pictures.add(List.of(
                new File("vacation.jpg", 120),
                new File("profile.png", 80)
        ));

        logs.add(List.of(
                new File("app.log", 200),
                new File("error.log", 180)
        ));

        apache.add(List.of(
                new File("httpd.conf", 5),
                new File("mime.types", 2)
        ));

        conf.add(List.of(
                new File("sys.conf", 3),
                new File("net.conf", 4)
        ));

        etc.add(new File("hosts", 1));

        // Build hierarchy
        documents.add(pictures); // Level 4
        user.add(documents);
        user.add(downloads);     // Level 3

        home.add(user);          // Level 2
        var.add(logs);
        conf.add(apache);
        etc.add(conf);

        root.add(List.of(home, var, etc)); // Level 1
        /**
         * /
         * ├── home
         * │   └── user
         * │       ├── file1.txt
         * │       ├── file2.txt
         * │       ├── file3.txt
         * │       ├── downloads
         * │       │   ├── movie.mp4
         * │       │   └── game.iso
         * │       └── documents
         * │           ├── resume.docx
         * │           ├── design.pdf
         * │           └── pictures
         * │               ├── vacation.jpg
         * │               └── profile.png
         * ├── var
         * │   └── logs
         * │       ├── app.log
         * │       └── error.log
         * └── etc
         *     ├── hosts
         *     └── conf
         *         ├── sys.conf
         *         ├── net.conf
         *         └── apache
         *             ├── httpd.conf
         *             └── mime.types
         * */

        FileListingVisitor fileListingVisitor = new FileListingVisitor();
        fileListingVisitor.visit(root);
        fileListingVisitor.reset();

        SizeCalculatorVisitor sizeVisitor = new SizeCalculatorVisitor();
        root.accept(sizeVisitor);
        System.out.println("Total size: " + sizeVisitor.getSize() + " KB");
    }
}
