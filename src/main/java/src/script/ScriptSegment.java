package src.script;

import src.element.GrammarElement;

import java.util.Deque;
import java.util.NoSuchElementException;

import static java.lang.System.out;

/**
 * Collection of indices representing a substring within a {@link Script}, including sub-segments.
 *
 * @see Script
 */
public class ScriptSegment implements LocalSegment {
    private static final Object NONE = new Object();
    private static int tabs = 0;

    static ScriptSegment forUnion(Script parent, GrammarElement match, Deque<ScriptSegment> segments, int production) {
        return new ScriptSegment(parent, match, segments) {
            @Override
            public int production() {
                return production;
            }
        };
    }
    static ScriptSegment forNonterminal(Script parent, GrammarElement match, int totalChildren, Deque<ScriptSegment> segments) {
        return new ScriptSegment(parent, match, totalChildren, segments);
    }
    static ScriptSegment forTerminal(Script parent, GrammarElement match, int begin, int end) {
        return new ScriptSegment(parent, match, begin, end);
    }

    private final ScriptSegment[] children;
    private final GrammarElement match;
    private final Script parent;
    private final int begin, end;
    private Object context = NONE;

    private ScriptSegment(Script parent, GrammarElement match, Deque<ScriptSegment> segments) {
        children = new ScriptSegment[] { segments.pop() };
        begin = children[0].begin;
        end = children[children.length - 1].end;
        this.parent = parent;
        this.match = match;
    }

    private ScriptSegment(Script parent, GrammarElement match, int totalChildren, Deque<ScriptSegment> segments) {
        children = new ScriptSegment[totalChildren];
        for (int i = totalChildren - 1; i >= 0; --i)
            children[i] = segments.pop();
        begin = children[0].begin;
        end = children[children.length - 1].end;
        this.parent = parent;
        this.match = match;
    }

    private ScriptSegment(Script parent, GrammarElement match, int begin, int end) {
        this.begin = begin;
        this.end = end;
        this.parent = parent;
        this.match = match;
        children = null;
    }

    @SuppressWarnings("unchecked")
    public final <T> T context() {
        return (T) context;
    }

    public final ScriptSegment parse() {
        // TODO remove
        return this;
        /*
        if (match.handler instanceof Visitor visitor)
            context = visitor.apply(this);
        else
            ((UnqualifiedVisitor) match.handler).accept(this);
        return this;

         */
    }

    @Override
    public final String capture() {
        return parent.substring(begin, end);
    }

    @Override
    public final ScriptSegment child(int at) {
        if (children == null)
            throw new NoSuchElementException("Attempted access of non-existent child " + at + ".");
        return children[at];
    }

    @Override
    public final String name() {
        return match.name();
    }

    @Override
    public final int totalChildren() {
        return children == null ? 0 : children.length;
    }

    @Override
    public int production() {
        return 0;
    }

    @Override
    public String toString() {
        out.print("\t".repeat(tabs) + "\"" + parent.substring(begin, end) + "\" (" + match.name() + ")");
        if (children != null) {
            out.println(" {");
            ++tabs;
            for (var child : children)
                child.toString();
            --tabs;
            out.println("\t".repeat(tabs) + '}');
        } else
            out.println();
        return "";
    }
}
