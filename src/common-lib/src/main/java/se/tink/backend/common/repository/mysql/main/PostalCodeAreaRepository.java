package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.PostalCodeArea;

public interface PostalCodeAreaRepository extends JpaRepository<PostalCodeArea, String>, PostalCodeAreaRepositoryCustom {
}
