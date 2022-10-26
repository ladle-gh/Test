package src.element.symbol;

import src.script.Script;

import java.io.IOException;

/**
 * <em>Composite</em> symbol matching {@link RecursiveUnion}, less a given possible match in addition to those
 * already decomposed by further calls to {@link #PartialUnion(RecursiveUnion, int)}).
 *
 * @see RecursiveUnion
 */
public final class PartialUnion extends Symbol {
    private final RecursiveUnion base;
    private final int which;

    PartialUnion(RecursiveUnion base, int which) {
        super(base.name(), base.visitor);
        this.base = base;
        this.which = which;
    }

    @Override
    public int accept(Script input) throws IOException {
        base.decomposed[which] = true;
        final int result = base.accept(input);
        base.decomposed[which] = false;
        return result;
    }

    @Override
    protected String defaultName() {
        return base.name() + " ~ " + which;
    }
}
