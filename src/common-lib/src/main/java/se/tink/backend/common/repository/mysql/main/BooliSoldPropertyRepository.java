package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.BooliSoldProperty;

public interface BooliSoldPropertyRepository extends JpaRepository<BooliSoldProperty, String> {
}
