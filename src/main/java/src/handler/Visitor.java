package src.handler;

import src.script.LocalSegment;

import java.util.function.Function;

@FunctionalInterface
public interface Visitor extends Function<LocalSegment, Object>, Handler {
}