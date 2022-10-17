package src.handler;

import src.script.LocalSegment;

@FunctionalInterface
public interface QualifiedVisitor<T> extends Visitor {
    T apply(LocalSegment root);
}
