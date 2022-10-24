package src.symbol;

import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

/**
 * @see DecomposedUnion
 */
public final class RecursiveUnion extends Symbol {
    final boolean[] decomposed;
    private final AbstractSymbol[] symbols;

    public RecursiveUnion(String name, Visitor visitor, AbstractSymbol... symbols) {
        super(name, visitor);

        this.symbols = symbols;
        decomposed = new boolean[symbols.length - 1];
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
                input.match(this, i, 1);
                return result;
            }
        }
        input.fail();
        return NO_MATCH;
    }

    @Override
    boolean isInclusiveComposite() {
        return true;
    }

    @Override
    public String defaultName() {
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
