package src.element.literal;

import src.element.LexicalUnit;
import src.visitor.Visitor;

import java.util.function.IntPredicate;

public sealed abstract class Literal extends LexicalUnit
permits /* Lexemes */       CharLiteral, PredicateLiteral, StringLiteral,
        /* Special cases */ ShortCircuit {
    /**
     * @return string representation of {@code c} visible to command-line debugger
     */
    static String escape(int c) {
        return switch (c) {
            case '\t' -> "\\t";
            case '\'' -> "\\'";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\\' -> "\\\\";
            case '\f' -> "\\f";
            default   -> Character.isWhitespace(c) || c < 33 || c == 127 ? "<" + c + ">" : String.valueOf((char) c);
        };
    }

    public static Literal of(int literal) {
        return new CharLiteral(null, null, literal);
    }

    public static Literal of(String name, int literal) {
        return new CharLiteral(name, null, literal);
    }

    public static Literal of(String name, Visitor visitor, int literal) {
        return new CharLiteral(name, visitor, literal);
    }

    public static Literal of(IntPredicate literal) {
        return new PredicateLiteral(null, null, literal);
    }

    public static Literal of(String name, IntPredicate literal) {
        return new PredicateLiteral(name, null, literal);
    }

    public static Literal of(String name, Visitor visitor, IntPredicate literal) {
        return new PredicateLiteral(name, visitor, literal);
    }

    public static Literal of(String literal) {
        return new StringLiteral(null, null, literal);
    }

    public static Literal of(String name, String literal) {
        return new StringLiteral(name, null, literal);
    }

    public static Literal of(String name, Visitor visitor, String literal) {
        return new StringLiteral(name, visitor, literal);
    }

    Literal(String name, Visitor visitor) {
        super(name, visitor);
    }

    @Override
    public String unambiguousName() {
        return name;
    }
}
