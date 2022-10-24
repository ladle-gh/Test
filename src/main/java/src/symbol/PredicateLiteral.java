package src.symbol;

import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;
import java.util.function.IntPredicate;

public final class PredicateLiteral extends Symbol {
    private final IntPredicate literal;

    public PredicateLiteral(String name, Visitor visitor, IntPredicate literal) {
        super(name, visitor);
        this.literal = literal;
    }

    @Override
    public int accept(Script input) throws IOException {
        final int query = input.read(0);
        if (literal.test(query)) {
            input.matchLiteral(this, 1);
            return 1;
        }
        return NO_MATCH;
    }

    @Override
    public String defaultName() {
        return literal.toString() /* = Object.toString() */;
    }
}
