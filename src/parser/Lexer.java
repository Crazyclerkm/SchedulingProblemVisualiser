package parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer implements TokenStream {
    private static final Pattern idPattern = Pattern.compile("^-?[a-zA-Z\200-\377_0-9.]+");
    private String dotData;

    public Lexer(String file) throws IOException {
        byte[] dir = Files.readAllBytes(Paths.get(file));

        //Assume UTF-8, because really why would it be anything else?
        this.dotData = new String(dir, StandardCharsets.UTF_8);
    }


    @Override
    public Token nextToken() {
        int i;

        for (i = 0; i < this.dotData.length(); i++) {
            char c = this.dotData.charAt(i);

            if (c != ' ' && c != '\n' && c != '\r' && c != '\t') {
                break;
            }
        }

        this.dotData = this.dotData.substring(i);

        //Check for EOF
        if (this.dotData.isEmpty()) {
            return new Token(TokenType.EOF, "");
        }

        //Try to match ID
        Matcher matcher = idPattern.matcher(this.dotData);

        if (matcher.find()) {
            String data = matcher.group();
            this.dotData = this.dotData.substring(matcher.end());
            return new Token(TokenType.ID, data);
        }

        //Arrow
        if (this.dotData.startsWith("->")) {
            this.dotData = this.dotData.substring(2);
            return new Token(TokenType.RIGHT_ARROW, "");
        }

        //All the other tokens are one character
        char c = this.dotData.charAt(0);

        if (c == '=') {
            this.dotData = this.dotData.substring(1);
            return new Token(TokenType.EQUALS, "");
        }

        if (c == '{') {
            this.dotData = this.dotData.substring(1);
            return new Token(TokenType.OPEN_CURLY, "");
        }

        if (c == '}') {
            this.dotData = this.dotData.substring(1);
            return new Token(TokenType.CLOSE_CURLY, "");
        }

        if (c == '[') {
            this.dotData = this.dotData.substring(1);
            return new Token(TokenType.OPEN_SQUARE, "");
        }

        if (c == ']') {
            this.dotData = this.dotData.substring(1);
            return new Token(TokenType.CLOSE_SQUARE, "");
        }

        if (c == ';') {
            this.dotData = this.dotData.substring(1);
            return new Token(TokenType.SEMICOLON, "");
        }

        if (c == ',') {
            this.dotData = this.dotData.substring(1);
            return new Token(TokenType.COMMA, "");
        }

        //Check for quoted ID
        if (c == '"') {
            boolean inEscape = false;
            boolean quoted = false;
            int j;

            for (j = 1; j < this.dotData.length(); j++) {
                if (inEscape) {
                    inEscape = false;
                } else {
                    if (this.dotData.charAt(j) == '\\') {
                        inEscape = true;
                    } else if (this.dotData.charAt(j) == '"') {
                        quoted = true;
                        break;
                    }
                }
            }

            if (quoted) {
                String id = this.dotData.substring(0, j + 1);
                this.dotData = this.dotData.substring(j + 1);
                return new Token(TokenType.ID, id);
            }
        }

        //Illegal character
        return new Token(TokenType.ERROR, "");
    }
}
