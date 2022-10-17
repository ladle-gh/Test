package src.symbol;

import src.script.Script;

import java.io.IOException;

@FunctionalInterface
public interface Lexer {
    int accept(Script input) throws IOException;
}
