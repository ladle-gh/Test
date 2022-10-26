package src.element;

import src.script.Script;

import java.io.IOException;

/**
 * <p>
 *     An {@link GrammarElement} placeholder.
 * </p>
 * <p>
 *     Used to construct symbols utilizing recursion.
 * </p>
 */
public final class Reference extends GrammarElement {

    private GrammarElement target;

    /**
     * Reduces the {@link #accept(Script)} method of this {@code Proxy} to that of {@code target}.
     */
    public void assign(GrammarElement target) {
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
    public String unambiguousName() {
        return target.unambiguousName();
    }
}
