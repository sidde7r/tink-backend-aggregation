package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import se.tink.backend.core.AbnAmroBufferedTransaction;

public interface AbnAmroBufferedTransactionRepositoryCustom {

    void deleteByCredentialsId(String credentialsId);

    List<AbnAmroBufferedTransaction> findByCredentialsId(String credentialsId);

}
