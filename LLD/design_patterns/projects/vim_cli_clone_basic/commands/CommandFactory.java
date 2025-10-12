package projects.vim_cli_clone_basic.commands;

public class CommandFactory {
    public static Command createCommand(String input) {
        if (input.startsWith(":open ")) return new OpenCommand(input.substring(6).trim());
        if (input.startsWith(":save ")) return new SaveCommand(input.substring(6).trim());
        if (input.startsWith(":move ")) return new MoveCommand(input.substring(6).trim());
        if (input.equals(":delete")) return new DeleteCommand();
        if (input.equals(":insert")) return new InsertCommand();
        return null;
    }
}
