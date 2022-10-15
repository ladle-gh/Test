package src.symbol;

import src.Handler;

import static nullity.Nullity.using;

public abstract class Symbol extends AbstractSymbol {
    @Override
    public final String name() {
        return name;
    }

    @Override
    public final Handler<?> handler() {
        return handler;
    }

    Symbol(String name, Handler<?> handler) {
        this.name = name;
        this.handler = handler;
    }

    private final String name;
    private final Handler<?> handler;
}
