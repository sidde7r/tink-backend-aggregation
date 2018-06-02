package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.Giro;

public interface GiroRepository extends JpaRepository<Giro, String> {
    public Giro findOneByAccountNumber(String accountNumber);
}
