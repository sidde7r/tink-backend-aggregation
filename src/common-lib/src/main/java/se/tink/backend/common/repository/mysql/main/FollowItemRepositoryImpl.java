package se.tink.backend.common.repository.mysql.main;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public class FollowItemRepositoryImpl implements FollowItemRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    @Override
    public void deleteByUserId(String userId) {
        em.createQuery("DELETE FROM FollowItem f WHERE f.userId = :userId").setParameter("userId", userId)
                .executeUpdate();
    }
}
