package src;

import src.symbol.AbstractSymbol;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;

import static nullity.Nullity.using;
import static nullity.Nullity.usingMembers;

public abstract class Script {
    public static final int EOF = -1;

    public static Script of(String input, AbstractSymbol ignore) {
        using(input);

        try {
            return new Script(ignore) {
                @Override
                public void mark() {
                    markedIndices.push(next);
                }

                @Override
                public int read(int offset) {
                    final int target = next + offset;
                    if (target < input.length())
                        return input.charAt(target);
                    return EOF;
                }

                @Override
                public void revert() {
                    next = markedIndices.pop();
                }
            };
        } catch (IOException e) {   // Impossible
            throw new UncheckedIOException(e);
        }
    }

    public static Script of(Reader input, AbstractSymbol ignore) throws IOException {
        return of(input, ignore, DEFAULT_BUFFER_SIZE);
    }

    public static Script of(Reader input, AbstractSymbol ignore, int bufferSize) throws IOException {
        using(input);

        return new Script(ignore) {
            // Contract: offset > 0
            @Override
            public int read(int offset) throws IOException {
                /* Search for location in current buffer */
                int target = next + offset;
                if (target < buffer.length)         // Current buffer contains location
                    return buffer.get(target);
                target -= buffer.length;            // target = distance to next
                if (buffer.containsEOF)             // All data has been read from stream
                    return EOF;

                /* Look ahead in next buffer(s) */
                int relative = target;
                for (Buffer next = buffer.next(); next != null; next = next.next()) {
                    if (relative < next.length)     // Linked buffer contains location
                        return next.get(relative);
                    relative -= next.length;
                }

                /* Read data from stream into next buffer */
                buffer = new Buffer(input, buffer, Math.max(bufferSize, target + 1));
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
            public void revert() {
                buffer = markedBuffers.pop();
                next = markedIndices.pop();
            }

            static final class Buffer {
                public final boolean containsEOF;
                public final int length;

                public Buffer(Reader istream, int size) throws IOException {
                    using(istream);

                    buffer = new char[size];
                    length = istream.read(buffer, 0, size);
                    containsEOF = length != size;
                    last = null;
                }

                public Buffer(Reader istream, Buffer last, int size) throws IOException {
                    using(istream, last);

                    buffer = new char[size];
                    length = istream.read(buffer, 0, size);
                    containsEOF = length != size;
                    this.last = last;
                    last.next = this;
                }

                public char get(int at) {
                    return buffer[at];
                }
                public Buffer last() {
                    return last;
                }
                public Buffer next() {
                    return next;
                }

                private final char[] buffer;
                private final Buffer last;
                private Buffer next;
            }

            private final Deque<Buffer> markedBuffers = new ArrayDeque<>();
            private Buffer buffer = new Buffer(input, bufferSize);
        };
    }

    public boolean recordIndices = true;

    public Script(AbstractSymbol ignore) throws IOException {
        advance(ignore.accept(this));
    }

    public final void advance(int by) {
        next += by;
    }

    public final void match(AbstractSymbol as, int children) {
        if (recordIndices) {
            if (children == 0)
                tokens.push(new Token(as, markedIndices.peek(), next));
            else {
                final Token[] childArray = new Token[children];
                for (int i = 0; i < children; ++i)
                    childArray[i] = tokens.pop();
                tokens.push(new Token(as, childArray));
            }
        }
        revert();
    }

    public final void matchLiteral(AbstractSymbol as, int length) {
        tokens.push(new Token(as, next, next + length));
    }

    // SyntaxTreeNode toSyntaxTree();



    public abstract void mark();

    public abstract int read(int offset) throws IOException;

    public abstract void revert();

    protected final IntStack markedIndices = new IntStack();
    protected int next = 0;

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private final Deque<Token> tokens =  new ArrayDeque<>();

    private static final class Token {
        public final int begin, end;

        // Contract: children in ascending order
        public Token(AbstractSymbol match, Token[] children) {
            using(match, children);
            usingMembers(children);

            begin = children[0].begin;
            end = children[children.length - 1].end;
            this.match = match;
            this.children = children;
        }
        public Token(AbstractSymbol match, int begin, int end) {
            using(match);

            this.begin = begin;
            this.end = end;
            this.match = match;
            this.children = null;
        }

        public Object parse() {
            return null;
        }

        private final AbstractSymbol match;
        private Token[] children;
    }
}
