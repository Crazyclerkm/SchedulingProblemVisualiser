package parser;

public class Token {
    public final TokenType type;
    public final String data;

    public Token(TokenType type, String data) {
        this.type = type;
        this.data = data;
    }
}
