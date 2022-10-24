package src.symbol;

import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

public final class Grouping extends Symbol {
    private final AbstractSymbol ignore;
    private final AbstractSymbol[] symbols;

    public Grouping(String name, Visitor visitor, AbstractSymbol ignore, AbstractSymbol... symbols) {
        super(name, visitor);
        this.ignore = ignore;
        this.symbols = symbols;
    }

    public AbstractSymbol symbol(int at) {
        return symbols[at];
    }

    @Override
    public int accept(Script input) throws IOException {
        input.mark();
        int length = 0, result;
        for (AbstractSymbol symbol : symbols) {
            result = symbol.accept(input);
            if (result == NO_MATCH) {
                input.fail();
                return NO_MATCH;
            }
            input.advance(result);
            length += result;
            input.advance(result = input.query(ignore));
            length += result;
        }
        input.match(this, symbols.length);
        return length;
    }

    @Override
    boolean isInclusiveComposite() {
        return true;
    }

    @Override
    public String defaultName() {
        final var builder = new StringBuilder("(");
        for (int i = 0; i < symbols.length; ++i) {
            if (i != 0)
                builder.append(' ');
            builder.append(symbols[i].unambiguousName());
        }
        return builder.append(")<")
                      .append(ignore.name())    // Alignment always has higher precedence
                      .append('>')
                      .toString();
    }
}
