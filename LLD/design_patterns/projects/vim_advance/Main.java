import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Editor editor = new Editor();
        if (args.length > 0) {
            try {
                editor.loadFile(args[0]);
            } catch (IOException e) {
                System.err.println("Failed to load file: " + e.getMessage());
                return;
            }
        } else {
            System.out.println("No file specified. Starting with empty buffer.");
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            editor.render();
            System.out.print("Input: ");
            String line = scanner.nextLine();

            if (line.isEmpty()) {
                continue;
            }
            char input = line.charAt(0);

            if (input == 27) { // Esc key ASCII code (just for demonstration)
                System.out.println("Exiting view mode loop.");
                break;
            }

            editor.handleInput(input);
        }

        scanner.close();
        System.out.println("Exiting editor.");
    }
}
