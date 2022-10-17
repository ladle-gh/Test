package src.symbol;

import src.handler.Handler;

public abstract class Symbol extends AbstractSymbol {
    public final Handler handler;
    public final String name;

    Symbol(String name, Handler handler) {
        this.name = name;
        this.handler = handler;
    }
}
