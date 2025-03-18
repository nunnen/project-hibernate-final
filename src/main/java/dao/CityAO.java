package dao;

import domain.City;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

public class CityAO extends AbstractAO<City> {
    public CityAO(SessionFactory sessionFactory) {
        super(City.class, sessionFactory);
    }

    public int getTotalCount() {
        Session session = super.getCurrentSession();
        Query<Long> query = session.createQuery("select count(c) from City c", Long.class);
        return Math.toIntExact(query.uniqueResult());
    }
}
