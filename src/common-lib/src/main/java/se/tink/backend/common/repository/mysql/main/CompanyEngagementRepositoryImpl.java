package se.tink.backend.common.repository.mysql.main;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class CompanyEngagementRepositoryImpl implements CompanyEngagementsRepositoryCustom {

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public void truncate() {
        em.createNativeQuery("TRUNCATE TABLE companies_engagements").executeUpdate();
    }

}
