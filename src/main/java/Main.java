import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.*;

public class Main {
    static Set <String> relationalOperators = Set.of("<", "=", ">", "!");
    static Set <String> spaceOperators = Set.of("\t", " ");
    static HashMap <String, String> dictionary = new HashMap <> ();
    static Set <String> digits = Set.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    static HashMap<String, String> keywords;
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


        keywords = new HashMap<>();
        keywords.put("and",    "AND");
        keywords.put("class",  "CLASS");
        keywords.put("else",   "ELSE");
        keywords.put("false",  "FALSE");
        keywords.put("for",    "FOR");
        keywords.put("fun",    "FUN");
        keywords.put("if",     "IF");
        keywords.put("nil",    "NIL");
        keywords.put("or",     "OR");
        keywords.put("print",  "PRINT");
        keywords.put("return", "RETURN");
        keywords.put("super",  "SUPER");
        keywords.put("this",   "THIS");
        keywords.put("true",   "TRUE");
        keywords.put("var",    "VAR");
        keywords.put("while",  "WHILE");
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
                    input.set(input.size() - 1, input.get(input.size() - 1) + "/");
                }
            } else {
                input.add(c + "");
            } 
        }
        return input;
    }

    static boolean isIdetifier(char c) {
        if (c == '_') return true;
        if (c >= 'a' && c <= 'z') return true;
        if (c >= 'A' && c <= 'Z') return true;
        return false;
    }

    static String printNumber(String currentNumber) {
        if (currentNumber.length() > 0) {
            Print("NUMBER " + currentNumber + " " + Double.parseDouble(currentNumber));
            currentNumber = "";
        }
        return currentNumber;
    }

    static String printIdentifier(String identifier) {
        if (identifier.length() > 0) {
            if (keywords.containsKey(identifier)) {
                Print(keywords.get(identifier) + " " + identifier + " null");
            } 

            else {
                Print("IDENTIFIER " + identifier + " null");
            }

            identifier = "";
        }
        return identifier;
    }

    static int readLine(String fileContents, int nline) {
        List <String> input = parseInput(fileContents);
        input.add(" ");

        int errors = 0;
        int isString = 0;
        String currentString = "";
        String currentNumber = "";
        String identifier = "";

        int id = -1;
        for (String x : input) {
            ++id;
            if (x.equals("\"")) {
                identifier = printIdentifier(identifier);
                currentNumber = printNumber(currentNumber);

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

            if (spaceOperators.contains(x)) {
                identifier = printIdentifier(identifier);
                currentNumber = printNumber(currentNumber);
                continue;
            }

            if (currentNumber.length() > 0 && x.equals(".") && id < input.size() - 1 && digits.contains(input.get(id + 1))) {
                currentNumber = currentNumber + x;
                continue;
            }

            if (dictionary.containsKey(x)) {
                identifier = printIdentifier(identifier);
                currentNumber = printNumber(currentNumber);

                Print(dictionary.get(x));
                continue;
            } 

            if (identifier.length() > 0) {
                identifier = identifier + x;
                continue;
            }

            if (digits.contains(x)) {
                currentNumber = currentNumber + x;
                continue;
            }

            if (x.equals("//")) break;
            
            currentNumber = printNumber(currentNumber);

            if (isIdetifier(x.charAt(0))) {
                identifier = identifier + x;
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

    interface Expr {
        double evaluate();
    }

    class Literal implements Expr {
        double value;
        Literal(double value) { this.value = value; }
        public double evaluate() { return value; }
    }

    class Binary implements Expr {
        Expr left;
        String op;
        Expr right;

        Binary(Expr l, String op, Expr r) {
            left = l; this.op = op; right = r;
        }

        public double evaluate() {
            double lv = left.evaluate();
            double rv = right.evaluate();
            return switch (op) {
                case "+" -> lv + rv;
                case "-" -> lv - rv;
                case "*" -> lv * rv;
                case "/" -> lv / rv;
                default -> throw new RuntimeException("Unknown operator: " + op);
            };
        }
    }


    class Parser {
    private final String input;
        private int pos = 0;

        Parser(String input) {
            this.input = input.replaceAll("\\s+", ""); // bỏ khoảng trắng
        }

        private char peek() {
            return pos < input.length() ? input.charAt(pos) : '\0';
        }

        private char advance() {
            return input.charAt(pos++);
        }

        private boolean match(char expected) {
            if (peek() == expected) {
                pos++;
                return true;
            }
            return false;
        }

        public Expr parse() {
            return expression();
        }

        private Expr expression() {
            Expr expr = term();
            while (peek() == '+' || peek() == '-') {
                char op = advance();
                Expr right = term();
                expr = new Binary(expr, String.valueOf(op), right);
            }
            return expr;
        }

        private Expr term() {
            Expr expr = factor();
            while (peek() == '*' || peek() == '/') {
                char op = advance();
                Expr right = factor();
                expr = new Binary(expr, String.valueOf(op), right);
            }
            return expr;
        }

        private Expr factor() {
            if (match('(')) {
                Expr expr = expression();
                if (!match(')')) throw new RuntimeException("Expected ')'");
                return expr;
            }

            return number();
        }

        private Expr number() {
            int start = pos;
            while (Character.isDigit(peek()) || peek() == '.') pos++;
            if (start == pos) throw new RuntimeException("Expected number");
            double val = Double.parseDouble(input.substring(start, pos));
            return new Literal(val);
        }
    }


    static void parseLine(String fileContents) {
        List<String> words = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"[^\"]*\"|\\S+");  
        Matcher matcher = pattern.matcher(fileContents);
        while (matcher.find()) {
            words.add(matcher.group());
        }

        if (keywords.containsKey(words.get(0))) {
            Print(words.get(0));
            return;
        }

        else if (words.get(0).charAt(0) == '\"') {
            words.set(0, words.get(0).substring(1, words.get(0).length() - 1));
            Print(words.get(0));
        } else if (words.get(0).charAt(0) == '(') {
            Print("(group " + words.get(0).substring(2, words.get(0).length() - 2) + ")");
        } else {
            Print("" + Double.parseDouble(words.get(0)));
        }
        //else Print("(" + words[1] + " " + Double.parseDouble(words[0]) + " " + Double.parseDouble(words[2]) + ")");
    }

    public static void main(String[] args) {
        sieve();
        //args = new String[] {"tokenize", "test.lox"};

        if (args.length < 2) {
            System.err.println("Usage: ./your_program.sh tokenize <filename>");
            System.exit(1);
        }

        String command = args[0];
        String filename = args[1];

        /*if (!command.equals("tokenize")) {
            System.err.println("Unknown command: " + command);
            System.exit(1);
        }*/

        String fileContents = "";
        try {
            fileContents = Files.readString(Path.of(filename));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }

        // --------------------------- START OF INTERPRETER --------------------------

        String [] lines = fileContents.split("\\R");

        if (command.equals("tokenize")) {
            int errors = 0;
        
            for (int i = 0; i < lines.length; i++) {
                errors = readLine(lines[i], i + 1);
            }

            System.out.println("EOF  null");
            System.exit(errors);
        } else if (command.equals("parse")) {
            for (int i = 0; i < lines.length; i++) {
                parseLine(lines[i]);
            }
        }
        
    }
}
