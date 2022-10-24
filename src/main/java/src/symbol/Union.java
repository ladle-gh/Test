package src.symbol;

import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

public final class Union extends Symbol {
    private final AbstractSymbol[] symbols;

    public Union(String name, Visitor visitor, AbstractSymbol... symbols) {
        super(name, visitor);
        this.symbols = symbols;
    }

    @Override
    public int accept(Script input) throws IOException {
        input.mark();
        int result;
        for (AbstractSymbol symbol : symbols) {
            result = symbol.accept(input);
            if (result != NO_MATCH) {
                input.match(this, 1);
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
            if (i != 0)
                builder.append(" | ");
            builder.append(symbols[i].unambiguousName());
        }
        return builder.toString();
    }
}
