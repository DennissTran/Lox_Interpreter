import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {
    static int errors = 0;
    static enum TokenType {
        // Single-character tokens.
        LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
        COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

        // One or two character tokens.
        BANG, BANG_EQUAL,
        EQUAL, EQUAL_EQUAL,
        GREATER, GREATER_EQUAL,
        LESS, LESS_EQUAL,

        // Literals.
        IDENTIFIER, STRING, NUMBER,

        // Keywords.
        AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
        PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

        // End of file
        EOF
    }
    private static final Map <String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    TokenType.AND);
        keywords.put("class",  TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        errors = 65;
    }

    static class Token {
        final TokenType type;
        final String lexeme;
        final Object literal;
        final int line; 

        Token(TokenType type, String lexeme, Object literal, int line) {
            this.type = type;
            this.lexeme = lexeme;
            this.literal = literal;
            this.line = line;
        }

        public String toString() {
            return type + " " + lexeme + " " + literal;
        }
    }

    static class Scanner {
        private final String source;
        private final List <Token> tokens = new ArrayList<>();
        private int start = 0;
        private int current = 0;
        private int line = 1;

        private char peek() {
            if (isAtEnd()) return '\0';
            return source.charAt(current);
        }

        private char advance() {
            return source.charAt(current++);
        }

        private boolean isAtEnd() {
            return current >= source.length();
        }

        private boolean match(char expected) {
            if (isAtEnd()) return false;
            if (source.charAt(current) != expected) return false;

            current++;
            return true;
        }

        Scanner(String source) {
            this.source = source;
        }


        private void addToken(TokenType type) {
            addToken(type, null);
        }

        private void addToken(TokenType type, Object literal) {
            String text = source.substring(start, current);
            tokens.add(new Token(type, text, literal, line));
        }

        private void string() {
            while (peek() != '"' && !isAtEnd()) {
                if (peek() == '\n') line++;
                advance();
            }

            if (isAtEnd()) {
                error(line, "Unterminated string.");
                return;
            }

            // The closing ".
            advance();

            // Trim the surrounding quotes.
            String value = source.substring(start + 1, current - 1);
            addToken(TokenType.STRING, value);
        }

        private char peekNext() {
            if (current + 1 >= source.length()) return '\0';
            return source.charAt(current + 1);
        } 

        private void number() {
            while (isDigit(peek())) advance();

            // Look for a fractional part.
            if (peek() == '.' && isDigit(peekNext())) {
                // Consume the "."
                advance();

                while (isDigit(peek())) advance();
            }

            addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
        }

        private void identifier() {
            while (isAlphaNumeric(peek())) advance();
            String text = source.substring(start, current);
            TokenType type = keywords.get(text);
            if (type == null) type = TokenType.IDENTIFIER;
            addToken(type);
        }

        private boolean isAlpha(char c) {
            return (c >= 'a' && c <= 'z') ||
                   (c >= 'A' && c <= 'Z') ||
                    c == '_';
        }

        private boolean isAlphaNumeric(char c) {
            return isAlpha(c) || isDigit(c);
        }

        private void scanToken() {
            char c = advance();
            switch (c) {
                case '(': addToken(TokenType.LEFT_PAREN); break;
                case ')': addToken(TokenType.RIGHT_PAREN); break;
                case '{': addToken(TokenType.LEFT_BRACE); break;
                case '}': addToken(TokenType.RIGHT_BRACE); break;
                case ',': addToken(TokenType.COMMA); break;
                case '.': addToken(TokenType.DOT); break;
                case '-': addToken(TokenType.MINUS); break;
                case '+': addToken(TokenType.PLUS); break;
                case ';': addToken(TokenType.SEMICOLON); break;
                case '*': addToken(TokenType.STAR); break;
                case '!':
                    addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                    break;
                case '=':
                    addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                    break;
                case '<':
                    addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                    break;
                case '>':
                    addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                    break;
                case '/':
                    if (match('/')) {
                        // A comment goes until the end of the line.
                        while (peek() != '\n' && !isAtEnd()) advance();
                    } else {
                        addToken(TokenType.SLASH);
                    }
                    break;

                case ' ':
                case '\r':
                case '\t':
                    // Ignore whitespace.
                    break;

                case '\n':
                    line++;
                    break;

                case '"': 
                    string();
                    break;

                case 'o':
                    if (match('r')) {
                        addToken(TokenType.OR);
                    }
                    break;

                default:
                    if (isDigit(c)) {
                        number();
                    } else if (isAlpha(c)) {
                        identifier();
                    } else {
                        error(line, "Unexpected character.");
                    }
                    break;
            }
        }

        private boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        } 

        List <Token> scanTokens() {
            while (!isAtEnd()) {
                // We are at the beginning of the next lexeme.
                start = current;
                scanToken();
            }

            tokens.add(new Token(TokenType.EOF, "", null, line));
            return tokens;
        }
    }

    static abstract class Expr{
        interface Visitor <R> {
            R visitBinaryExpr(Binary expr);
            R visitGroupingExpr(Grouping expr);
            R visitLiteralExpr(Literal expr);
            R visitUnaryExpr(Unary expr);
        }

        abstract <R> R accept (Visitor <R> visitor);

        static class Binary extends Expr {
            final Expr left;
            final Token operator;
            final Expr right;

            Binary(Expr left, Token operator, Expr right) {
                this.left = left;
                this.operator = operator;
                this.right = right;
            }

            @Override
            <R> R accept(Visitor <R> visitor) {
                return visitor.visitBinaryExpr(this);
            }
        }

        static class Grouping extends Expr {
            final Expr expression;

            Grouping(Expr expression) {
                this.expression = expression;
            }

            @Override
            <R> R accept(Visitor <R> visitor) {
                return visitor.visitGroupingExpr(this);
            }
        }

        static class Literal extends Expr {
            final Object value;

            Literal(Object value) {
                this.value = value;
            }

            @Override
            <R> R accept(Visitor <R> visitor) {
                return visitor.visitLiteralExpr(this);
            }
        }

        static class Unary extends Expr {
            final Token operator;
            final Expr right;

            Unary(Token operator, Expr right) {
                this.operator = operator;
                this.right = right;
            }

            @Override
            <R> R accept(Visitor <R> visitor) {
                return visitor.visitUnaryExpr(this);
            }
        }
    }

    static class AstPrinter implements Expr.Visitor<String> {
        String print(Expr expr) {
            return expr.accept(this);
        }

        public String visitBinaryExpr(Expr.Binary expr) {
            return parenthesize(expr.operator.lexeme, expr.left, expr.right);
        }

        public String visitGroupingExpr(Expr.Grouping expr) {
            return parenthesize("group", expr.expression);
        }

        public String visitLiteralExpr(Expr.Literal expr) {
            if (expr.value == null) return "nil";
            return expr.value.toString();
        }

        public String visitUnaryExpr(Expr.Unary expr) {
            return parenthesize(expr.operator.lexeme, expr.right);
        }

        private String parenthesize(String name, Expr... exprs) {
            StringBuilder builder = new StringBuilder();
            builder.append("(").append(name);
            for (Expr expr : exprs) {
                builder.append(" ");
                builder.append(expr.accept(this));
            }
            builder.append(")");
            return builder.toString();
        }
    }

    static class Parser{
        private static List <Token> tokens;
        private static int current = 0;

        Parser(List <Token> tokens) {
            this.tokens = tokens;
        }

        static Expr expression() {
            return equality();
        }

        private static Expr equality() {
            Expr expr = comparison();

            while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
                Token operator = previous();
                Expr right = comparison();
                expr = new Expr.Binary(expr, operator, right);
            }

            return expr;
        }

        private static Expr comparison() {
            Expr expr = term();

            while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, 
                TokenType.LESS, TokenType.LESS_EQUAL)) {
                Token operator = previous();
                Expr right = term();
                expr = new Expr.Binary(expr, operator, right);
            }

            return expr;
        }

        private static Expr term() {
            Expr expr = factor();

            while (match(TokenType.PLUS, TokenType.MINUS)) {
                Token operator = previous();
                Expr right = factor();
                expr = new Expr.Binary(expr, operator, right);
            }

            return expr;
        }

        private static Expr factor() {
            Expr expr = unary();

            while (match(TokenType.STAR, TokenType.SLASH)) {
                Token operator = previous();
                Expr right = unary();
                expr = new Expr.Binary(expr, operator, right);
            }

            return expr;
        }

        private static Expr unary() {
            if (match(TokenType.BANG, TokenType.MINUS)) {
                Token operator = previous();
                Expr right = unary();
                return new Expr.Unary(operator, right);
            }

            return primary();
        }

        private static Expr primary() {
            if (match(TokenType.FALSE)) return new Expr.Literal(false);
            if (match(TokenType.TRUE)) return new Expr.Literal(true);
            if (match(TokenType.NIL)) return new Expr.Literal(null);

            if (match(TokenType.NUMBER, TokenType.STRING)) {
                return new Expr.Literal(previous().literal);
            }

            if (match(TokenType.LEFT_PAREN)) {
                Expr expr = expression();
                consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
                return new Expr.Grouping(expr);
            }

            //System.out.println(peek().type.toString().toLowerCase());

            if (keywords.containsKey(peek().type.toString().toLowerCase())) {
                return new Expr.Literal(advance());
            }

            throw error(peek(), "Expect expression.");
        }

        private static boolean match(TokenType... types) {
            for (TokenType type : types) {
                if (check(type)) {
                    advance();
                    return true;
                }
            }
            return false;
        }

        private static Token consume(TokenType type, String message) {
            if (check(type)) return advance();
            throw error(peek(), message);
        }

        private static boolean check(TokenType type) {
            if (isAtEnd()) return false;
            return peek().type == type;
        }

        private static Token advance() {
            if (!isAtEnd()) current++;
            return previous();
        }

        private static boolean isAtEnd() {
            return peek().type == TokenType.EOF;
        }

        private static Token peek() {
            return tokens.get(current);
        }

        private static Token previous() {
            return tokens.get(current - 1);
        }

        private static ParseError error(Token token, String message) {
            System.err.println("[line " + token.line + "] Error at " +
                (token.type == TokenType.EOF ? "end" : "'" + token.lexeme + "'") +
                ": " + message);
            return new ParseError();
        }

        private static class ParseError extends RuntimeException {}
    }

    static void parseLine(String source) {
        Scanner scanner = new Scanner(source);
        List <Token> tokens = scanner.scanTokens();

        //For tokenize
        /*for (Token token : tokens) {
            System.out.println(token);
        }*/

        //For parse
        Parser pa = new Parser(tokens);
        AstPrinter printer = new AstPrinter();
        System.out.println(printer.print(pa.expression()));
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

        String [] lines = fileContents.split("\\R");

        if (command.equals("tokenize")) {
            /*int errors = 0;
            for (int i = 0; i < lines.length; i++) {
                errors = readLine(lines[i], i + 1);
            }

            System.out.println("EOF  null");
            System.exit(errors);*/
        } else if (command.equals("parse")) {
            parseLine(fileContents);
            System.exit(errors);
        }
    }
}
