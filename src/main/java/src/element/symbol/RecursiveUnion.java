package src.element.symbol;

import src.element.GrammarElement;
import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

/**
 * @see PartialUnion
 */
public final class RecursiveUnion extends Symbol {
    public static RecursiveUnion of(GrammarElement... symbols) {
        return new RecursiveUnion(null, null, symbols);
    }

    public static RecursiveUnion of(String name, GrammarElement... symbols) {
        return new RecursiveUnion(name, null, symbols);
    }

    public static RecursiveUnion of(String name, Visitor visitor, GrammarElement... symbols) {
        return new RecursiveUnion(name, visitor, symbols);
    }

    final boolean[] decomposed;
    private final GrammarElement[] symbols;

    private RecursiveUnion(String name, Visitor visitor, GrammarElement[] symbols) {
        super(name, visitor);

        this.symbols = symbols;
        decomposed = new boolean[symbols.length - 1];
    }

    public PartialUnion recur(int which) {
        return new PartialUnion(this, which);
    }

    @Override
    public int accept(Script input) throws IOException {
        input.mark();
        int result = symbols[0].accept(input);
        if (result != NO_MATCH) {
            input.match(this, 1);
            return result;
        }
        for (int i = 1; i < symbols.length; ++i) {
            if (decomposed[i - 1])
                continue;
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
            if (!decomposed[i]) {
                if (!builder.isEmpty())
                    builder.append(" | ");
                builder.append(symbols[i].unambiguousName());
            }
        }
        return builder.toString();
    }
}
