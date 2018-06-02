package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.Market;

public interface MarketRepository extends JpaRepository<Market, String> {
    
}
