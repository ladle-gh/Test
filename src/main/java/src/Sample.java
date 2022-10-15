package src;

import src.symbol.AbstractSymbol;
import src.symbol.RecursiveUnion;
import src.symbol.Reference;

import java.io.*;
import java.net.URL;

import static src.symbol.AbstractSymbol.*;

public class Sample {
    private static final Reference
        ref_modification = reference(),
        ref_operation = reference(),
        ref_expression = reference();

    public static final AbstractSymbol
        ign_none = literal(c -> false),

        all = literal(c -> true),
        whitespace = literal(c -> switch (c) {
            case ' ', '\t', '\n', '\013', '\f', '\r' -> true;
            default -> false;
        }),
        comment = group(ign_none, literal('#'), all, literal('\n')),
        ign_space = union(whitespace, comment).any(ign_none),

        horizontalSpace = literal(c -> switch (c) {
            case ' ', '\t', '\013', '\f' -> true;
            default -> false;
        }),

        identifierStart = literal(c -> switch (c) { // [a-zA-Z]
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' -> true;
            default -> false;
        }),
        identifierPart = literal(c -> switch (c) {  // [a-zA-Z0-9]
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                 '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> true;
            default -> false;
        }),
        identifier = group(ign_none, identifierStart, identifierPart.any(ign_none)),

        charEscape = literal(c -> switch (c) {      // [t'nr\\f]
            case 't', '\'', 'n', 'r', '\\', 'f' -> true;
            default -> false;
        }),
        octalDigit = literal(c -> switch (c) {      // [0-7]
            case '0', '1', '2', '3', '4', '5', '6', '7' -> true;
            default -> false;
        }),
        hexDigit = literal(c -> switch (c) {        // [0-9a-fA-F]
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                 'a', 'b', 'c', 'd', 'e', 'f',
                 'A', 'B', 'C', 'D', 'E', 'F' -> true;
            default -> false;
        }),
        octalEscape = union(octalDigit, group(ign_none, octalDigit, octalDigit),
                            group(ign_none, octalDigit, octalDigit, octalDigit)),
        hexEscape = group(ign_none, literal('u'), hexDigit, hexDigit, hexDigit, hexDigit),
        escapeSequence = group(ign_none, literal('\\'), union(charEscape, octalEscape, hexEscape)),

        switchStart = literal('['),
        switchEnd = literal(']'),
        switchElement = all.except(switchStart, switchEnd),
        charSwitch = group(ign_none, switchStart, union(switchElement.multiple(ign_none), escapeSequence),
                           switchEnd),
        stringBoundary = literal('\''),
        stringElement = all.except(stringBoundary),
        string = group(ign_none, stringBoundary, union(stringElement.multiple(ign_none), escapeSequence),
                       stringBoundary),

        union = literal('|'),
        except = literal('-'),
        optional = literal('?'),
        multiple = literal('+'),
        any = literal('*');

    private static final RecursiveUnion
        expression = recursiveUnion(identifier, charSwitch, string, ref_modification, ref_operation,
                           group(ign_space, literal('('), ref_expression, literal(')')));

    private static final AbstractSymbol
        dcmp2_expression = expression.decompose(2),
        dcmp3_expression = expression.decompose(3),

        modification = union(group(ign_space, dcmp2_expression, optional), group(ign_space, dcmp2_expression, multiple),
                             group(ign_space, dcmp2_expression, any)),
        operation = union(group(ign_space, dcmp3_expression, union, expression),
                          group(ign_space, dcmp3_expression, except, expression)),

        expressions = expression.multiple(ign_space),
        newline = union(literal(System.lineSeparator()), literal(Script.EOF)),
        handler = group(ign_space, literal("->"), identifier, horizontalSpace, newline).optional(),
        productionsStart = group(ign_space, expressions, handler),
        productionsPart = group(ign_space, union, expressions, handler),
        productions = group(ign_space, productionsStart, productionsPart.any(ign_space)),

        rule = group(ign_space, identifier, literal("::"), productions),
        infoRule = group(ign_space, identifier, literal(':'), productions),

        start = union(infoRule, rule).multiple(ign_space);

    static {
        ref_modification.assign(modification);
        ref_operation.assign(operation);
        ref_expression.assign(expression);
    }
    public static Reader getFile(String path) throws Exception {
        URL url = ign_space.getClass().getClassLoader().getResource("src/some.txt");
        assert(url != null);
        return
        new BufferedReader(
            new FileReader(
                new File(url.toURI())
            )
        );
    }
    public static void main(String[] args) throws Exception {
        System.out.println(start.parse(ign_space, getFile("src/some.txt")));
    }
}
