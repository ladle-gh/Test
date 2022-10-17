package src.symbol;

import src.handler.Handler;
import src.script.Script;

import java.io.IOException;

public final class RecursiveUnion extends Symbol {
    RecursiveUnion(String name, Handler handler, AbstractSymbol[] symbols) {
        super(name, handler);

        this.symbols = symbols;
        decomposed = new boolean[symbols.length - 1];
    }

    public Symbol decompose(int which) {
        return new Symbol(name, handler) {
            @Override
            public int accept(Script input) throws IOException {
                decomposed[which] = true;
                final int result = RecursiveUnion.this.accept(input);
                decomposed[which] = false;
                return result;
            }
        };
    }

    @Override
    public int accept(Script input) throws IOException {
        input.mark();
        int result = symbols[0].accept(input);
        if (result != NO_MATCH) {
            input.match(this, 1);
            return result;
        }
        for (int i = 1; i < symbols.length; ++i) {
            if (decomposed[i - 1])
                continue;
            result = symbols[i].accept(input);
            if (result != NO_MATCH) {
                input.match(this, 1, i);
                return result;
            }
        }
        input.fail();
        return NO_MATCH;
    }

    private final AbstractSymbol[] symbols;
    private final boolean[] decomposed;
}
