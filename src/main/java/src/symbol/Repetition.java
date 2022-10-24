package src.symbol;

import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

public final class Repetition extends Symbol {
    private final AbstractSymbol base;
    private final AbstractSymbol ignore;

    public Repetition(String name, Visitor visitor, AbstractSymbol ignore, AbstractSymbol base) {
        super(name, visitor);
        this.base = base;
        this.ignore = ignore;
    }

    @Override
    public int accept(Script input) throws IOException {
        input.mark();
        int result = base.accept(input);
        if (result == NO_MATCH) {
            input.fail();
            return NO_MATCH;
        }
        int length = result;
        input.mark();
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
        return base.unambiguousName() + "+<" + ignore.name() + '>';
    }
}
