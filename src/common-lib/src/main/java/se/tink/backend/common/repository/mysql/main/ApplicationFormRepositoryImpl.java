package se.tink.backend.common.repository.mysql.main;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.ApplicationFormRow;

@Transactional
public class ApplicationFormRepositoryImpl implements ApplicationFormRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    @Override
    public void deleteByApplicationId(String applicationId) {
        em.createQuery(
                String.format("DELETE FROM %s af where af.applicationId = :applicationId",
                        ApplicationFormRow.class.getSimpleName())).setParameter("applicationId", applicationId)
                .executeUpdate();
    }
}
