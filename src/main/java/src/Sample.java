package src;

import src.script.Script;
import src.symbol.*;

import java.io.*;
import java.net.URL;

public class Sample {
    private static final Proxy
        modification_proxy = new Proxy(),
        operation_proxy = new Proxy(),
        expression_proxy = new Proxy();

    public static final AbstractSymbol
        /*
            // :: []?
            nothing = new PredicateLiteral("nothing", null, c -> false);

            // :: [-]
            anything = new PredicateLiteral("anything", null, c -> true);
         */

        // :: [ \t\n\013\f\r]
        whitespace = new PredicateLiteral("whitespace", null, c -> switch (c) {
            case ' ', '\t', '\n', '\013', '\f', '\r' -> true;
            default -> false;
        }),

        // ::<nothing> '#' anything '\n'
        comment = new Grouping("comment", null, Nothing.INSTANCE,
            new CharLiteral(null, null, '#'),
            Anything.INSTANCE,
            new CharLiteral(null, null, '\n')
        ),

        // :: (whitespace | comment)*<nothing>
        multiLine = new Wildcard("multiLine", null, Nothing.INSTANCE, new Union(null, null, whitespace, comment)),

        // :: [ \t\013\f]*<nothing>
        inline = new Wildcard("inline", null, Nothing.INSTANCE,
                                        new PredicateLiteral(null, null, c -> switch (c) {
            case ' ', '\t', '\013', '\f' -> true;
            default -> false;
        })),

        // :: [a-zA-Z]
        identifierStart = new PredicateLiteral("identifierStart", null, c -> switch (c) {
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' -> true;
            default -> false;
        }),

        // :: [a-zA-Z0-9]
        identifierPart = new PredicateLiteral("identifierPart", null, c -> switch (c) {
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                 '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> true;
            default -> false;
        }),

        // ::<nothing> identifierStart identifierPart*<nothing>
        identifier = new Grouping("identifier", null, Nothing.INSTANCE, identifierStart,
                                  new Wildcard(null, null, Nothing.INSTANCE, identifierPart)),

        // :: [t'nr\\f]
        charEscape = new PredicateLiteral("charEscape", null, c -> switch (c) {
            case 't', '\'', 'n', 'r', '\\', 'f' -> true;
            default -> false;
        }),

