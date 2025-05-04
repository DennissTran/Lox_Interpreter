import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static List<Token> tokens;
    private static int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) {
            return printStatement();
        }
        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
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
        if (match(TokenType.FALSE))
            return new Expr.Literal(false);
        if (match(TokenType.TRUE))
            return new Expr.Literal(true);
        if (match(TokenType.NIL))
            return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        // System.out.println(peek().type.toString().toLowerCase());

        /*
         * if (keywords.containsKey(peek().type.toString().toLowerCase())) {
         * return new Expr.Literal(advance());
         * }
         */

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
        if (check(type))
            return advance();
        error(peek(), message);
        return advance();
    }

    private static boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private static Token advance() {
        if (!isAtEnd())
            current++;
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
