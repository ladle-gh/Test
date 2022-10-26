package src.element.symbol;

import src.element.GrammarElement;
import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

/**
 * <em>Modifier</em> mapping to an optional symbol, {@code x?}.
 */
public final class Optional extends Symbol {
    public static Optional of(GrammarElement base) {
        return new Optional(null, null, base);
    }

    public static Optional of(String name, GrammarElement base) {
        return new Optional(name, null, base);
    }

    public static Optional of(String name, Visitor visitor, GrammarElement base) {
        return new Optional(name, visitor, base);
    }

    private final GrammarElement base;

    private Optional(String name, Visitor visitor, GrammarElement base) {
        super(name, visitor);
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
    protected String defaultName() {
        return base.unambiguousName() + '?';
    }
}
