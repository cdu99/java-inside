package fr.umlv.javainside;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MutableCallSite;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;
import static java.util.stream.Collectors.groupingBy;

public class StringMatcher {
    private static final MethodHandle EQUALS;

    static {
        try {
            EQUALS = publicLookup().findVirtual(String.class, "equals",
                    methodType(boolean.class, Object.class));
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

    public static MethodHandle matchWithAnInliningCache(Map<String, Integer> mapping) {
        return new InliningCache(mapping).dynamicInvoker();
    }

    private static class InliningCache extends MutableCallSite {
        private static final MethodHandle SLOW_PATH;
        static {
            var lookup = lookup();
            try {
                SLOW_PATH = lookup.findVirtual(InliningCache.class, "slowPath",
                        methodType(int.class, String.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }

        private final Map<String, Integer> mapping;

        public InliningCache(Map<String, Integer> mapping) {
            super(methodType(int.class, String.class));
            this.mapping = mapping;
            setTarget(MethodHandles.insertArguments(SLOW_PATH, 0, this));
        }

        private int slowPath(String text) {
            var index = mapping.getOrDefault(text, -1);

            var test = insertArguments(EQUALS, 1, text);
            var target = dropArguments(constant(int.class, index), 0, String.class);

            var guard = guardWithTest(test, target,
                    new InliningCache(mapping).dynamicInvoker());

            setTarget(guard);
            return index;
        }
    }

    private static final MethodHandle HASH_CODE;
    static {
        var lookup = lookup();
        try {
            HASH_CODE = lookup.findVirtual(String.class, "hashCode", methodType(int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public static MethodHandle matchUsingHashCodes(Map<String, Integer> mapping) {
        var hashMap =
                mapping.keySet().stream().collect(groupingBy(String::hashCode));
        int[] hashes = new int[hashMap.size()];
        MethodHandle[] mhs = new MethodHandle[hashMap.size() + 1];

        var index = 0;
        for (var entry : hashMap.entrySet()) {
            var hash = entry.getKey();
            var strings = entry.getValue();

            hashes[index] = hash;
            var mh = matchWithAnInliningCache(strings.stream().
                    collect(Collectors.toMap(s -> s, mapping::get)));
            mhs[index] = dropArguments(mh, 0, int.class);
            index++;
        }

            mhs[mhs.length - 1] = dropArguments(
                    constant(int.class, -1), 0, int.class, String.class);
        return foldArguments(LookupSwitchGenerator.lookupSwitch(hashes, mhs), HASH_CODE);
    }
}
