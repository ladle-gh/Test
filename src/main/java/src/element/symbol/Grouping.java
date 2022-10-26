package src.element.symbol;

import src.element.GrammarElement;
import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

/**
 * <em>Operation</em> mapping to a symbol grouping, {@code (x y)}.
 */
public final class Grouping extends Symbol {
    public static Grouping of(GrammarElement ignore, GrammarElement... symbols) {
        return new Grouping(null, null, ignore, symbols);
    }

    public static Grouping of(String name, GrammarElement ignore, GrammarElement... symbols) {
        return new Grouping(name, null, ignore, symbols);
    }

    public static Grouping of(String name, Visitor visitor, GrammarElement ignore, GrammarElement... symbols) {
        return new Grouping(name, visitor, ignore, symbols);
    }

    private final GrammarElement ignore;
    private final GrammarElement[] symbols;

    private Grouping(String name, Visitor visitor, GrammarElement ignore, GrammarElement[] symbols) {
        super(name, visitor);
        this.ignore = ignore;
        this.symbols = symbols;
    }

    @Override
    public int accept(Script input) throws IOException {
        input.mark();
        int length = 0, result;
        for (GrammarElement symbol : symbols) {
            result = symbol.accept(input);
            if (result == NO_MATCH) {
                input.fail();
                return NO_MATCH;
            }
            length += input.align(result, ignore);
        }
        input.match(this, symbols.length);
        return length;
    }

    @Override
    protected String defaultName() {
        final var builder = new StringBuilder("(");
        for (int i = 0; i < symbols.length; ++i) {
            if (i != 0)
                builder.append(' ');
            builder.append(symbols[i].unambiguousName());
        }
        return builder.append(")<")
                      .append(ignore.name())    // Alignment always has higher precedence
                      .append('>')
                      .toString();
    }
}
