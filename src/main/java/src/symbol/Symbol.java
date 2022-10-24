package src.symbol;

import src.visitor.Visitor;

/**
 * <p>
 *     Symbol qualified with a name and a handler.
 * </p>
 * <p>
 *     Able to be converted to a {@link src.script.ScriptSegment} during lexical processing.
 * </p>
 */
public abstract sealed class Symbol extends AbstractSymbol
permits /* Literals */      CharLiteral, StringLiteral, PredicateLiteral,
        /* Composites */    Exclusion, Grouping, Union, DecomposedUnion, RecursiveUnion,
        /* Modifications */ Optional, Repetition, Wildcard,
        /* Backtracking */  // ExclusiveGrouping, ExclusiveRepetition, ExclusiveWildcard,   // TODO
        /* Catch-all */     Anything, Nothing {
    /**
     * @return string representation of {@code c} visible to command-line debugger
     */
    static String escape(int c) {
        return switch (c) {
            case '\t' -> "\\t";
            case '\'' -> "\\'";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\\' -> "\\\\";
            case '\f' -> "\\f";
            default   -> Character.isWhitespace(c) || c < 33 || c == 127 ? "<" + c + ">" : String.valueOf((char) c);
        };
    }

    /**
     * Called during parsing of adjacent {@link src.script.ScriptSegment}
     */
    public final Visitor visitor;

    private final String name;

    Symbol(String name, Visitor visitor) {
        this.name = name;
        this.visitor = visitor;
    }

    /**
     * @return true if symbol is composite but not an {@link Exclusion}, false otherwise
     */
    boolean isInclusiveComposite() {
        return false;
    }

    /**
     * Base implementation used symbols where {@link #name} is never {@code null}.
     */
    String defaultName() {
        return name;
    }

    @Override
    public final String name() {
        return name == null ? defaultName() : name;
    }

    final String unambiguousName() {
        return isInclusiveComposite() ? '(' + name + ')' : name;
    }
}
