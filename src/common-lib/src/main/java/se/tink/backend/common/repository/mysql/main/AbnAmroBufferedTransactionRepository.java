package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.AbnAmroBufferedTransaction;
import se.tink.backend.core.AbnAmroBufferedTransactionsPk;

@Repository
public interface AbnAmroBufferedTransactionRepository
        extends JpaRepository<AbnAmroBufferedTransaction, AbnAmroBufferedTransactionsPk>,
        AbnAmroBufferedTransactionRepositoryCustom {
}
