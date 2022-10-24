package src.symbol;

import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

public final class Wildcard extends Symbol {
    private final AbstractSymbol base;
    private final AbstractSymbol ignore;

    public Wildcard(String name, Visitor visitor, AbstractSymbol ignore, AbstractSymbol base) {
        super(name, visitor);
        this.base = base;
        this.ignore = ignore;
    }

    @Override
    public int accept(Script input) throws IOException {
        input.mark();
        int result = base.accept(input);
        if (result == NO_MATCH) {
            input.match(this);
            return 0;
        }
        int length = result;
        for (int i = 1; true; ++i) {
            input.advance(result);
            input.advance(result = input.query(ignore));
            length += result;
            result = base.accept(input);
            if (result == NO_MATCH) {
                input.match(this, i);
                return length;
            }
            length += result;
        }
    }

    @Override
    public String defaultName() {
        return base.unambiguousName() + "*<" + ignore.name() + '>';
    }
}
