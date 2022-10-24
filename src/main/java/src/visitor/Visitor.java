package src.visitor;

import src.script.LocalSegment;

import java.util.function.Function;

@FunctionalInterface
public interface Visitor extends Function<LocalSegment, Object> {
    /**
     * Dummy value passed by {@link #of(VoidVisitor)}.
     */
    Object VOID = new Object();

    /**
     * @return {@code Visitor} adaptation of {@code vv}.
     */
    static Visitor of(VoidVisitor vv) {
        return ls -> {
            vv.accept(ls);
            return VOID;
        };
    }
}
