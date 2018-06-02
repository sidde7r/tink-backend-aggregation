package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class PostalCodeAreaRepositoryImpl implements PostalCodeAreaRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<String> findAllCities() {
        return em.createQuery("SELECT city from PostalCodeArea group by city order by city", String.class)
                .getResultList();
    }
}
