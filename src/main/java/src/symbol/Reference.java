package src.symbol;

import src.Handler;
import src.Script;

import java.io.IOException;

import static nullity.Nullity.using;

public final class Reference extends AbstractSymbol {
    public void assign(AbstractSymbol reference) {
        using(reference);

        this.reference = reference;
    }

    @Override
    public int accept(Script input) throws IOException {
        return reference.accept(input);
    }

    @Override
    public String name() {
        return reference.name();
    }

    @Override
    public Handler<?> handler() {
        return reference.handler();
    }

    protected Reference() {
    }

    private AbstractSymbol reference;
}
