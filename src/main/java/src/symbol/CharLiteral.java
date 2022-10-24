package src.symbol;

import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

/**
 * <em>Literal</em> symbol matching a single character from input.
 */
public final class CharLiteral extends Symbol {
    private final int literal;

    public CharLiteral(String name, Visitor visitor, int literal) {
        super(name, visitor);
        this.literal = literal;
    }

    @Override
    public int accept(Script input) throws IOException {
        final int query = input.read(0);
        if (query == literal) {
            input.matchLiteral(this, 1);
            return 1;
        }
        return NO_MATCH;
    }

    @Override
    String defaultName() {
        return '\'' + escape(literal) + '\'';
    }
}
