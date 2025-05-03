import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {
    static int errors = 0;

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

    static class Interpreter implements Expr.Visitor<Object> {
        static void runtimeError(RuntimeError error) {
            System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
            System.exit(70);
        }

        void interpret(Expr expression) { 
            try {
                Object value = evaluate(expression);
                System.out.println(stringify(value));
            } catch (RuntimeError error) {
                runtimeError(error);
            }
        }

        private String stringify(Object object) {
            if (object == null) return "nil";


            if (object instanceof Double) {
                String text = object.toString();
                if (text.endsWith(".0")) {
                    text = text.substring(0, text.length() - 2);
                }
                return text;
            }

            return object.toString();
        }


        private Object evaluate(Expr expr) {
            return expr.accept(this);
        }

        public Object visitLiteralExpr(Expr.Literal expr) {
            return expr.value;
        }

        public Object visitGroupingExpr(Expr.Grouping expr) {
            return evaluate(expr.expression);
        }

        class RuntimeError extends RuntimeException {
            final Token token;

            RuntimeError(Token token, String message) {
                super(message);
                this.token = token;
            }
        }

        private void checkNumberOperand(Token operator, Object operand) {
            if (operand instanceof Double) return;
            throw new RuntimeError(operator, "Operand must be a number.");
        }

        public Object visitUnaryExpr(Expr.Unary expr) {
            Object right = evaluate(expr.right);

            switch (expr.operator.type) {
                case MINUS:
                    checkNumberOperand(expr.operator, right);
                    return -(double)right;

                case BANG:
                    return !isTruthy(right);
                default:
                    break;
            }

            // Unreachable.
            return null;
        }

        private boolean isTruthy(Object object) {
            if (object == null) return false;
            if (object instanceof Boolean) return (boolean)object;
            return true;
        }

        private void checkNumberOperands(Token operator,
                                   Object left, Object right) {
            if (left instanceof Double && right instanceof Double) return;
    
            throw new RuntimeError(operator, "Operands must be numbers.");
        }

        public Object visitBinaryExpr(Expr.Binary expr) {
            Object left = evaluate(expr.left);
            Object right = evaluate(expr.right); 

            switch (expr.operator.type) {
                case MINUS:
                    checkNumberOperands(expr.operator, left, right);
                    return (double)left - (double)right;
                case SLASH:
                    checkNumberOperands(expr.operator, left, right);
                    return (double)left / (double)right;
                case STAR:
                    checkNumberOperands(expr.operator, left, right);
                    return (double)left * (double)right;
                case PLUS:
                    if (left instanceof Double && right instanceof Double) {
                        return (double) left + (double) right;
                    } 

                    if (left instanceof String && right instanceof String) {
                        return (String) left + (String) right;
                    }


                    throw new RuntimeError(expr.operator,
                    "Operands must be two numbers or two strings.");
                case GREATER:
                    checkNumberOperands(expr.operator, left, right);
                    return (double)left > (double)right;
                case GREATER_EQUAL:
                    checkNumberOperands(expr.operator, left, right);
                    return (double)left >= (double)right;
                case LESS:
                    checkNumberOperands(expr.operator, left, right);
                    return (double)left < (double)right;
                case LESS_EQUAL:
                    checkNumberOperands(expr.operator, left, right);
                    return (double)left <= (double)right;
                case BANG_EQUAL: 
                    return !isEqual(left, right);
                case EQUAL_EQUAL: 
                    return isEqual(left, right);
                default:
                    break;
            }

            // Unreachable.
            return null;
        }

        private boolean isEqual(Object a, Object b) {
            if (a == null && b == null) return true;
            if (a == null) return false;

            return a.equals(b);
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

            /*if (keywords.containsKey(peek().type.toString().toLowerCase())) {
                return new Expr.Literal(advance());
            }*/

            error(peek(), "Expect expression.");
            return new Expr.Literal(null);
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
            error(peek(), message);
            return advance();
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

        private static void error(Token token, String message) {
            System.err.println("[line " + token.line + "] Error at " +
                (token.type == TokenType.EOF ? "end" : "'" + token.lexeme + "'") +
                ": " + message);
            System.exit(65);
        }
    }

    static void parseLine(String source) {
        Scanner scanner = new Scanner(source);
        List <Token> tokens = scanner.scanTokens();

        Parser pa = new Parser(tokens);
        AstPrinter printer = new AstPrinter();
        System.out.println(printer.print(pa.expression()));
    }

    static void scanLine(String source) {
        Scanner scanner = new Scanner(source);
        List <Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    static void evaluateLine(String source) {
        Scanner scanner = new Scanner(source);
        List <Token> tokens = scanner.scanTokens();

        Parser pa = new Parser(tokens);
        Interpreter interpreter = new Interpreter();
        interpreter.interpret(pa.expression());
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
                System.exit(errors);
            case "parse":
                parseLine(fileContents);
                System.exit(errors);
            case "evaluate":
                evaluateLine(fileContents);
                System.exit(errors);
            default:
                break;
        }
    }
}
