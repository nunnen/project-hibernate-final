package dao;

import domain.Country;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class CountryAO extends AbstractAO<Country> {
    public CountryAO(SessionFactory sessionFactory) {
        super(Country.class, sessionFactory);
    }

    @Override
    public List<Country> getAll() {
        Session session = super.getCurrentSession();
        Query<Country> query = session.createQuery("SELECT c FROM Country c join fetch c.languages", Country.class);
        return query.list();
    }
}
