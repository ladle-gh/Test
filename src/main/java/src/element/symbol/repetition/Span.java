package src.element.symbol.repetition;

import src.element.GrammarElement;
import src.visitor.Visitor;
import src.script.Script;

public final class Span extends Repetition {
    public static Span of(GrammarElement ignore, GrammarElement base) {
        return new Span(null, null, ignore, base);
    }

    public static Span of(String name, GrammarElement ignore, GrammarElement base) {
        return new Span(name, null, ignore, base);
    }

    public static Span of(String name, Visitor visitor, GrammarElement ignore, GrammarElement base) {
        return new Span(name, visitor, ignore, base);
    }

    private Span(String name, Visitor visitor, GrammarElement ignore, GrammarElement base) {
        super(name, visitor, ignore, base);
    }

    @Override
    public String defaultName() {
        return base.unambiguousName() + "*..." + ignore.unambiguousName();
    }

    @Override
    int fail(Script input) {
        input.match(this);
        return 0;
    }
}
