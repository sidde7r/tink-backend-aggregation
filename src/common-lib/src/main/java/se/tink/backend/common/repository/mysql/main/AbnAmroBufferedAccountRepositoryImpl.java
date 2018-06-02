package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.AbnAmroBufferedAccount;

@Transactional
public class AbnAmroBufferedAccountRepositoryImpl implements AbnAmroBufferedAccountRepositoryCustom {

    @PersistenceContext
    private EntityManager em;
    
    @Override
    @Transactional
    public void deleteByCredentialsId(String credentialsId) {
        em.createQuery(
                String.format("DELETE FROM %s a WHERE a.credentialsId = :credentialsId",
                        AbnAmroBufferedAccount.class.getSimpleName()))
        .setParameter("credentialsId", credentialsId)
        .executeUpdate();
    }

    @Override
    public List<AbnAmroBufferedAccount> findByCredentialsId(String credentialsId) {
        return em
                .createQuery(
                        String.format(
                                "SELECT a FROM %s a WHERE a.credentialsId = :credentialsId",
                                AbnAmroBufferedAccount.class.getSimpleName()), AbnAmroBufferedAccount.class)
                .setParameter("credentialsId", credentialsId)
                .getResultList();
    }
}
