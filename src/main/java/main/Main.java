package main;

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
import redis.CityCountry;
import redis.Language;

import java.util.*;
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

        main.sessionFactory.getCurrentSession().close();

        List<Integer> ids = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102);

        long startRedis = System.currentTimeMillis();
        main.testRedisData(ids);
        long stopRedis = System.currentTimeMillis();

        long startMysql = System.currentTimeMillis();
        main.testMysqlData(ids);
        long stopMysql = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMysql - startMysql));

        main.shutdown();
    }

    public List<City> fetchData() {
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction tx = session.beginTransaction();

            List<Country> countries = countryAO.getAll();
            int step = 500;
            int size = cityAO.getTotalCount();
            List<City> cities = new ArrayList<>();

            for (int i = 0; i < size; i += step) {
                cities.addAll(cityAO.getItems(i, step));
            }

            tx.commit();
            return cities;
        }
    }

    public List<CityCountry> transformData(List<City> cities) {
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

    public void pushToRedis(List<CityCountry> data) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private RedisClient prepareRedisClient() {
        RedisClient redisClient = RedisClient.create(RedisURI.create("redis-db", 6379));
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            System.out.println("\nConnected to Redis\n");
        }

        return redisClient;

    }

    public SessionFactory prepareRelationalDb() {
        final SessionFactory sessionFactory;

        sessionFactory = new Configuration()
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(CountryLanguage.class)
                .buildSessionFactory();
        return sessionFactory;
    }

    public void testMysqlData(List<Integer> ids) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                City city = cityAO.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();
            }
            session.getTransaction().commit();
        }
    }

    public void testRedisData(List<Integer> ids) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id : ids) {
                String value = sync.get(String.valueOf(id));
                try {
                    mapper.readValue(value, CityCountry.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void shutdown() {
        if (nonNull(sessionFactory)) {
            sessionFactory.close();
        }
        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }
}
