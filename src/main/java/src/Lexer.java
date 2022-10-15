package src;

import java.io.IOException;

@FunctionalInterface
public interface Lexer {
    int accept(Script input) throws IOException;
}
