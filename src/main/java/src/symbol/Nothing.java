package src.symbol;

import src.script.Script;

/**
 * <em>Catch-all</em> symbol matching <strong>no</strong> single character read from input.
 */
public final class Nothing extends Symbol {
    /**
     * Singleton instance.
     */
    public static final AbstractSymbol INSTANCE = new Nothing();

    /**
     * Singleton constructor.
     */
    private Nothing() {
        super("[]?", null);
    }

    @Override
    public int accept(Script input) {
        return 0;
    }
}
