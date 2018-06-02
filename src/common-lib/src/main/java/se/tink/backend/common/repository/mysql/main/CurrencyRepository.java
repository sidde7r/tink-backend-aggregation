package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.Currency;

public interface CurrencyRepository extends JpaRepository<Currency, String> {

}
