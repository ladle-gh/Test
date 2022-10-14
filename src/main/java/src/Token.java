package src;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

import static src.Token.ReferenceToken;
import static src.Token.RecursiveToken;

public sealed class Token permits ReferenceToken, RecursiveToken {
    public static final int NO_MATCH = -1;

    @Contract(pure = true)
    public static @NotNull Token group(Token ignore, @NotNull Token @NotNull ... series) {
        return literal(input -> {
            int length = 0, result;
            input.mark();
            for (var token : series) {
                result = token.lexer.parse(input);
                if (result == NO_MATCH) {
                    input.revert();
                    return NO_MATCH;
                }
                input.advance(result);
                length += result;
                input.advance(result = ignore.lexer.parse(input));
                length += result;
            }
            input.revert();
            return length;
        }).debug(joinTokens(series, ", "));
    }

    @Contract(pure = true)
    public static @NotNull Token union(@NotNull Token @NotNull ... either) {
        return literal(input -> {
            int result;
            for (var token : either) {
                result = token.lexer.parse(input);
                if (result != NO_MATCH)
                    return result;
            }
            return NO_MATCH;
        }).debug(joinTokens(either, " | "));
    }
    @Contract(pure = true)
    public static @NotNull RecursiveToken recursion(@NotNull Token @NotNull ... either) {
        return new RecursiveToken(either).debug(joinTokens(either, " | "));
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Token literal(Lexer l) {
        final Token result = new Token();
        result.lexer = l;
        return result;
    }

    public static @NotNull Token literal(String s) {
        return literal(input -> {
            int c;
            for (int i = 0; i < s.length(); ++i) {
                c = input.peek(i);
                if (c == LexerParameter.EOF || c != s.charAt(i))
                    return NO_MATCH;
            }
            return s.length();
        }).debug("\"" + s + "\"");
    }

    public static @NotNull Token literal(int c) {
        return literal(input -> {
            if (input.peek(0) > 0)
                System.out.println((char) input.peek(0));
            else
                System.out.println("-1");
            return input.peek(0) == c ? 1 : NO_MATCH;
        }).debug(makeVisible(c));
    }

    public Token except(Token @NotNull ... neither) {
        return literal(input -> {
            int result;
            for (var token : neither) {
                result = token.lexer.parse(input);
                if (result != NO_MATCH)
                    return NO_MATCH;
            }
            return lexer.parse(input);
        }).debug(name + " - " + joinTokens(neither, " | "));
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Token multiple(Token ignore) {
        return literal(input -> {
            int result = lexer.parse(input);
            if (result == NO_MATCH)
                return NO_MATCH;
            int length = result;
            input.mark();
            while (true) {
                input.advance(result);
                input.advance(result = ignore.lexer.parse(input));
                length += result;
                result = lexer.parse(input);
                if (result == NO_MATCH) {
                    input.revert();
                    return length;
                }
                length += result;
            }
        }).debug(name + "+");
    }
    @Contract(value = " -> new", pure = true)
    public @NotNull Token optional() {
        return literal(input -> {
            final int result = lexer.parse(input);
            if (result == NO_MATCH)
                return 0;
            return result;
        }).debug(name + "?");
    }

    @Contract("_ -> new")
    public @NotNull Token any(Token ignore) {
        return multiple(ignore).optional();
    }

    public int parse(Token ignore, String input) {
        try {
            return lexer.parse(new StringParameter(ignore, input));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public int parse(Token ignore, Reader istream) throws IOException {
        return lexer.parse(new ReaderParameter(ignore, istream));
    }

    protected Lexer lexer;

    public static final class RecursiveToken extends Token {
        private RecursiveToken(Token @NotNull [] either) {
            decomposed = new boolean[either.length - 1];
            lexer = input -> {
                int result = either[0].lexer.parse(input);
                if (result != NO_MATCH)
                    return result;
                for (int i = 1; i < either.length; ++i) {
                    if (decomposed[i - 1])
                        continue;
                    result = either[i].lexer.parse(input);
                    if (result != NO_MATCH)
                        return result;
                }
                return NO_MATCH;
            };
        }

        @Contract(value = "_ -> new", pure = true)
        public @NotNull Token decompose(int which) {
            return literal(input -> {
                decomposed[which] = true;
                final int result = lexer.parse(input);
                decomposed[which] = false;
                return result;
            }).debug(name + "~" + which);
        }

        private final boolean[] decomposed;
    }

    @Contract(" -> new")
    public static @NotNull Token.ReferenceToken reference() {
        return new ReferenceToken();
    }
    public static final class ReferenceToken extends Token {
        private ReferenceToken() {
        }
        public void assign(@NotNull Token reference) {
            lexer = reference.lexer;
        }
    }

    public Lexer lexer() {
        return lexer;
    }

    //////////////
    // DEBUGGER //
    //////////////

    protected String name = "<" + this.toString() + ">";
    private boolean debugOn = false;

    @SuppressWarnings("unchecked")
    public <T extends Token> T debug(String name) {
        this.name = name;
        if (!debugOn) {
            Lexer old = lexer;
            lexer = input -> {
                System.out.println("|   ".repeat(tabs) + name);
                ++tabs;
                var l_result = old.parse(input);
                --tabs;
                System.out.println("|   ".repeat(tabs) + "[" + l_result + "]");
                return l_result;
            };
        }
        debugOn = true;
        return (T) this;
    }

    private static int tabs = 0;

    private static @NotNull String joinTokens(Token[] arr, String delimiter) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (int i = 0; i < arr.length; ++i) {
            if (i != 0)
                sb.append(delimiter);
            sb.append(arr[i].name.equals("'\n'") ? "<newline>" : arr[i].name);
        }
        sb.append(')');
        return sb.toString();
    }

    private static String makeVisible(int c) {
        return switch (c) {
            case '\n'               -> "<newline>";
            case LexerParameter.EOF -> "<EOF>";
            default                 -> "'" + (char) c + "'";
        };
    }
}
