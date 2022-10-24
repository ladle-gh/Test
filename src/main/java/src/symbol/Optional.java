package src.symbol;

import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

public final class Optional extends Symbol {
    private final AbstractSymbol base;

    public Optional(String name, Visitor visitor, AbstractSymbol base) {
        super(name != null ? name : base + "?", visitor);
        this.base = base;
    }

    @Override
    public int accept(Script input) throws IOException {
        input.mark();
        final int result = base.accept(input);
        if (result == NO_MATCH) {
            input.match(this);
            return 0;
        }
        input.match(this, 1);
        return result;
    }

    @Override
    public String defaultName() {
        return base.unambiguousName() + '?';
    }
}
