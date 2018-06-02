package se.tink.backend.common.repository.mysql.main;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

public class UserOriginRepositoryImpl implements UserOriginRepositoryCustom {

    private static final int MAX_BATCH_SIZE = 10000;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    @Override
    public void deleteByUserId(String userId) {
        em.createQuery("DELETE from UserOrigin u where u.userId = :userId").setParameter("userId", userId)
                .executeUpdate();
    }
}
