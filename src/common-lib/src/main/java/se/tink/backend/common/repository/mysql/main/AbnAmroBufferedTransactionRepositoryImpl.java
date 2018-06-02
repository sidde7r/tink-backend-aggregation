package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.AbnAmroBufferedTransaction;

@Transactional
public class AbnAmroBufferedTransactionRepositoryImpl implements AbnAmroBufferedTransactionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void deleteByCredentialsId(String credentialsId) {
        em.createQuery(
                String.format("DELETE FROM %s t WHERE t.credentialsId = :credentialsId",
                        AbnAmroBufferedTransaction.class.getSimpleName()))
        .setParameter("credentialsId", credentialsId)
        .executeUpdate();
    }

    @Override
    public List<AbnAmroBufferedTransaction> findByCredentialsId(String credentialsId) {
        return em
                .createQuery(
                        String.format(
                                "SELECT a FROM %s a WHERE a.credentialsId = :credentialsId",
                                AbnAmroBufferedTransaction.class.getSimpleName()), AbnAmroBufferedTransaction.class)
                .setParameter("credentialsId", credentialsId)
                .getResultList();
    }

}
