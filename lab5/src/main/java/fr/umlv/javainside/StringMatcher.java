package fr.umlv.javainside;

import java.lang.invoke.MethodHandle;
import java.util.Map;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

public class StringMatcher {
    private static final MethodHandle EQUALS;

    static {
        try {
            EQUALS = publicLookup().findVirtual(String.class, "equals", methodType(boolean.class, Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public static MethodHandle matchWithGWTs(Map<String, Integer> mapping) {
        var mh = dropArguments(constant(int.class, -1), 0, String.class);
        for (var entry : mapping.entrySet()) {
            var text = entry.getKey();
            var index = entry.getValue();
            var test = insertArguments(EQUALS, 1, text);
            var target = dropArguments(constant(int.class, index), 0, String.class);
            mh = guardWithTest(test, target, mh);
        }
        return mh;
    }
}
