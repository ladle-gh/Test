package src.symbol;

import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

public final class StringLiteral extends Symbol {
    private final String literal;

    public StringLiteral(String name, Visitor visitor, String literal) {
        super(name, visitor);
        this.literal = literal;
    }

    @Override
    public int accept(Script input) throws IOException {
        int c;
        for (int i = 0; i < literal.length(); ++i) {
            c = input.read(i);
            if (c != literal.charAt(i))
                return NO_MATCH;
        }
        input.matchLiteral(this, literal.length());
        return literal.length();
    }

    @Override
    String defaultName() {
        return '\'' + literal.chars().mapToObj(Symbol::escape).reduce("", String::concat) + '\'';
    }
}
