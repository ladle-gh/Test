package src;

import java.io.IOException;

public final class StringParameter implements LexerParameter {
    public StringParameter(Token ignore, String input) throws IOException {
        this.input = input;
        advance(ignore.lexer().parse(this));
    }

    @Override
    public void advance(int by) {
        next += by;
    }
    @Override
    public void mark() {
        markedIndices.push(next);
    }
    @Override
    public int peek(int offset) {
        final int target = next + offset;
        if (target < input.length())
            return input.charAt(target);
        return EOF;
    }
    @Override
    public int read() {
        if (next < input.length())
            return input.charAt(next++);
        return EOF;
    }
    @Override
    public void revert() {
        next = markedIndices.pop();
    }

    private int next = 0;

    private final IntStack markedIndices = new IntStack();
    private final String input;
}
