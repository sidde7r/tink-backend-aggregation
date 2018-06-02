package se.tink.backend.common.repository.mysql.main;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

public class UserForgotPasswordTokenRepositoryImpl implements UserForgotPasswordTokenRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        em.createQuery("delete from UserForgotPasswordToken t where t.userId = :userId").setParameter("userId", userId)
                .executeUpdate();
    }
}
