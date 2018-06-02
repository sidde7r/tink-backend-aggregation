package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.ApplicationRow;

public interface ApplicationRepository extends JpaRepository<ApplicationRow, String>, ApplicationRepositoryCustom {
    List<ApplicationRow> findAllByUserId(String userId);
}
