package se.tink.backend.common.repository.mysql.main;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class BankIdAuthenticationRepositoryImpl implements BankIdAuthenticationRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public int deleteExpiredTokens(int bankidAuthenticationTimeToLive) {
        return em.createNativeQuery("DELETE FROM bankid_authentications WHERE created < (NOW() - INTERVAL :ttl SECOND)")
                .setParameter("ttl", Integer.toString(bankidAuthenticationTimeToLive))
                .executeUpdate();
    }
}
