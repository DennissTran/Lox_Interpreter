import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    static void parseLine(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser pa = new Parser(tokens);
        AstPrinter printer = new AstPrinter();
        System.out.println(printer.print(pa.expression()));
    }

    static void scanLine(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    static void evaluateLine(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser pa = new Parser(tokens);
        Interpreter interpreter = new Interpreter();
        System.out.println(stringify(interpreter.evaluate(pa.expression())));
    }

    private static String stringify(Object evaluate) {
        if (evaluate == null) return "nil";
        if (evaluate instanceof Double) {
            String text = evaluate.toString();
            if (text.endsWith(".0")) {
                return text.substring(0, text.length() - 2);
            }
            return text;
        }
        return evaluate.toString();
    }

    static void runLine(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser pa = new Parser(tokens);
        Interpreter interpreter = new Interpreter();
        interpreter.interpret(pa.parse());
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: ./your_program.sh tokenize <filename>");
            System.exit(1);
        }

        String command = args[0];
        String filename = args[1];

        String fileContents = "";
        try {
            fileContents = Files.readString(Path.of(filename));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }

        // --------------------------- START OF INTERPRETER --------------------------

        switch (command) {
            case "tokenize":
                scanLine(fileContents);
            case "parse":
                parseLine(fileContents);
            case "evaluate":
                evaluateLine(fileContents);
            case "run":
                runLine(fileContents);
            default:
                break;
        }
    }
}
