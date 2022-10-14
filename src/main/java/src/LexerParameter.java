package src;

import java.io.IOException;

public interface LexerParameter {
    int EOF = -1;

    // Get char, but do not pop
    int peek(int offset) throws IOException;

    // Get char + pop
    int read() throws IOException;

    void mark();
    void revert();
    void advance(int by);
}
