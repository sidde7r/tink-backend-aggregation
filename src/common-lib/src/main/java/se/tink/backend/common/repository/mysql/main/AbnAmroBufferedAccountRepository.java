package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.AbnAmroBufferedAccount;
import se.tink.backend.core.AbnAmroBufferedAccountPk;

@Repository
public interface AbnAmroBufferedAccountRepository extends
        JpaRepository<AbnAmroBufferedAccount, AbnAmroBufferedAccountPk>, AbnAmroBufferedAccountRepositoryCustom {

}
