package se.tink.backend.common.dao;

import com.google.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.assertj.core.util.Strings;
import se.tink.backend.common.repository.cassandra.DataExportFragmentsRepository;
import se.tink.backend.common.repository.cassandra.DataExportsRepository;
import se.tink.backend.common.repository.mysql.main.DataExportRequestRepository;
import se.tink.backend.core.DataExport;
import se.tink.backend.core.DataExportRequest;
import se.tink.libraries.uuid.UUIDUtils;

public class DataExportsDao {
    private final DataExportsRepository dataExportsRepository;
    private final DataExportFragmentsRepository dataExportFragmentsRepository;
    private final DataExportRequestRepository dataExportRequestRepository;

    @Inject
    public DataExportsDao(DataExportRequestRepository dataExportRequestRepository,
            DataExportsRepository dataExportsRepository, DataExportFragmentsRepository dataExportFragmentsRepository) {
        this.dataExportsRepository = dataExportsRepository;
        this.dataExportFragmentsRepository = dataExportFragmentsRepository;
        this.dataExportRequestRepository = dataExportRequestRepository;
    }

    public void deleteByUserId(String userId) {
        List<DataExportRequest> requests = dataExportRequestRepository.findByUserId(userId);

        for (DataExportRequest request : requests) {
            // Todo, fix the naming of the link. Should be named something else.
            if (!Strings.isNullOrEmpty(request.getLink())) {
                deleteDataExport(UUIDUtils.fromString(userId), UUIDUtils.fromString(request.getLink()));
            }

            dataExportRequestRepository.delete(request);
        }
    }

    private void deleteDataExport(UUID userId, UUID id) {
        DataExport export = dataExportsRepository.findOneByUserIdAndId(userId, id);

        for (int index = 0; index < export.getCount(); index++) {
            dataExportFragmentsRepository.deleteByIdAndIndex(id, index);
        }

        dataExportsRepository.delete(export);
    }
}
