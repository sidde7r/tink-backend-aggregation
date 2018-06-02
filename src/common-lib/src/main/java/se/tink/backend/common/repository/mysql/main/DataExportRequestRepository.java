package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.DataExportRequest;

@Repository
public interface DataExportRequestRepository extends JpaRepository<DataExportRequest, String> {

    List<DataExportRequest> findByUserId(String userId);
    DataExportRequest findByUserIdAndId(String userId, String id);
}
