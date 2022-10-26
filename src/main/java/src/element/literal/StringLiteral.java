package src.element.literal;

import src.element.GrammarElement;
import src.visitor.Visitor;
import src.script.Script;

import java.io.IOException;

final class StringLiteral extends Literal {
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
                return GrammarElement.NO_MATCH;
        }
        input.matchLiteral(this, literal.length());
        return literal.length();
    }

    @Override
    protected String defaultName() {
        return '\'' + literal.chars().mapToObj(Literal::escape).reduce("", String::concat) + '\'';
    }
}
