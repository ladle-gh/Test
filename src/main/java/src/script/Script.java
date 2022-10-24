package src.script;

import src.symbol.AbstractSymbol;
import src.symbol.Symbol;
import src.util.IntStack;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.*;

import static java.util.Collections.binarySearch;

/**
 * Unified API used by {@link AbstractSymbol symbols} to read character data from a {@link String} or
 * {@link Reader}.
 *
 * @see ScriptSegment
 */
public abstract class Script {
    public  static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * <p>
     *     Implementation details
     *     <ul>
     *         <li>
     *             {@link #mark()}
     *             <ul>
     *                 <li></li>
     *             </ul>
     *         </li>
     *     </ul>
     * </p>
     * @param ignore symbol whose match length is used as alignment
     * @return script representation of {@code input}
     */
    public static Script of(String input, AbstractSymbol ignore) {
        try {
            var script = new Script() {
                @Override
                public int read(int offset) {
                    final int target = next + offset;
                    if (target < input.length())
                        return input.charAt(target);
                    return EOF;
                }

                @Override
                String substring(int begin, int end) {
                    return input.substring(begin, end);
                }
            };
            script.advance(script.query(ignore));
            return script;
        } catch (IOException e) {   // Impossible
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @param ignore symbol whose match length is used as alignment
     * @return script representation of {@code input} with default buffer size
     */
    public static Script of(Reader input, AbstractSymbol ignore) throws IOException {
        return of(input, ignore, DEFAULT_BUFFER_SIZE);
    }

    /**
     *
     * @param ignore symbol whose match length is used as alignment
     * @param bufferSize minimum size of each segmented buffer
     * @return script representation of {@code input}
     * @throws IOException an error occurred during reading of {@code input}
     */
    public static Script of(Reader input, AbstractSymbol ignore, int bufferSize) throws IOException {
        var script = new Script() {
            @Override
            public void mark() {
                super.mark();
                markedBuffers.push(currentBuffer);
            }

            // Contract: offset > 0
            @Override
            public int read(int offset) throws IOException {
                /* Search for location in current buffer */
                var buffer = buffers.get(currentBuffer);
                int target = next + offset;
                if (target < buffer.length)         // Current buffer contains location
                    return buffer.chars[target];
                if (buffer.containsEOF)             // All data has been read from stream
                    return EOF;

                /* Look ahead in next buffer(s) */
                target -= buffer.length;            // target = distance to next
                int relative = target;
                for (int i = currentBuffer; i < buffers.size(); ++i) {
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
                super.revert();
                currentBuffer = markedBuffers.pop();
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
                    if (index < start)
                        return -1;
                    if (index > start + length)
                        return 1;
                    return 0;
                }
            }

            @Override
            String substring(int begin, int end) {
                final int first = binarySearch(buffers, begin), last = binarySearch(buffers, end);
                var buffer1 = buffers.get(first);
                var buffer2 = buffers.get(last);
                begin -= buffer1.start; // begin -> rel. index in buffer1.chars
                end -= buffer2.start;   // begin -> rel. index in buffer2.chars
                if (first == last)
                    return String.valueOf(buffer1.chars, begin, end - begin);
                final var builder = new StringBuilder(end - begin);
                builder.append(buffer1.chars, begin, buffer1.chars.length - begin);
                for (int i = first + 1; i < end; ++i)
                    builder.append(buffers.get(i).chars);
                builder.append(buffer2.chars, end, buffer2.chars.length - end);
                return builder.toString();
            }

            private final IntStack markedBuffers = new IntStack();
            private final List<Buffer> buffers = new ArrayList<>();
            private int currentBuffer = 0;

            {
                buffers.add(new Buffer(input, 0, bufferSize));
            }
        };
        script.advance(script.query(ignore));
        return script;
    }


    protected int next = 0;
    private final IntStack markedIndices = new IntStack(), localSegments = new IntStack();
    private final Deque<ScriptSegment> segments = new ArrayDeque<>();
    private boolean recordIndices = true;

    /**
     * Internal constructor.
     */
    private Script() {
        localSegments.push(0);
    }

    public final void advance(int by) {
        next += by;
    }

    public final void fail() {
        if (recordIndices) {
            final int popCount = localSegments.pop();
            for (int i = 0; i < popCount; ++i)
                segments.pop();
        }
        revert();
    }

    public void mark() {
        markedIndices.push(next);
        if (recordIndices)
                localSegments.push(0);
    }

    public final void match(Symbol to) {
        if (recordIndices) {
            segments.push(new ScriptSegment(this, to, markedIndices.peek(), next));
            localSegments.popIncrement();
        }
        revert();
    }

    public final void match(Symbol to, int totalChildren) {
        if (recordIndices) {
            segments.push(new ScriptSegment(this, to, totalChildren, segments));
            localSegments.popIncrement();
        }
        revert();
    }

    public final void match(Symbol to, int totalChildren, int production) {
        if (recordIndices) {
            segments.push(new ScriptSegment(this, to, totalChildren, segments) {
                @Override
                public int production() {
                    return production;
                }
            });
            localSegments.popIncrement();
        }
        revert();
    }

    public final void matchLiteral(Symbol to, int length) {
        if (recordIndices) {
            segments.push(new ScriptSegment(this, to, next, next + length));
            localSegments.increment();
        }
    }

    public final ScriptSegment parse() {
        return segments.pop().parse();
    }

    /**
     * Reverts
     */
    public void revert() {
        next = markedIndices.pop();
    }

    public final int query(AbstractSymbol s) throws IOException {
        recordIndices = false;
        final int result = s.accept(this);
        recordIndices = true;
        return result;
    }

    /**
     * @return character value at index [<em>{@literal <next index>}</em> + {@code offset}]
     * @throws IOException an error occurred while reading input ({@link Reader} implementation only)
     */
    public abstract int read(int offset) throws IOException;

    /**
     * @param begin first index of substring
     * @param end last index of substring - 1
     * @return substring within {@code Script}
     */
    abstract String substring(int begin, int end);
}
