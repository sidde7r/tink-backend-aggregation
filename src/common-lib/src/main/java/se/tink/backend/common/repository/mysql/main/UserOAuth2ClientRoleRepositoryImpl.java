package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.UserOAuth2ClientRole;

public class UserOAuth2ClientRoleRepositoryImpl implements UserOAuth2ClientRoleRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        em.createQuery(
                String.format("DELETE FROM %s a WHERE a.userId = :userId", UserOAuth2ClientRole.class.getSimpleName()))
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public List<UserOAuth2ClientRole> findByUserId(String userId) {
        return em
                .createQuery(
                        String.format(
                                "SELECT a FROM %s a WHERE a.userId = :userId",
                                UserOAuth2ClientRole.class.getSimpleName()), UserOAuth2ClientRole.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
