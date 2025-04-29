import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void Print(String msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
        //args = new String[] {"tokenize", "Contents.txt"};

        if (args.length < 2) {
            System.err.println("Usage: ./your_program.sh tokenize <filename>");
            System.exit(1);
        }

        String command = args[0];
        String filename = args[1];

        if (!command.equals("tokenize")) {
            System.err.println("Unknown command: " + command);
            System.exit(1);
        }

        String fileContents = "";
        try {
            fileContents = Files.readString(Path.of(filename));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }

        int errors = 0;

        if (fileContents.length() > 0) {
            for (char c : fileContents.toCharArray()) {
                if (c == '(') Print("LEFT_PAREN ( null");
                else if (c == ')') Print("RIGHT_PAREN ) null");
                else if (c == '{') Print("LEFT_BRACE { null");
                else if (c == '}') Print("RIGHT_BRACE } null");
                else if (c == '*') Print("STAR * null");
                else if (c == '.') Print("DOT . null");
                else if (c == ',') Print("COMMA , null");
                else if (c == '+') Print("PLUS + null");
                else if (c == '-') Print("MINUS - null");
                else if (c == ';') Print("SEMICOLON ; null");
                else {
                    System.err.println("[line 1] Error: Unexpected character: " + c);
                    errors = 65;
                }
            }

            System.out.println("EOF  null");
        } else {
            System.out.println("EOF  null"); // Placeholder, remove this line when implementing the scanner
        }

        System.exit(errors);
    }
}
