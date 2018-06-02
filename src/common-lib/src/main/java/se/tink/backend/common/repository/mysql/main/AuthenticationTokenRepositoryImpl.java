package se.tink.backend.common.repository.mysql.main;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AuthenticationTokenRepositoryImpl implements AuthenticationTokenRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public int deleteExpiredTokens(int authenticationTimeToLive) {
        return em.createNativeQuery("DELETE FROM authentication_tokens WHERE created < (NOW() - INTERVAL :ttl SECOND)")
                .setParameter("ttl", Integer.toString(authenticationTimeToLive))
                .executeUpdate();
    }
}
