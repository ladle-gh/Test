package nullity;

import java.util.Optional;

public final class Nullity {
    public static final Object __ = new Object();

    public static void using(Object param1) {
        Nullity.param1 = param1;
        if (param1 == null)     throw nullArgument(1);
    }

    public static void using(Object param1, Object param2) {
        Nullity.param1 = param1;
        if (param1 == null)     throw nullArgument(1);
        Nullity.param2 = param2;
        if (param2 == null)     throw nullArgument(2);
    }

    public static void using(Object param1, Object param2, Object param3) {
        Nullity.param1 = param1;
        if (param1 == null)     throw nullArgument(1);
        Nullity.param2 = param2;
        if (param2 == null)     throw nullArgument(2);
        Nullity.param3 = param3;
        if (param3 == null)     throw nullArgument(3);
    }

    public static void using(Object param1, Object param2, Object param3, Object param4) {
        Nullity.param1 = param1;
        if (param1 == null)     throw nullArgument(1);
        Nullity.param2 = param2;
        if (param2 == null)     throw nullArgument(2);
        Nullity.param3 = param3;
        if (param3 == null)     throw nullArgument(3);
        Nullity.param4 = param4;
        if (param4 == null)     throw nullArgument(4);
    }

    public static void usingMembers(Object[] param) {
        for (int i = 0; i < param.length; ++i) {
            if (param[i] == null)
                throw nullMember(param, i);
        }
    }

    private static String caller;
    private static Object param1, param2, param3, param4;

    private static void getCaller() {
        final Optional<String> caller = StackWalker.getInstance().walk(frames -> frames
            .map(StackWalker.StackFrame::getMethodName)
            .skip(1)
            .findFirst()
        );
        if (caller.isPresent()) {
            Nullity.caller = caller.get();
            return;
        }
        throw new IllegalCallerException("Nullity check called without caller.");   // Impossible
    }

    private static NullityException nullArgument(int which) {
        return nullityException("Argument " + which);
    }

    private static NullityException nullityException(String prefix) {
        getCaller();
        return new NullityException(prefix + " of " + caller + " declared non-null, but contains null.");
    }

    private static NullityException nullMember(int which, int index) {
        return nullityException("Index " + index + " of argument " + which);
    }

    private static NullityException nullMember(Object param, int index) {
        if (param == param1)    return nullMember(1, index);
        if (param == param2)    return nullMember(2, index);
        if (param == param3)    return nullMember(3, index);
        if (param == param4)    return nullMember(4, index);
        throw new IllegalArgumentException("Array is not a parameter.");
    }
}