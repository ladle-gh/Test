package src;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;

public final class ReaderParameter implements LexerParameter {
    public ReaderParameter(@NotNull Token ignore, Reader istream) throws IOException {
        this.istream = istream;
        buffer = new ReaderBuffer(istream, increment = DEFAULT_CAPACITY);
        advance(ignore.lexer().parse(this));
    }
    public ReaderParameter(@NotNull Token ignore, Reader istream, int bufferSize) throws IOException {
        this.istream = istream;
        buffer = new ReaderBuffer(istream, increment = bufferSize);
        advance(ignore.lexer().parse(this));
    }

    @Override
    public void advance(int by) {
        next += by;
    }

    // Contract: offset > 0
    @Override
    public int peek(int offset) throws IOException {
        /* Search for location in current buffer */
        int target = next + offset;
        if (target < buffer.length)         // Current buffer contains location
            return buffer.get(target);
        target -= buffer.length;            // target = distance to next
        if (buffer.containsEOF)             // All data has been read from stream
            return EOF;

        /* Look ahead in next buffer(s) */
        int relative = target;
        for (ReaderBuffer next = buffer.next(); next != null; next = next.next()) {
            if (relative < next.length)     // Linked buffer contains location
                return next.get(relative);
            relative -= next.length;
        }

        /* Read data from stream into next buffer */
        buffer = new ReaderBuffer(istream, buffer, Math.max(increment, target + 1));
        if (target < buffer.length)         // Next buffer contains location
            return buffer.get(target);
        return EOF;                         // Location exceeds EOF
    }


    @Override
    public void mark() {
        markedBuffers.push(buffer);
        markedIndices.push(next);
    }

    @Override
    public int read() throws IOException {
        /* Search for location in current buffer */
        if (next < buffer.length)       // Current buffer contains location
            return buffer.get(next++);
        if (buffer.containsEOF)         // All data has been read from stream
            return EOF;

        /* Read data from stream into next buffer */
        buffer = new ReaderBuffer(istream, buffer, increment);
        return buffer.get(next = 0);
    }

    public void save
    @Override
    public void revert() {
        buffer = markedBuffers.pop();
        next = markedIndices.pop();
    }
    
    private static final int DEFAULT_CAPACITY = 8192;

    private final Deque<ReaderBuffer> markedBuffers = new ArrayDeque<>();
    private final IntStack markedIndices = new IntStack();
    private final int increment;
    private ReaderBuffer buffer;
    private int next = 0;
    private final Reader istream;
}
