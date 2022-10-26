package src.element.symbol.repetition;

import src.element.GrammarElement;
import src.visitor.Visitor;
import src.script.Script;

public final class Repeat extends Repetition {
    public static Repeat of(GrammarElement ignore, GrammarElement base) {
        return new Repeat(null, null, ignore, base);
    }

    public static Repeat of(String name, GrammarElement ignore, GrammarElement base) {
        return new Repeat(name, null, ignore, base);
    }

    public static Repeat of(String name, Visitor visitor, GrammarElement ignore, GrammarElement base) {
        return new Repeat(name, visitor, ignore, base);
    }

    private Repeat(String name, Visitor visitor, GrammarElement ignore, GrammarElement base) {
        super(name, visitor, ignore, base);
    }

    @Override
    public String defaultName() {
        return base.unambiguousName() + "+..." + ignore.unambiguousName();
    }

    @Override
    int fail(Script input) {
        input.fail();
        return NO_MATCH;
    }
}
