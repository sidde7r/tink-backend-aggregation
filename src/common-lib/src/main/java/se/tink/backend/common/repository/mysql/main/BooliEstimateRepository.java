package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.BooliEstimate;

public interface BooliEstimateRepository extends JpaRepository<BooliEstimate, String> {
}
