package se.tink.backend.common.repository.mysql.main;

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        em.createQuery("delete from Account a where a.userId = :userId").setParameter("userId", userId).executeUpdate();
    }

    @Override
    @Transactional
    public void deleteByUserIdAndCredentialsId(String userId, String credentialsId) {
        em.createQuery("delete from Account a where a.userId = :userId AND a.credentialsId = :credentialsId")
                .setParameter("userId", userId)
                .setParameter("credentialsId", credentialsId)
                .executeUpdate();
    }

    @Override
    public void deleteByIds(List<String> accountIds) {
        em.createQuery("delete from Account where id in (:ids)")
                .setParameter("ids", accountIds).executeUpdate();
    }

    /**
     * This enforces an atomic update of the balance which avoids race conditions between processes.
     */
    @Override
    public void addToBalanceById(String accountId, double additionToBalance) {
        em.createQuery("UPDATE Account SET balance = balance + :additionToBalance WHERE id = :accountId")
                .setParameter("additionToBalance", additionToBalance)
                .setParameter("accountId", accountId)
                .executeUpdate();
    }
    
    @Override
    public void setCertainDateById(String accountId, Date certainDate) {
        em.createQuery("UPDATE Account SET certaindate = :certainDate WHERE id = :accountId")
                .setParameter("certainDate", certainDate)
                .setParameter("accountId", accountId)
                .executeUpdate();
    }
}
