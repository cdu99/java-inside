package fr.umlv.javainside;

import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class SwitchBenchMark {
    private static final List<String> TEXTS = List.of("foo", "bar", "boo", "abc");
    private static final Map<String, Integer> CASES = Map.of("foo", 0, "bar", 1, "baz", 2, "boo", 3, "booze", 4, "Aa", 5, "BB", 6);

    private static final MethodHandle MATCH_WITH_GWTS = StringMatcher.matchWithGWTs(CASES);
    private static final MethodHandle MATCH_INLINE_CACHE = StringMatcher.matchWithAnInliningCache(CASES);
    private static final MethodHandle MATCH_USING_HASHCODES = StringMatcher.matchUsingHashCodes(CASES);

    @Benchmark
    public int match_with_gwts() throws Throwable {
        var sum = 0;
        for (var text : TEXTS) {
            sum += (int) MATCH_WITH_GWTS.invokeExact(text);
        }
        return sum;
    }
    // Result:
    // Benchmark                        Mode  Cnt   Score   Error  Units
    // SwitchBenchMark.match_with_gwts  avgt   15  27,294 ± 3,916  ns/op

    @Benchmark
    public int match_with_inlining_cache() throws Throwable {
        var sum = 0;
        for (var text : TEXTS) {
            sum += (int) MATCH_INLINE_CACHE.invokeExact(text);
        }
        return sum;
    }

    // Benchmark                                  Mode  Cnt   Score   Error  Units
    // SwitchBenchMark.match_with_inlining_cache  avgt   15  20,488 ± 0,249  ns/op

    @Benchmark
    public int match_with_hash_codes() throws Throwable {
        var sum = 0;
        for (var text : TEXTS) {
            sum += (int) MATCH_USING_HASHCODES.invokeExact(text);
        }
        return sum;
    }

    // Benchmark                              Mode  Cnt   Score   Error  Units
    // SwitchBenchMark.match_with_hash_codes  avgt   15  14,215 ± 0,496  ns/op

}