package src;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;

public final class ReaderBuffer {
    public final boolean containsEOF;
    public final int length;

    public ReaderBuffer(@NotNull Reader istream, int size) throws IOException {
        buffer = new char[size];
        length = istream.read(buffer, 0, size);
        containsEOF = length != size;
        last = null;
    }

    public ReaderBuffer(@NotNull Reader istream, @NotNull ReaderBuffer last, int size) throws IOException {
        buffer = new char[size];
        length = istream.read(buffer, 0, size);
        containsEOF = length != size;
        this.last = last;
        last.next = this;
    }

    public char get(int at) {
        return buffer[at];
    }
    public ReaderBuffer last() {
        return last;
    }
    public ReaderBuffer next() {
        return next;
    }

    private final char[] buffer;
    private final ReaderBuffer last;
    private ReaderBuffer next;
}
