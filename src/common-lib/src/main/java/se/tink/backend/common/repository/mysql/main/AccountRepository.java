package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.tink.backend.core.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, String>, AccountRepositoryCustom {

    List<Account> findByUserId(String userId);

    List<Account> findByCredentialsId(String credentialsId);

    List<Account> findByUserIdAndCredentialsId(String userId, String credentialsId);

    Account findByUserIdAndCredentialsIdAndBankId(String userId, String credentialsId, String bankId);
}
