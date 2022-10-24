package src.visitor;

import src.script.LocalSegment;

import java.util.function.Consumer;

@FunctionalInterface
public interface VoidVisitor extends Consumer<LocalSegment> {
}
