package src.symbol;

import src.script.Script;

import java.io.IOException;

public final class Reference extends AbstractSymbol {
    public void assign(AbstractSymbol reference) {
        this.reference = reference;
    }

    @Override
    public int accept(Script input) throws IOException {
        return reference.accept(input);
    }

    Reference() {
    }

    private AbstractSymbol reference;
}
