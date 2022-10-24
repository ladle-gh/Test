package src.symbol;

import src.script.Script;

import java.io.IOException;

/**
 * <p>
 *     An {@link AbstractSymbol} placeholder.
 * </p>
 * <p>
 *     Used to construct symbols utilizing recursion.
 * </p>
 */
public final class Proxy extends AbstractSymbol {
    private AbstractSymbol target;

    /**
     * Reduces the {@link #accept(Script)} method of this {@code Proxy} to that of {@code target}.
     */
    public void assign(AbstractSymbol target) {
        this.target = target;
    }

    @Override
    public int accept(Script input) throws IOException {
        return target.accept(input);
    }

    @Override
    public String name() {
        return target.name();
    }

    @Override
    String unambiguousName() {
        return target.unambiguousName();
    }
}
