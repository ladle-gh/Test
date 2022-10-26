package src.element.literal;

import src.script.Script;

/**
 * <em>Catch-all</em> symbol matching <strong>one</strong> character read from input.
 */
public final class ShortCircuit extends Literal {
    /**
     * Singleton instances.
     */
    public static final Literal any = new ShortCircuit("[-]", 1), none = new ShortCircuit("[]?", 0);

    private final int length;

    /**
     * Singleton constructor.
     */
    private ShortCircuit(String name, int length) {
        super(name, null);
        this.length = length;
    }

    @Override
    public int accept(Script input) {
        return length;
    }

    @Override
    protected String defaultName() {
        return name;
    }
}
