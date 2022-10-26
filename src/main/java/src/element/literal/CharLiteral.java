package src.element.literal;

import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

/**
 * <em>Literal</em> symbol matching a single character from input.
 */
final class CharLiteral extends Literal {
    private final int literal;

    CharLiteral(String name, Visitor visitor, int literal) {
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
    protected String defaultName() {
        return '\'' + escape(literal) + '\'';
    }
}
