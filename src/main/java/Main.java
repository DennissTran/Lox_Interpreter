import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static int EXIT_CODE = 0;

    void parseLine(String source) {
        try {
            Scanner scanner = new Scanner(source);
            List<Token> tokens = scanner.scanTokens();
            
            Parser pa = new Parser(tokens);
            AstPrinter printer = new AstPrinter();
            System.out.println(printer.print(pa.expression()));
        } catch (ParseError error) {
            System.err.println(error.getMessage());
            EXIT_CODE = 65;
        } catch (IndexOutOfBoundsException error) {
            EXIT_CODE = 65;
        } 
    }

    void scanLine(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    void evaluateLine(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        
        try {
            Parser pa = new Parser(tokens);
            Interpreter interpreter = new Interpreter();
            System.out.println(stringify(interpreter.evaluate(pa.expression())));
        } catch (RuntimeError error) {
            System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
            EXIT_CODE = 70;
        } catch (ParseError error) {
            System.err.println(error.getMessage());
            EXIT_CODE = 65;
        }
        
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

    void runLine(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        try{
            Parser pa = new Parser(tokens);
            Interpreter interpreter = new Interpreter();
        
            List <Stmt> statements = pa.parse();
            Resolver resolver = new Resolver(interpreter);
            resolver.resolve(statements);

            if (EXIT_CODE == 65) {
                return;
            }
    
            if (EXIT_CODE == 70) {
                return;
            }
            interpreter.interpret(statements);

        } catch (RuntimeError error) {
            System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
            EXIT_CODE = 70;
        } catch (ParseError error) {
            System.err.println(error.getMessage());
            EXIT_CODE = 65;
        }
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
        } 
        catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }

        // --------------------------- START OF INTERPRETER --------------------------
        Main obj = new Main();
        switch (command) {
            case "tokenize":
                obj.scanLine(fileContents);
                break;
            case "parse":
                obj.parseLine(fileContents);
                break;
            case "evaluate":
                obj.evaluateLine(fileContents);
                break;
            case "run":
                obj.runLine(fileContents);
                break;
            default:
                break;
        }
        //System.out.println("Exit code: " + EXIT_CODE);
        System.exit(EXIT_CODE);
    }
}
