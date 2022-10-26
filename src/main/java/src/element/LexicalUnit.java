package src.element;

import src.element.literal.Literal;
import src.element.symbol.Symbol;
import src.visitor.Visitor;

/**
 * <p>
 *     Symbol qualified with a name and a handler.
 * </p>
 * <p>
 *     Able to be converted to a {@link src.script.ScriptSegment} during lexical processing.
 * </p>
 */
public abstract sealed class LexicalUnit extends GrammarElement
permits Symbol, Literal {

    /**
     * Called during parsing of adjacent {@link src.script.ScriptSegment}
     */
    public final Visitor visitor;

    protected final String name;

    protected LexicalUnit(String name, Visitor visitor) {
        this.name = name;
        this.visitor = visitor;
    }

    @Override
    public final String name() {
        return name == null ? defaultName() : name;
    }

    protected abstract String defaultName();
    public abstract String unambiguousName();
}
