package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import se.tink.backend.core.FraudDetails;

public class FraudDetailsRepositoryImpl implements FraudDetailsRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<FraudDetails> findAllForIds(List<String> fraudDetailIds) {
        return em.createQuery("select fd from FraudDetails fd WHERE fd.id IN (:fraudDetailIds)", FraudDetails.class)
                .setParameter("fraudDetailIds", fraudDetailIds) .getResultList();
    }
}
