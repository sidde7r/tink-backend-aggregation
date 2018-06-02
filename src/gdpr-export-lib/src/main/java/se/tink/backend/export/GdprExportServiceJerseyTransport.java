package se.tink.backend.export;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.core.DataExportRequest;
import se.tink.backend.export.controller.ExportController;
import se.tink.backend.export.validators.exception.DataExportException;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class GdprExportServiceJerseyTransport implements GdprExportService {

    private final ExportController exportController;
    private LogUtils log = new LogUtils(GdprExportService.class);

    @Inject
    public GdprExportServiceJerseyTransport(ExportController exportController){
        this.exportController = exportController;
    }

    @Override
    public String ping() {
        return "pong";
    }

    @Override
    public DataExportRequest createExport(String userId) {
        try {
            DataExportRequest storedRequest = exportController.storeRequest(userId);

            exportController.generateAndStoreExportAsync(userId, storedRequest);

            return storedRequest;
        } catch (Exception e){
            log.error(userId + "Could not create export.", e);
            throw new DataExportException(e);
        }
    }

    @Override
    public List<DataExportRequest> listDataExportRequests(String userId) {
        return exportController.getDataExportRequests(userId);
    }

    @Override
    public byte[] getExportFile(String userIdAndId) {
        // Fixme: Resolve hack to bypass strange bug of not allowing two @PathParams
        List<String> ids = Lists.newArrayList(userIdAndId.split("-"));
        ids.forEach(UUIDUtils::isValidTinkUUID);

        try {
            return exportController.fetchExport(UUIDUtils.fromTinkUUID(ids.get(0)), UUIDUtils.fromTinkUUID(ids.get(1)));
        } catch (Exception e) {
            log.error(ids.get(0), "Could not fetch Export");
            throw new DataExportException(e);
        }
    }
}
