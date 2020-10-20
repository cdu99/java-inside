package fr.umlv.javainside;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class LoggerBenchMark {
    class Foo {}
//    private static final Logger LOGGER = Logger.of(Foo.class, __ -> {});
//    LoggerBenchMark.no_op          avgt   15  0,377 ± 0,004  ns/op
//    LoggerBenchMark.simple_logger  avgt   15  4,045 ± 0,008  ns/op

    private static final Logger LOGGER = Logger.lambdaOf(Foo.class, __ -> {});
//    LoggerBenchMark.no_op          avgt   15  0,378 ± 0,005  ns/op
//    LoggerBenchMark.simple_logger  avgt   15  0,376 ± 0,002  ns/op



    @Benchmark
    public void no_op() {
        // empty
    }

    @Benchmark
    public void simple_logger() {
        LOGGER.log("");
    }
}

// Implementer une interface --> classe anonyme, lambda ou record