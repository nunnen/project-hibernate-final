import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CityAO;
import dao.CountryAO;
import domain.City;
import domain.Country;
import domain.CountryLanguage;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import redis.CityCountry;
import redis.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class Main {
    private final SessionFactory sessionFactory;
    private final RedisClient redisClient;

    private final ObjectMapper mapper;

    private final CityAO cityAO;
    private final CountryAO countryAO;

    public Main() {
        sessionFactory = prepareRelationalDb();
        cityAO = new CityAO(sessionFactory);
        countryAO = new CountryAO(sessionFactory);

        redisClient = prepareRedisClient();
        mapper = new ObjectMapper();
    }

    public static void main(String[] args) {
        Main main = new Main();
        List<City> cities = main.fetchData();
        List<CityCountry> preparedData = main.transformData(cities);
        main.pushToRedis(preparedData);
        main.shutdown();
    }

    private List<City> fetchData() {
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction tx = session.beginTransaction();

            List<Country> countries = countryAO.getAll();
            int step = 500;
            int size = cityAO.getTotalCount();
            List<City> cities = new ArrayList<>();

            for (int i = 0; i < size; i+=step) {
                cities.addAll(cityAO.getItems(i, step));
            }

            tx.commit();
            return cities;
        }
    }

    private List<CityCountry> transformData(List<City> cities) {
        return cities.stream()
                .map(city -> {
                    CityCountry res = new CityCountry();
                    res.setId(city.getId());
                    res.setName(city.getName());
                    res.setPopulation(city.getPopulation());
                    res.setDistrict(city.getDistrict());

                    Country country = city.getCountry();
                    res.setAlternativeCountryCode(country.getAlternativeCountryCode());
                    res.setContinent(country.getContinent());
                    res.setCountryCode(country.getCode());
                    res.setCountryName(country.getName());
                    res.setCountryPopulation(country.getPopulation());
                    res.setCountryRegion(country.getRegion());
                    res.setCountrySurfaceArea(country.getSurfaceArea());
                    Set<CountryLanguage> countryLanguages = country.getLanguages();
                    Set<Language> languages = countryLanguages.stream()
                            .map(cl -> {
                                Language language = new Language();
                                language.setLanguage(cl.getLanguage());
                                language.setIsOfficial(cl.getIsOfficial());
                                language.setPercentage(cl.getPercentage());
                                return language;
                            }).collect(Collectors.toSet());
                    res.setLanguages(languages);

                    return res;
                }).collect(Collectors.toList());
    }

    private void pushToRedis(List<CityCountry> data) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                }
                catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private RedisClient prepareRedisClient() {
        RedisClient redisClient = RedisClient.create(RedisURI.create("localhost", 6379));
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            System.out.println("\nConnected to Redis\n");
        }

        return redisClient;

    }

    private SessionFactory prepareRelationalDb() {
        final SessionFactory sessionFactory;
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/world");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        properties.put(Environment.HBM2DDL_AUTO, "validate");
        properties.put(Environment.STATEMENT_BATCH_SIZE, "100");

        sessionFactory = new Configuration()
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(CountryLanguage.class)
                .addProperties(properties)
                .buildSessionFactory();
        return sessionFactory;
    }


    private void shutdown() {
        if (nonNull(sessionFactory)) {
            sessionFactory.close();
        }
        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }
}
