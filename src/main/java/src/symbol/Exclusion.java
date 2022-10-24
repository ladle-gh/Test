package src.symbol;

import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

/**
 * Operation mapping to a symbol exclusion, x - y.
 */
public final class Exclusion extends Symbol {
    private final AbstractSymbol base;
    private final AbstractSymbol symbol;

    public Exclusion(String name, Visitor visitor, AbstractSymbol base, AbstractSymbol symbol) {
        super(name, visitor);
        this.base = base;
        this.symbol = symbol;
    }

    @Override
    public int accept(Script input) throws IOException {
        input.mark();
        int result = input.query(symbol);
        if (result != NO_MATCH) {
            input.revert();
            return NO_MATCH;
        }
        input.revert();
        input.mark();
        result = base.accept(input);
        if (result != NO_MATCH)
            input.match(this, result);
        return result;
    }

    @Override
    String defaultName() {
        return base.unambiguousName() + " - " + symbol.unambiguousName();
    }
}
