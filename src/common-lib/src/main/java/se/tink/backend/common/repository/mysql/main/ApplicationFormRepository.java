package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.ApplicationFormRow;

public interface ApplicationFormRepository extends JpaRepository<ApplicationFormRow, String>,
        ApplicationFormRepositoryCustom {

    List<ApplicationFormRow> findAllByApplicationId(String applicationId);
}
