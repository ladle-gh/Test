package src.symbol;

import src.Handler;
import src.Lexer;
import src.Script;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.function.IntPredicate;

import static nullity.Nullity.*;

public abstract class AbstractSymbol implements Lexer {
    public static final int NO_MATCH = -1;

    public static AbstractSymbol group(AbstractSymbol ignore, AbstractSymbol... symbols) {
        return group(null, null, ignore, symbols);
    }

    public static AbstractSymbol group(String name, Handler<?> handler, AbstractSymbol ignore,
                                       AbstractSymbol... symbols) {
        using(__, __, ignore, symbols);
        usingMembers(symbols);

        return new Symbol(name, handler) {
            @Override
            public int accept(Script input) throws IOException {
                using(input);

                input.mark();
                int length = 0, result;
                for (var symbol : symbols) {
                    result = symbol.accept(input);
                    if (result == NO_MATCH) {
                        input.revert();
                        return NO_MATCH;
                    }
                    input.advance(result);
                    length += result;
                    input.advance(result = ignore.accept(input));
                    length += result;
                }
                input.match(this, symbols.length);
                return length;
            }
        };
    }

    public static AbstractSymbol literal(IntPredicate ip) {
        return literal(null, null, ip);
    }

    public static AbstractSymbol literal(String name, Handler<?> handler, IntPredicate ip) {
        using(ip);

        return new Symbol(name, handler) {
            @Override
            public int accept(Script input) throws IOException {
                if (ip.test(input.read(0))) {
                    input.matchLiteral(this, 1);
                    return 1;
                }
                return NO_MATCH;
            }
        };
    }

    public static AbstractSymbol literal(String s) {
        return literal(null, null, s);
    }

    public static AbstractSymbol literal(String name, Handler<?> handler, String s) {
        using(s);

        return new Symbol(name, handler) {
            @Override
            public int accept(Script input) throws IOException {
                int c;
                for (int i = 0; i < s.length(); ++i) {
                    c = input.read(i);
                    if (c != s.charAt(i))
                        return NO_MATCH;
                }
                input.matchLiteral(this, s.length());
                return s.length();
            }
        };
    }

    public static AbstractSymbol literal(int c) {
        return literal(null, null, c);
    }

    public static AbstractSymbol literal(String name, Handler<?> handler, int c) {
        return new Symbol(name, handler) {
            @Override
            public int accept(Script input) throws IOException {
                if (input.read(0) == c) {
                    input.matchLiteral(this, 1);
                    return 1;
                }
                return NO_MATCH;
            }
        };
    }

    public static AbstractSymbol union(AbstractSymbol... symbols) {
        return union(null, null, symbols);
    }

    public static AbstractSymbol union(String name, Handler<?> handler, AbstractSymbol... symbols) {
        using(__, __, symbols);
        usingMembers(symbols);

        return new Symbol(name, handler) {
            @Override
            public int accept(Script input) throws IOException {
                using(input);

                input.mark();
                int result;
                for (var symbol : symbols) {
                    result = symbol.accept(input);
                    if (result != NO_MATCH) {
                        input.match(this, 1);
                        return result;
                    }
                }
                input.revert();
                return NO_MATCH;
            }
        };
    }

    public static RecursiveUnion recursiveUnion(AbstractSymbol... symbols) {
        return recursiveUnion(null, null, symbols);
    }

    public static RecursiveUnion recursiveUnion(String name, Handler<?> handler, AbstractSymbol... symbols) {
        return new RecursiveUnion(name, handler, symbols);
    }

    public static Reference reference() {
        return new Reference();
    }

    public final AbstractSymbol multiple(AbstractSymbol ignore) {
        return multiple(null, null, ignore);
    }

    public final AbstractSymbol multiple(String name, Handler<?> handler, AbstractSymbol ignore) {
        using(__, __, ignore);

        return new Symbol(name, handler) {
            @Override
            public int accept(Script input) throws IOException {
                using(input);

                input.mark();
                int result = AbstractSymbol.this.accept(input);
                if (result == NO_MATCH) {
                    input.revert();
                    return NO_MATCH;
                }
                int length = result;
                input.mark();
                for (int i = 1; true; ++i) {
                    input.advance(result);
                    input.advance(result = ignore.accept(input));
                    length += result;
                    result = AbstractSymbol.this.accept(input);
                    if (result == NO_MATCH) {
                        input.match(this, i);
                        return length;
                    }
                    length += result;
                }
            }
        };
    }

    public final AbstractSymbol any(AbstractSymbol ignore) {
        return any(null, null, ignore);
    }

    public final AbstractSymbol any(String name, Handler<?> handler, AbstractSymbol ignore) {
        using(__, __, ignore);

        return new Symbol(name, handler) {
            @Override
            public int accept(Script input) throws IOException {
                using(input);

                input.mark();
                int result = AbstractSymbol.this.accept(input);
                if (result == NO_MATCH) {
                    input.match(this, 0);
                    return 0;
                }
                int length = result;
                input.mark();
                for (int i = 1; true; ++i) {
                    input.advance(result);
                    input.advance(result = ignore.accept(input));
                    length += result;
                    result = AbstractSymbol.this.accept(input);
                    if (result == NO_MATCH) {
                        input.match(this, i);
                        return length;
                    }
                    length += result;
                }
            }
        };
    }

    public final AbstractSymbol optional() {
        return optional(null, null);
    }

    public final AbstractSymbol optional(String name, Handler<?> handler) {
        return new Symbol(name, handler) {
            @Override
            public int accept(Script input) throws IOException {
                using(input);

                input.mark();
                final int result = AbstractSymbol.this.accept(input);
                if (result == NO_MATCH) {
                    input.match(this, 0);
                    return 0;
                }
                input.match(this, 1);
                return result;
            }
        };
    }

    public final AbstractSymbol except(AbstractSymbol... symbols) {
        return except(null, null, symbols);
    }

    public final AbstractSymbol except(String name, Handler<?> handler, AbstractSymbol... symbols) {
        using(__, __, symbols);
        usingMembers(symbols);

        return new Symbol(name, handler) {
            @Override
            public int accept(Script input) throws IOException {
                using(input);

                input.recordIndices = false;
                input.mark();
                int result = 0 /* Placeholder */;
                for (var symbol : symbols) {
                    result = symbol.accept(input);
                    if (result != NO_MATCH) {
                        input.revert();
                        return NO_MATCH;
                    }
                }
                input.revert();
                input.recordIndices = true;
                input.mark();
                result = AbstractSymbol.this.accept(input);
                if (result == NO_MATCH)
                    input.revert();
                else
                    input.match(this, result);
                return result;
            }
        };
    }

    public final int parse(AbstractSymbol ignore, String input) {
        using(ignore, input);

        try {
            return accept(Script.of(input, ignore));
        } catch (IOException e) {   // Impossible
            throw new UncheckedIOException(e);
        }
    }

    public final int parse(AbstractSymbol ignore, Reader istream) throws IOException {
        using(ignore, istream);

        return accept(Script.of(istream, ignore));
    }

    public abstract String name();
    public abstract Handler<?> handler();
}