        // :: [0-7]
        octalDigit = new PredicateLiteral("octalDigit", null, c -> switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7' -> true;
            default -> false;
        }),

        // :: [0-9a-fA-F]
        hexDigit = new PredicateLiteral("hexDigit", null, c -> switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                 'a', 'b', 'c', 'd', 'e', 'f',
                 'A', 'B', 'C', 'D', 'E', 'F' -> true;
            default -> false;
        }),

        // :: octalDigit | octalDigit octalDigit | octalDigit octalDigit octalDigit
        octalEscape = new Union("octalEscape", null,
            octalDigit,
            new Grouping(null, null, Nothing.INSTANCE, octalDigit, octalDigit),
            new Grouping(null, null, Nothing.INSTANCE, octalDigit, octalDigit, octalDigit)
        ),

        // ::<nothing> 'u' hexDigit hexDigit hexDigit hexDigit
        hexEscape = new Grouping("hexEscape", null, Nothing.INSTANCE, new CharLiteral(null, null, 'u'),
                                 hexDigit, hexDigit, hexDigit, hexDigit),

        // ::<nothing> '\\' (charEscape | octalEscape | hexEscape)
        escapeSequence = new Grouping("escapeSequence", null, Nothing.INSTANCE,
            new CharLiteral(null, null, '\\'),
            new Union(null, null, charEscape, octalEscape, hexEscape)
        ),

        // :: '[
        switchStart = new CharLiteral("switchStart", null, '['),

        // :: ']'
        switchEnd = new CharLiteral("switchEnd", null, ']'),

        // :: anything - (switchStart | switchEnd)
        switchElement = new Exclusion(null, null, Anything.INSTANCE, switchStart, switchEnd),

        // TODO implement '-' functionality in switch statements
        // TODO check if this is correct
        // ::<nothing> switchStart (switchElement+<nothing> | escapeSequence)*<nothing> switchEnd
        charSwitch = new Grouping("charSwitch", null, Nothing.INSTANCE,
            switchStart,
            new Wildcard(null, null, Nothing.INSTANCE,
                new Union(null, null,
                    new Repetition(null, null, Nothing.INSTANCE, switchElement),
                    escapeSequence
                )
            ),
            switchEnd
        ),

        // :: '\''
        stringBoundary = new CharLiteral("stringBoundary", null, '\''),

        // :: anything - stringBoundary
        stringElement = new Exclusion("stringElement", null, Anything.INSTANCE, stringBoundary),

        // ::<nothing> stringBoundary (stringElement+<nothing> | escapeSequence)*<nothing> stringBoundary
        string = new Grouping("string", null, Nothing.INSTANCE,
            stringBoundary,
            new Wildcard(null, null, Nothing.INSTANCE,
                new Union(null, null,
                    new Repetition(null, null, Nothing.INSTANCE, stringElement),
                    escapeSequence
                )
            ),
            stringBoundary
        ),

        // :: '<' identifier '>'
        alignment = new Grouping("alignment", null, multiLine,
            new CharLiteral(null, null, '<'),
            identifier,
            new CharLiteral(null, null, '>')
        ),

        // :: '.'
        eof = new CharLiteral("eof", null, '.');

    private static final RecursiveUnion
        // :: identifier | charSwitch | string | modification | operation | ('(' expression ')')<multiLine>
        expression = new RecursiveUnion("expression", null,
            identifier,
            charSwitch,
            string,
            modification_proxy,
            operation_proxy,
            new Grouping(null, null, multiLine,
                new CharLiteral(null, null, '('),
                expression_proxy,
                new CharLiteral(null, null, ')'),
                alignment
            ),
            eof     // Epsilon
        );

    private static final DecomposedUnion
        expression_less2 = new DecomposedUnion(expression, 2),
        expression_less3 = new DecomposedUnion(expression, 3);

    private static final AbstractSymbol
        // :: '?'
        optional = new CharLiteral("optional", null, '?'),

        // :: '+'
        repetition = new CharLiteral("multiple", null, '+'),

        // :: '*'
        wildcard = new CharLiteral("any", null, '*'),

        // :: (expression optional)<multiLine> | (expression repetition)<multiLine> | (expression wildcard)<multiLine>
        modification = new Union("modification", null,
            new Grouping(null, null, multiLine, expression_less2, optional),
            new Grouping(null, null, multiLine, expression_less2, repetition, alignment),
            new Grouping(null, null, multiLine, expression_less2, wildcard, alignment)
        ),

        // :: '|'
        union = new CharLiteral("union", null, '|'),

        // :: '-'
        exclusion = new CharLiteral("except", null, '-'),

        // :: (expression union expression)<multiLine> | (expression exclusion expression)<multiLine>
        operation = new Union("operation", null,
            new Grouping(null, null, multiLine, expression_less3, union, expression_less3),
            new Grouping(null, null, multiLine, expression_less3, exclusion, expression_less3)
        ),

        // :: (expression - (expression infoRuleDecl | expression ruleDecl)<multiLine>)+<multiLine>
        expressions = new Repetition("expressions", null, multiLine, expression),

        // :: '{line feed}' | .
        lineBreak = new Union("lineBreak", null,
            new StringLiteral(null, null, System.lineSeparator()),
            new CharLiteral(null, null, Script.EOF)
        ),

        // :: ('->' identifier line_break)<inline>?
        handler = new Optional("handler", null,
            new Grouping(null, null, inline,
                new StringLiteral(null, null, "->"),
                identifier,
                lineBreak
            )
        ),
        // TODO add:
        /*
            <<strict ignore>>

         */
        // :: ('::' expressions handler)<multiline>*<inline>
        ruleProductions = new Repetition("ruleProductions", null, inline,
            new Grouping(null, null, multiLine,
                new StringLiteral(null, null, "::"),
                expressions,
                handler
            )
        ),

        // :: (':' expressions handler)<multiline>*<inline>
        infoRuleProductions = new Repetition("infoRuleProductions", null, inline,
            new Grouping(null, null, multiLine,
                new CharLiteral(null, null, ':'),
                expressions,
                handler
            )
        ),

        rule = group("rule", null, multiLine, identifier, ruleProductions),

        infoRule = group("infoRule", null, multiLine, identifier, infoRuleProductions),

        start = union("start", null, infoRule, rule).multiple(multiLine);

    static {
        modification_proxy.assign(modification);
        operation_proxy.assign(operation);
        expression_proxy.assign(expression);
    }
    public static Reader getFile(String path) throws Exception {
        URL url = multiLine.getClass().getClassLoader().getResource("src/some.txt");
        assert(url != null);
        return
        new BufferedReader(
            new FileReader(
                new File(url.toURI())
            )
        );
    }
    public static void main(String[] args) throws Exception {
        System.out.println(start.parse(multiLine, getFile("src/some.txt")));
    }
}
