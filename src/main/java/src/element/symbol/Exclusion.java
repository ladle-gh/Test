package src.element.symbol;

import src.element.GrammarElement;
import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

/**
 * Operation mapping to a symbol exclusion, {@code x - y}.
 */
public final class Exclusion extends Symbol {
    public static Exclusion of(GrammarElement base, GrammarElement exclude) {
        return new Exclusion(null, null, base, exclude);
    }

    public static Exclusion of(String name, GrammarElement base, GrammarElement exclude) {
        return new Exclusion(name, null, base, exclude);
    }

    public static Exclusion of(String name, Visitor visitor, GrammarElement base, GrammarElement exclude) {
        return new Exclusion(name, visitor, base, exclude);
    }

    private final GrammarElement base, exclude;

    private Exclusion(String name, Visitor visitor, GrammarElement base, GrammarElement exclude) {
        super(name, visitor);
        this.base = base;
        this.exclude = exclude;
    }

    @Override
    public int accept(Script input) throws IOException {
        input.mark();
        int result = input.query(exclude);
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
    protected String defaultName() {
        return base.unambiguousName() + " - " + exclude.unambiguousName();
    }
}
