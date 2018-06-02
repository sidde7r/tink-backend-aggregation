package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.auth.bankid.BankIdAuthentication;

public interface BankIdAuthenticationRepository extends JpaRepository<BankIdAuthentication, String>, BankIdAuthenticationRepositoryCustom {

}
