package src;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;

import static src.Token.*;

public class Sample {
    private static final ReferenceToken
        _modification = reference().debug("_modification"),
        _operation = reference().debug("_operation"),
        _expression = reference().debug("_expression");

    public static final Token
        ignoreNone = literal(input -> 0).debug("ignoreNone"),

        all = literal(input -> 1).debug("all"),
        whitespace = literal(input -> switch (input.peek(0)) {
            case ' ', '\t', '\n', '\013', '\f', '\r' -> 1;
            default -> NO_MATCH;
        }).debug("whitespace"),
        comment = group(ignoreNone, literal('#'), all, literal('\n')).debug("comment"),
        ignoreSpace = union(whitespace, comment).any(ignoreNone).debug("ignoreSpace"),

        horizontalSpace = literal(input -> switch (input.peek(0)) {
            case ' ', '\t', '\013', '\f' -> 1;
            default -> NO_MATCH;
        }).debug("horizontalSpace"),

        identifierStart = literal(input -> {
            System.out.println(input.peek(0));
            switch (input.peek(0)) {  // [a-zA-Z]
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' : return 1;
            default : return NO_MATCH;
        }}).debug("identifierStart"),
        identifierPart = literal(input -> switch (input.peek(0)) {  // [a-zA-Z0-9]
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                 '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> 1;
            default -> NO_MATCH;
        }).debug("identifierPart"),
        identifier = group(ignoreNone, identifierStart, identifierPart.any(ignoreNone)).debug("identifier"),

        charEscape = literal(input -> switch (input.peek(0)) {       // [t'nr\\f]
            case 't', '\'', 'n', 'r', '\\', 'f' -> 1;
            default -> NO_MATCH;
        }).debug("charDigit"),
        octalDigit = literal(input -> switch (input.peek(0)) {       // [0-7]
            case '0', '1', '2', '3', '4', '5', '6', '7' -> 1;
            default -> NO_MATCH;
        }).debug("octalDigit"),
        hexDigit = literal(input -> switch (input.peek(0)) {         // [0-9a-fA-F]
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                 'a', 'b', 'c', 'd', 'e', 'f',
                 'A', 'B', 'C', 'D', 'E', 'F' -> 1;
            default -> NO_MATCH;
        }).debug("hexDigit"),
        octalEscape = union(octalDigit, group(ignoreNone, octalDigit, octalDigit),
                            group(ignoreNone, octalDigit, octalDigit, octalDigit)).debug("octalEscape"),
        hexEscape = group(ignoreNone, literal('u'), hexDigit, hexDigit, hexDigit, hexDigit).debug("hexEscape"),
        escapeSequence = group(ignoreNone, literal('\\'),
                               union(charEscape, octalEscape, hexEscape)).debug("escapeSequence"),

        switchStart = literal('[').debug("switchStart"),
        switchEnd = literal(']').debug("switchEnd"),
        switchElement = all.except(switchStart, switchEnd).debug("switchElement"),
        charSwitch = group(ignoreNone, switchStart, union(switchElement.multiple(ignoreNone), escapeSequence),
                           switchEnd).debug("charSwitch"),
        stringBoundary = literal('\'').debug("stringBoundary"),
        stringElement = all.except(stringBoundary).debug("stringElement"),
        string = group(ignoreNone, stringBoundary, union(stringElement.multiple(ignoreNone), escapeSequence),
                       stringBoundary).debug("string"),

        union = literal('|').debug("union"),
        except = literal('-').debug("except"),
        optional = literal('?').debug("optional"),
        multiple = literal('+').debug("multiple"),
        any = literal('*').debug("any");

    private static final RecursiveToken
        expression = recursion(identifier, charSwitch, string, _modification, _operation,
                           group(ignoreSpace, literal('('), _expression, literal(')'))).debug("expression");

    private static final Token
        _2expression = expression.decompose(2).debug("_2expression"),
        _3expression = expression.decompose(3).debug("_3expression"),

        modification = union(group(ignoreSpace, _2expression, optional), group(ignoreSpace, _2expression, multiple),
                             group(ignoreSpace, _2expression, any)).debug("modification"),
        operation = union(group(ignoreSpace, _3expression, union, expression),
                          group(ignoreSpace, _3expression, except, expression)).debug("operation"),

        expressions = expression.multiple(ignoreSpace),
        newline = union(literal(System.lineSeparator()), literal(LexerParameter.EOF)).debug("newline"),
        handler = group(ignoreSpace, literal("->"), identifier, horizontalSpace, newline).optional().debug("handler"),
        productionsStart = group(ignoreSpace, expressions, handler).debug("productionsStart"),
        productionsPart = group(ignoreSpace, union, expressions, handler).debug("productionsPart"),
        productions = group(ignoreSpace, productionsStart, productionsPart.any(ignoreSpace)).debug("productions"),

        rule = group(ignoreSpace, identifier, literal("::"), productions).debug("rule"),
        infoRule = group(ignoreSpace, identifier, literal(':'), productions).debug("infoRule"),

        start = union(infoRule, rule).multiple(ignoreSpace).debug("start");

    static {
        _modification.assign(modification);
        _operation.assign(operation);
        _expression.assign(expression);
    }
    @Contract("_ -> new")
    public static @NotNull Reader getFile(String path) throws Exception {
        URL url = ignoreSpace.getClass().getClassLoader().getResource("src/some.txt");
        assert(url != null);
        return
        new BufferedReader(
            new FileReader(
                new File(url.toURI())
            )
        );
    }
    public static void main(String[] args) throws Exception {
        System.out.println(start.parse(ignoreSpace, getFile("src/some.txt")));
    }
}
