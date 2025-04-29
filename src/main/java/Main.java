import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class Main {
    static Set <String> relationalOperators = Set.of("<", "=", ">", "!");
    static Set <String> spaceOperators = Set.of("\t", " ");
    static HashMap <String, String> dictionary = new HashMap <> ();

    static void sieve() {
        dictionary.put("(", "LEFT_PAREN ( null");
        dictionary.put(")", "RIGHT_PAREN ) null");
        dictionary.put("{", "LEFT_BRACE { null");
        dictionary.put("}", "RIGHT_BRACE } null");
        dictionary.put("*", "STAR * null");
        dictionary.put(".", "DOT . null");
        dictionary.put(",", "COMMA , null");
        dictionary.put("+", "PLUS + null");
        dictionary.put("-", "MINUS - null");
        dictionary.put(";", "SEMICOLON ; null");
        dictionary.put("=", "EQUAL = null");
        dictionary.put("==", "EQUAL_EQUAL == null");
        dictionary.put("!", "BANG ! null");
        dictionary.put("!=", "BANG_EQUAL != null");
        dictionary.put(">", "GREATER > null");
        dictionary.put("<", "LESS < null");
        dictionary.put("<=", "LESS_EQUAL <= null");
        dictionary.put(">=", "GREATER_EQUAL >= null");
        dictionary.put("/", "SLASH / null");
    }

    static void Print(String msg) {
        System.out.println(msg);
    }

    static List <String> parseInput(String fileContents) {
        List <String> input = new ArrayList <>();
        for (char c : fileContents.toCharArray()) {
            if (c == '=') {
                if (input.size() == 0) {
                    input.add(c + "");
                } else {
                    if (relationalOperators.contains(input.get(input.size() - 1))) {
                        input.set(input.size() - 1, input.get(input.size() - 1) + "=");
                    } else {
                        input.add(c + "");
                    }
                }
            } else if (c == '/') {
                if (input.size() == 0 || !input.get(input.size() - 1).equals("/")) {
                    input.add(c + "");
                } else {
                    input.remove(input.size() - 1);
                    break;
                }
            } else {
                input.add(c + "");
            } 
        }
        return input;
    }

    static int readLine(String fileContents, int nline) {
        List <String> input = parseInput(fileContents);

        int errors = 0;
        int isString = 0;
        String currentString = "";

        for (String x : input) {
            if (x.equals("\"")) {
                isString ^= 1;

                if (isString == 0) {
                    Print("STRING \"" + currentString + "\" " + currentString);
                    currentString = "";
                }
                continue;
            }

            if (isString == 1) {
                currentString = currentString + x;
                continue;
            }

            if (spaceOperators.contains(x)) continue;

            if (dictionary.containsKey(x)) {
                Print(dictionary.get(x));
            } else {
                System.err.println("[line " + nline + "] Error: Unexpected character: " + x);
                errors = 65;
            }
            
        }

        if (isString == 1) {
            System.err.println("[line " + nline + "] Error: Unterminated string.");
            errors = 65;
        }
        return errors;
    }

    public static void main(String[] args) {
        sieve();
        args = new String[] {"tokenize", "test.lox"};

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

        // --------------------------- START OF INTERPRETER --------------------------

        String [] lines = fileContents.split("\\R");
        int errors = 0;
        
        for (int i = 0; i < lines.length; i++) {
            errors = readLine(lines[i], i + 1);
        }

        System.out.println("EOF  null");
        System.exit(errors);
    }
}
