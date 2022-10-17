package src.script;

import src.symbol.Symbol;
import src.handler.UnqualifiedVisitor;
import src.handler.Visitor;

public class ScriptSegment implements LocalSegment {
    @SuppressWarnings("unchecked")
    public final <T> T context() {
        return (T) context;
    }

    public final ScriptSegment parse() {
        if (match.handler instanceof Visitor visitor)
            context = visitor.apply(this);
        else
            ((UnqualifiedVisitor) match.handler).accept(this);
        return this;
    }

    @Override
    public final String capture() {
        return parent.substring(begin, end);
    }

    @Override
    public final ScriptSegment child(int at) {
        if (children == null)
            throw new /* SegmentChildException */ RuntimeException();   // TODO implement
        return children[at];
    }

    @Override
    public final String name() {
        return match.name;
    }

    @Override
    public final int totalChildren() {
        return children == null ? 0 : children.length;
    }

    @Override
    public int production() {
        return 0;
    }

    // Contract: children in ascending order
    ScriptSegment(Script parent, Symbol match, ScriptSegment[] children) {
        begin = children[0].begin;
        end = children[children.length - 1].end;
        this.parent = parent;
        this.match = match;
        this.children = children;
    }

    ScriptSegment(Script parent, Symbol match, int begin, int end) {
        this.begin = begin;
        this.end = end;
        this.parent = parent;
        this.match = match;
        children = null;
    }

    private static final Object NONE = new Object();

    private final ScriptSegment[] children;
    private final Symbol match;
    private final Script parent;
    private final int begin, end;
    private Object context = NONE;
}
