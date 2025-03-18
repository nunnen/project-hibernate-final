package dao;

import domain.Country;
import org.hibernate.SessionFactory;

public class CountryAO extends AbstractAO<Country> {
    public CountryAO(SessionFactory sessionFactory) {
        super(Country.class, sessionFactory);
    }
}
