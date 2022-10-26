package src;

import src.element.symbol.*;
import src.element.symbol.repetition.Span;
import src.element.*;
import src.element.literal.Literal;

import java.io.*;
import java.net.URL;

import static src.element.literal.ShortCircuit.any;
import static src.element.literal.ShortCircuit.none;

public class Sample {
    private static final Reference
        modification_ref = new Reference(),
        operation_ref = new Reference(),
        expression_ref = new Reference();

    public static final GrammarElement
    /*
        // : []?
        none = Literal.of("NONE", c -> false);

        // : [-]
        any = Literal.of("ANY", c -> true);
    */

        // : [ \t\n\013\f\r]
        whitespace = Literal.of("whitespace", c -> switch (c) {
            case ' ', '\t', '\n', '\013', '\f', '\r' -> true;
            default -> false;
        }),
    
        // : NONE
        UNALIGNED = none,
    
        // : ('#' ANY '\n')..NONE
        comment = Grouping.of("comment", UNALIGNED, Literal.of('#'), any, Literal.of('\n')),

        // : (whitespace | comment)*..NONE
        ALIGNED = Span.of("MULTILINE", UNALIGNED, Union.of(whitespace, comment)),

        // : [a-zA-Z]
        identifierStart = Literal.of("identifierStart", c -> switch (c) {
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                 '_' -> true;
            default  -> false;
        }),

        // : [a-zA-Z0-9]
        identifierPart = Literal.of("identifierPart", c -> switch (c) {
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                 '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                 '_' -> true;
            default  -> false;
        }),

        // : (identifierStart identifierPart*..NONE)..NONE
        identifier = Grouping.of("identifier", UNALIGNED, identifierStart, Span.of(UNALIGNED, identifierPart)),

        // : [t'nr\\f]
        charEscape = Literal.of("charEscape", c -> switch (c) {
            case 't', '\'', 'n', 'r', '\\', 'f' -> true;
            default -> false;
        }),

        // : [0-7]
        octalDigit = Literal.of("octalDigit", c -> switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7' -> true;
            default -> false;
        }),

        // : [0-9a-fA-F]
        hexDigit = Literal.of("hexDigit", c -> switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                 'a', 'b', 'c', 'd', 'e', 'f',
                 'A', 'B', 'C', 'D', 'E', 'F' -> true;
            default -> false;
        }),

        // : octalDigit | (octalDigit octalDigit)..NONE | (octalDigit octalDigit octalDigit)..NONE
        octalEscape = Union.of("octalEscape",
            octalDigit,
            Grouping.of(UNALIGNED, octalDigit, octalDigit),
            Grouping.of(UNALIGNED, octalDigit, octalDigit, octalDigit)
        ),

        // : ('u' hexDigit hexDigit hexDigit hexDigit)..NONE
        hexEscape = Grouping.of("hexEscape", UNALIGNED, Literal.of('u'), hexDigit, hexDigit, hexDigit, hexDigit),

        // : ('\\' (charEscape | octalEscape | hexEscape))..NONE
        escapeSequence = Grouping.of("escapeSequence", UNALIGNED,
            Literal.of('\\'),
            Union.of(charEscape, octalEscape, hexEscape)
        ),

        // : '-'
        minusSign = Literal.of('-'),

        // : '['
        switchStart = Literal.of("switchStart", '['),

        // : ']'
        switchEnd = Literal.of("switchEnd", ']'),

        // : (escapeSequence | ANY) - (switchStart | switchEnd)
        switchElement = Exclusion.of("switchElement", Union.of(escapeSequence, any), Union.of(switchStart, switchEnd)),

        switchCase = Union.of(Grouping.of(UNALIGNED, switchElement, minusSign, switchElement), switchElement),

        // : (switchStart switchCase*..NONE switchEnd)..NONE
        charSwitch = Grouping.of("charSwitch", UNALIGNED, switchStart, Span.of(UNALIGNED, switchCase), switchEnd),

        // : '\''
        stringBoundary = Literal.of("stringBoundary", '\''),

        // : (escapeSequence | ANY) - stringBoundary
        stringElement = Exclusion.of("stringElement", Union.of(escapeSequence, any), stringBoundary),

        // : (stringBoundary stringElement*..NONE stringBoundary)..NONE
        string = Grouping.of("string", UNALIGNED, stringBoundary, Span.of(UNALIGNED, stringElement), stringBoundary),

        // : ('..' identifier)..MULTILINE
        alignment = Grouping.of("alignment", ALIGNED, Literal.of(".."), identifier);

    private static final RecursiveUnion
        // : identifier | charSwitch | string | modification | operation | ('(' expression ')')..MULTILINE
        expression = RecursiveUnion.of("expression",
            identifier,
            charSwitch,
            string,
        modification_ref,
        operation_ref,
            Grouping.of(ALIGNED, Literal.of('('), expression_ref, Literal.of(')'), alignment)
        );

    private static final PartialUnion
        expression_recur2 = expression.recur(2),
        expression_recur3 = expression.recur(3);

    private static final GrammarElement
        // : (expression '?')..MULTILINE | (expression '+')..MULTILINE | (expression '*')..MULTILINE
        modification = Union.of("modification",
            Grouping.of(ALIGNED, expression_recur2, Literal.of('?')),               // Optional
            Grouping.of(ALIGNED, expression_recur2, Literal.of('+'), alignment),    // Repeat
            Grouping.of(ALIGNED, expression_recur2, Literal.of('*'), alignment)     // Wildcard
        ),

        // : (expression '|' expression)..MULTILINE | (expression '-' expression)..MULTILINE
        operation = Union.of("operation",
            Grouping.of(ALIGNED, expression_recur3, Literal.of('|'), expression_recur3),    // Union
            Grouping.of(ALIGNED, expression_recur3, minusSign, expression_recur3)           // Exclusion
        ),

        // : ('->' identifier)..MULTILINE?
        alias = Optional.of("alias", Grouping.of(ALIGNED, Literal.of("->"), identifier)),

        // : (':' (identifier | charSwitch | string | modification | operation))..MULTILINE
        root = Grouping.of("root", ALIGNED,
            Literal.of(':'),
            Union.of(identifier, charSwitch, string, modification, operation)
        ),

        // : ('~' identifier root)..MULTILINE
        align = Grouping.of("align", ALIGNED, Literal.of('~'), identifier, root),

        // : ('!' identifier root)..MULTILINE
        start = Grouping.of("start", ALIGNED, Literal.of('!'), identifier, root),

        // : (identifier alias root)..MULTILINE
        symbol = Grouping.of("symbol", ALIGNED, identifier, alias, root),

        // : (align | start | symbol)*..MULTILINE
        __start__ = Span.of("__start__", ALIGNED, Union.of(align, start, symbol));

    static {
        modification_ref.assign(modification);
        operation_ref.assign(operation);
        expression_ref.assign(expression);
    }
    
    public static Reader getFile(String path) throws Exception {
        URL url = ALIGNED.getClass().getClassLoader().getResource("src/some.txt");
        assert(url != null);
        return new BufferedReader(new FileReader(new File(url.toURI())));
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println(__start__.parse(ALIGNED, getFile("src/some.txt")));
    }
}
