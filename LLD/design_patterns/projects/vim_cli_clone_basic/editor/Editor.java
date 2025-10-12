package projects.vim_cli_clone_basic.editor;

import projects.vim_cli_clone_basic.commands.Command;
import projects.vim_cli_clone_basic.commands.CommandFactory;

import java.util.Scanner;

public class Editor {
    private Mode mode = Mode.COMMAND;
    private final Buffer buffer = new Buffer();
    private final Cursor cursor = new Cursor();

    public Mode getMode() {
        return mode;
    }

    public void toggleMode() {
        mode = (mode == Mode.COMMAND) ? Mode.INSERT : Mode.COMMAND;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void run(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("-- VIM CLI Started (Press :q to quit) --");

        while (true){
            printStatusLine();
            String input = scanner.nextLine();

            if(mode == Mode.INSERT){
                if(input.equals("::esc")){
                    toggleMode();
                } else{
                    StringBuilder line = buffer.getLine(cursor.getLine());
                    line.insert(cursor.getColumn(), input);
                    cursor.moveTo(cursor.getLine(), cursor.getColumn() + input.length());
                }
            } else{
                if (input.equals(":q")) break;
                Command command = CommandFactory.createCommand(input);
                if (command != null) command.execute(this);
                else System.out.println("Invalid command.");
            }
        }

    }

    private void printStatusLine() {
        System.out.print("[" + mode + "]> ");
    }
}
