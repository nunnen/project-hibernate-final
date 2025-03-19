package benchmark;

import java.util.List;
import java.util.concurrent.TimeUnit;

import domain.City;
import main.Main;
import org.openjdk.jmh.annotations.*;
import redis.CityCountry;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BenchmarkDB {
    private Main main;
    private List<Integer> ids;

    @Setup(Level.Trial)
    public void setup() {
        main = new Main();
        ids = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102);

        List<City> cities = main.fetchData();
        List<CityCountry> preparedData = main.transformData(cities);
        main.pushToRedis(preparedData);
    }

    @Benchmark
    public void testRedis() {
        main.testRedisData(ids);
    }

    @Benchmark
    public void testMySQL() {
        main.testMysqlData(ids);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        main.shutdown();
    }

}
