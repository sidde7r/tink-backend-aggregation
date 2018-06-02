package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.tink.backend.core.Provider;
@Deprecated
public class ProviderRepositoryImpl implements ProviderRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Deprecated
    public List<Provider> findAll() {
        return em.createQuery("select p from Provider p", Provider.class).getResultList();
    }

    @Override
    @Deprecated
    public List<Provider> findProvidersByMarket(String market) {
        return em.createQuery("SELECT p FROM Provider p WHERE market=:market", Provider.class)
                .setParameter("market", market)
                .getResultList();
    }
}
