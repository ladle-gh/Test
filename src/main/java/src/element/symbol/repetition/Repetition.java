package src.element.symbol.repetition;

import src.element.GrammarElement;
import src.element.symbol.Symbol;
import src.script.Script;
import src.visitor.Visitor;

import java.io.IOException;

public sealed abstract class Repetition extends Symbol
permits Repeat, Span {
    protected final GrammarElement base, ignore;

    public Repetition(String name, Visitor visitor, GrammarElement ignore, GrammarElement base) {
        super(name, visitor);
        this.base = base;
        this.ignore = ignore;
    }

    @Override
    public final int accept(Script input) throws IOException {
        input.mark();
        int result = base.accept(input);
        if (result == NO_MATCH)
            return fail(input);
        int length = result;
        input.mark();
        for (int i = 1; true; ++i) {
            length += input.align(result, ignore);
            result = base.accept(input);
            if (result == NO_MATCH) {
                input.match(this, i);
                return length;
            }
        }
    }

    abstract int fail(Script input);
}
