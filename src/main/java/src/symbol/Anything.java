package src.symbol;

import src.script.Script;

/**
 * <em>Catch-all</em> symbol matching <strong>any</strong> single character read from input.
 */
public final class Anything extends Symbol {
    /**
     * Singleton instance.
     */
    public static final AbstractSymbol INSTANCE = new Anything();

    /**
     * Singleton constructor.
     */
    private Anything() {
        super("[-]", null);
    }

    @Override
    public int accept(Script input) {
        return 1;
    }
}
