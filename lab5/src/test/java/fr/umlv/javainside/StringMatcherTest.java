package fr.umlv.javainside;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StringMatcherTest {
    private void testMHWithCases(MethodHandle mh, Map<String, Integer> cases) throws Throwable {
        for(var entry: cases.entrySet()) {
            var acase = entry.getKey();
            var index = entry.getValue();
            assertEquals(index, (int) mh.invokeExact(acase));
        }
    }

    private void testMHWithUnknownCase(MethodHandle mh, List<String> unknownCases) throws Throwable {
        for(var acase: unknownCases) {
            assertEquals(-1, (int) mh.invokeExact(acase));
        }
    }

    @Test
    public void matchWithGWTs() {
        var cases = Map.of("foo", 1, "bar", 2, "baz", 3);
        var mh = StringMatcher.matchWithGWTs(cases);
        assertAll(
                () -> testMHWithCases(mh, cases),
                () -> testMHWithUnknownCase(mh, List.of("batman", "joker", "robin"))
        );
    }

    @Test
    public void matchWithAnInliningCache() {
        var cases = Map.of("foo", 1, "bar", 2, "baz", 3);
        var mh = StringMatcher.matchWithAnInliningCache(cases);
        assertAll(
                () -> testMHWithCases(mh, cases),
                () -> testMHWithUnknownCase(mh, List.of("batman", "joker", "robin"))
        );
    }

//    @Test
//    public void matchUsingHashCodes() {
//        var cases = Map.of("foo", 1, "bar", 2, "baz", 3);
//        var mh = StringMatcher.matchUsingHashCodes(cases);
//        assertAll(
//                () -> testMHWithCases(mh, cases),
//                () -> testMHWithUnknownCase(mh, List.of("batman", "joker", "robin"))
//        );
//    }
}