package src.script;

import src.symbol.AbstractSymbol;
import src.symbol.Symbol;
import src.util.IntStack;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.*;

import static java.util.Collections.binarySearch;

public abstract class Script {
    public static final int EOF = -1;

    public static Script of(String input, AbstractSymbol ignore) {
        try {
            return new Script(ignore) {
                @Override
                public void advance(int by) {
                    next += by;
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

                @Override
                String substring(int begin, int end) {
                    return input.substring(begin, end);
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
        return new Script(ignore) {
            @Override
            public void advance(int by) {
                next += by;
            }

            // Contract: offset > 0
            @Override
            public int read(int offset) throws IOException {
                /* Search for location in current buffer */
                var buffer = buffers.get(current);
                int target = next + offset;
                if (target < buffer.length)         // Current buffer contains location
                    return buffer.chars[target];
                if (buffer.containsEOF)             // All data has been read from stream
                    return EOF;

                /* Look ahead in next buffer(s) */
                target -= buffer.length;            // target = distance to next
                int relative = target;
                for (int i = current; i < buffers.size(); ++i) {
                    buffer = buffers.get(i);
                    if (relative < buffer.length)   // Linked buffer contains location
                        return buffer.chars[relative];
                    relative -= buffer.length;
                }

                /* Read data from stream into next buffer */
                buffer = new Buffer(input, next + offset, Math.max(bufferSize, target + 1));
                buffers.add(buffer);
                if (target < buffer.length)         // Next buffer contains location
                    return buffer.chars[target];
                return EOF;                         // Location exceeds EOF
            }

            @Override
            public void revert() {
                next = markedIndices.pop();
                current = markedBuffers.pop();
            }

            static final class Buffer implements Comparable<Integer> {
                public final char[] chars;
                public final boolean containsEOF;
                public final int start, length;

                public Buffer(Reader istream, int start, int size) throws IOException {
                    chars = new char[size];
                    length = istream.read(chars, 0, size);
                    containsEOF = length != size;
                    this.start = start;
                }

                @Override
                public int compareTo(Integer index) {
                    int ubIndex = index;
                    if (ubIndex < start)
                        return -1;
                    if (ubIndex >= start + length)
                        return 1;
                    return 0;
                }
            }

            @Override
            String substring(int begin, int end) {
                final int first = binarySearch(buffers, begin), last = binarySearch(buffers, end);
                Buffer buffer1 = buffers.get(first), buffer2 = buffers.get(last);
                begin -= buffer1.start;
                end -= buffer2.start;
                if (first == last) {
                    final var buffer = buffers.get(first);
                    return String.valueOf(buffer.chars, begin, end - begin);
                }
                final var builder = new StringBuilder(end - begin);
                builder.append(buffer1.chars, begin, buffer1.chars.length - begin);
                for (int i = first + 1; i < end; ++i)
                    builder.append(buffers.get(i).chars);
                builder.append(buffer2.chars, end, buffer2.chars.length - end);
                return builder.toString();
            }

            private final IntStack markedBuffers = new IntStack();
            private final List<Buffer> buffers = new ArrayList<>();
            private int current = 0;
        };
    }

    public boolean recordIndices = true;

    public final void fail() {
        if (recordIndices) {
            final int popCount = localSegments.pop();
            for (int i = 0; i < popCount; ++i)
                segments.pop();
        }
        revert();
    }

    public final void mark() {
        markedIndices.push(next);
    }

    public final void match(Symbol to) {
        if (recordIndices) {
            segments.push(new ScriptSegment(this, to, markedIndices.peek(), next));
            localSegments.popIncrement();
        }
        revert();
    }

    public final void match(Symbol to, int children) {
        if (recordIndices) {
            final ScriptSegment[] childArray = new ScriptSegment[children];
            for (int i = 0; i < children; ++i)
                childArray[i] = segments.pop();
            segments.push(new ScriptSegment(this, to, childArray));
            localSegments.popSubtract(children - 1);
        }
        revert();
    }

    public final void match(Symbol to, int production, int children) {
        if (recordIndices) {
            final ScriptSegment[] childArray = new ScriptSegment[children];
            for (int i = 0; i < children; ++i)
                childArray[i] = segments.pop();
            segments.push(new ScriptSegment(this, to, childArray) {
                @Override
                public int production() {
                    return production;
                }
            });
            localSegments.popSubtract(children - 1);
        }
        revert();
    }

    public final void matchLiteral(Symbol to, int length) {
        if (recordIndices) {
            segments.push(new ScriptSegment(this, to, next, next + length));
            localSegments.popIncrement();
        }
    }

    public final ScriptSegment parse() {
        return segments.pop().parse();
    }
    public abstract void advance(int by);

    public abstract int read(int offset) throws IOException;

    public abstract void revert();

    protected final IntStack markedIndices = new IntStack();
    protected int next = 0;

    abstract String substring(int begin, int end);

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private Script(AbstractSymbol ignore) throws IOException {
        advance(ignore.accept(this));
    }

    private final Deque<ScriptSegment> segments =  new ArrayDeque<>();
    private final IntStack localSegments = new IntStack();
}
