public class Token {
    final TokenType type; // The type of the token (e.g., identifier, number, etc.)
    final String lexeme; // The actual text of the token as it appears in the source code
    final Object literal; // The value of the token (e.g., a number or string literal)
    final int line; // The line number in the source code where the token was found

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
