package src.element.symbol;

import src.element.LexicalUnit;
import src.element.symbol.repetition.Repetition;
import src.visitor.Visitor;

public sealed abstract class Symbol extends LexicalUnit
permits /* Basic */     Exclusion, Grouping, Optional, Repetition, Union,
        /* Recursive */ PartialUnion, RecursiveUnion {
    protected Symbol(String name, Visitor visitor) {
        super(name, visitor);
    }

    @Override
    public String unambiguousName() {
        return '(' + name + ')';
    }
}
