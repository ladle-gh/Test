package src.element.symbol;

import src.element.GrammarElement;
import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

/**
 * <em>Operation</em> mapping to a symbol union, {@code x | y}.
 */
public final class Union extends Symbol {
    public static Union of(GrammarElement... symbols) {
        return new Union(null, null, symbols);
    }

    public static Union of(String name, GrammarElement... symbols) {
        return new Union(name, null, symbols);
    }

    public static Union of(String name, Visitor visitor, GrammarElement... symbols) {
        return new Union(name, visitor, symbols);
    }

    private final GrammarElement[] symbols;

    private Union(String name, Visitor visitor, GrammarElement[] symbols) {
        super(name, visitor);
        this.symbols = symbols;
    }

    @Override
    public int accept(Script input) throws IOException {
        input.mark();
        int result;
        for (int i = 0; i < symbols.length; ++i) {
            result = symbols[i].accept(input);
            if (result != NO_MATCH) {
                input.matchUnion(this, i);
                return result;
            }
        }
        input.fail();
        return NO_MATCH;
    }

    @Override
    protected String defaultName() {
        final var builder = new StringBuilder();
        for (int i = 0; i < symbols.length; ++i) {
            if (i != 0)
                builder.append(" | ");
            builder.append(symbols[i].unambiguousName());
        }
        return builder.toString();
    }
}
