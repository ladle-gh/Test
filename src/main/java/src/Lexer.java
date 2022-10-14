package src;

import java.io.IOException;

@FunctionalInterface
public interface Lexer {
    int parse(LexerParameter input) throws IOException;
}
