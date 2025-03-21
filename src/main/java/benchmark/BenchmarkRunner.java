package benchmark;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;

public class BenchmarkRunner {
    public static void main(String[] args) throws Exception {
        new File("result").mkdirs();

        Options options = new OptionsBuilder()
                .include(BenchmarkDB.class.getSimpleName())
                .forks(1)
                .warmupIterations(1)
                .measurementIterations(1)
                .result("/result/benchmarkResult.json")
                .resultFormat(ResultFormatType.JSON)
                .build();

        new Runner(options).run();
    }
}
