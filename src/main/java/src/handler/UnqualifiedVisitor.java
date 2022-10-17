package src.handler;

import src.script.LocalSegment;

import java.util.function.Consumer;

@FunctionalInterface
public interface UnqualifiedVisitor extends Consumer<LocalSegment>, Handler {
}
