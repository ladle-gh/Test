package src.element;

import src.script.Script;
import src.script.ScriptSegment;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

/**
 * <p>
 *     Abstract representation of a parser symbol.
 * </p>
 * <p>
 *     Parser symbols may either be used to parse input directly or as an intermediate between super- and sub-symbols.
 * </p>
 */
public abstract sealed class GrammarElement permits LexicalUnit, Reference {
    /**
     * Value returned by {@link #accept(Script)} if no match is found.
     */
    public static final int NO_MATCH = -1;

    /**
     * @return parsed segment containing this symbols
     */
    public final ScriptSegment parse(GrammarElement ignore, String input) {
        try {
            var script = Script.of(input, ignore);
            accept(script);
            return script.parse();
        } catch (IOException e) {   // Impossible
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return parsed segment containing this symbols
     * @throws IOException an error occurred while reading input (if {@code input} derived from {@link Reader})
     */
    public final ScriptSegment parse(GrammarElement ignore, Reader istream) throws IOException {
        var script = Script.of(istream, ignore);
        accept(script);
        return script.parse();
    }

    /**
     * Pushes next matching segment, if one exists, to segment stack of {@code input}.
     * @return length of match
     * @throws IOException an error occurred while reading input (if {@code input} derived from {@link Reader})
     */
    public abstract int accept(Script input) throws IOException;

    /**
     * @return name of symbol instance
     */
    public abstract String name();

    /**
     * @return name, surrounded in parentheses if necessary to preserve operator precedence
     */
    public abstract String unambiguousName();
}
