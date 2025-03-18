package dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public abstract class AbstractAO<T> {
    private final Class<T> clazz;

    private final SessionFactory sessionFactory;

    public AbstractAO(Class<T> clazz, SessionFactory sessionFactory) {
        this.clazz = clazz;
        this.sessionFactory = sessionFactory;
    }

    public T getById(int id) {
        return sessionFactory.getCurrentSession().get(clazz, id);
    }

    public List<T> getItems(int offset, int limit) {
        Query<T> query = sessionFactory.getCurrentSession().createQuery("from " + clazz.getName(), clazz);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.list();
    }

    public List<T> findAll() {
        Query<T> query = sessionFactory.getCurrentSession().createQuery("from " + clazz.getName(), clazz);
        return query.list();
    }

    public T persist(T entity) {
        sessionFactory.getCurrentSession().persist(entity);
        return entity;
    }

    public T update(T entity) {
        sessionFactory.getCurrentSession().merge(entity);
        return entity;
    }

    public void delete(T entity) {
        sessionFactory.getCurrentSession().remove(entity);
    }

    public void deleteById(int id) {
        T entity = getById(id);
        delete(entity);
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
}
