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

    static class Literal implements Expr {
        private final Object value;
        Literal(Object value) { this.value = value; }
        public double evaluate() {
            if (value instanceof Boolean) {
                return (Boolean) value ? 1 : 0;
            }
            return ((Number) value).doubleValue();
        }
        public String toString() { return value.toString(); }
    }

    static class Unary implements Expr {
        private final String operator;
        private final Expr right;
        Unary(String operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }
        public double evaluate() {
            double r = right.evaluate();
            return switch (operator) {
                case "!" -> (r == 0) ? 1 : 0;
                case "-" -> -r;
                default -> throw new RuntimeException("Unknown unary op: " + operator);
            };
        }
        public String toString() {
            return "(" + operator + " " + right + ")";
        }
    }

    static class Binary implements Expr {
        private final Expr left;
        private final String operator;
        private final Expr right;

        Binary(Expr left, String operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public double evaluate() {
            double l = left.evaluate();
            double r = right.evaluate();
            return switch (operator) {
                case "+" -> l + r;
                case "-" -> l - r;
                case "*" -> l * r;
                case "/" -> l / r;
                default -> throw new RuntimeException("Unknown binary op: " + operator);
            };
        }

        public String toString() {
            return "(" + operator + " " + left + " " + right + ")";
        }
    }

    static class Grouping implements Expr {
        private final Expr expression;
        Grouping(Expr expression) { this.expression = expression; }
        public double evaluate() { return expression.evaluate(); }
        public String toString() { return "(group " + expression + ")"; }
    }

    static class Parser {
        private final String input;
        private int pos = 0;
        Parser(String input) {
            this.input = input.replaceAll("\\s+", "");
        }
        private char peek() { return pos < input.length() ? input.charAt(pos) : '\0'; }
        private char advance() { return input.charAt(pos++); }
        private boolean match(char c) {
            if (peek() == c) { pos++; return true; }
            return false;
        }
        Expr parse() { return expression(); }
        private Expr expression() {
            Expr expr = term();
            while (peek() == '+' || peek() == '-') {
                char op = advance();
                Expr right = term();
                expr = new Unary(op == '-' ? "-" : "+", new Binary(expr, String.valueOf(op), right));
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
            if (match('!')) {
                return new Unary("!", factor());
            }

            if (match('-')) {
                return new Unary("-", factor());
            }

            if (match('(')) {
                Expr expr = expression();
                if (!match(')')) throw new RuntimeException("Expected ')' after expression");
                return new Grouping(expr);
            }
            return literal();
        }
        private Expr literal() {
            if (Character.isDigit(peek())) {
                int start = pos;
                while (Character.isDigit(peek()) || peek() == '.') advance();
                double v = Double.parseDouble(input.substring(start, pos));
                return new Literal(v);
            }
            if (Character.isLetter(peek())) {
                int start = pos;
                while (Character.isLetter(peek())) advance();
                String word = input.substring(start, pos);
                if ("true".equals(word) || "false".equals(word)) {
                    return new Literal(Boolean.parseBoolean(word));
                }
                throw new RuntimeException("Unknown literal: " + word);
            }
            throw new RuntimeException("Unexpected character: " + peek());
        }
    }


    static void parseLine(String fileContents) {
        Parser parser = new Parser(fileContents);
        Expr expr = parser.parse();
        System.out.println(expr);
    }

    public static void main(String[] args) {
        sieve();

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
