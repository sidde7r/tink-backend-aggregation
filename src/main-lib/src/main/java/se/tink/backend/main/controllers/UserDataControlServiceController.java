package se.tink.backend.main.controllers;

import com.google.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import se.tink.backend.common.providers.MarketProvider;
import se.tink.backend.core.GdprLoginMethod;
import se.tink.backend.core.Market;
import se.tink.backend.export.GdprExportService;
import se.tink.backend.export.client.GdprExportServiceFactory;
import se.tink.backend.export.validators.exception.DataExportException;
import se.tink.backend.main.mappers.DataExportRequestMapper;
import se.tink.backend.rpc.DataExportRequest;
import se.tink.backend.rpc.ExportDataRequestCommand;
import se.tink.backend.rpc.GetDataExportRequestsCommand;
import se.tink.libraries.uuid.UUIDUtils;

public class UserDataControlServiceController {

    private MarketProvider marketProvider;
    private final GdprExportServiceFactory gdprExportServiceFactory;

    @Inject
    UserDataControlServiceController(MarketProvider marketProvider,
            @Nullable GdprExportServiceFactory gdprExportServiceFactory) {
        this.marketProvider = marketProvider;
        this.gdprExportServiceFactory = gdprExportServiceFactory;
    }

    public DataExportRequest createAndSaveExportDataRequest(ExportDataRequestCommand command) {
        return DataExportRequestMapper
                .convert(gdprExportServiceFactory.getGdprExportService().createExport(command.getUserId()));
    }

    public List<GdprLoginMethod> getLoginMethodsByMarket() {

        final List<Market> markets = marketProvider.get();

        List<GdprLoginMethod> gdprLoginMethods = markets.stream()
                .map(m -> new GdprLoginMethod(m.getCode(), m.getDescription(), m.getGdprLoginMethods()))
                .collect(Collectors.toList());

        return gdprLoginMethods;
    }

    public List<DataExportRequest> getDataExportRequestsThin(GetDataExportRequestsCommand command) {
        return gdprExportServiceFactory.getGdprExportService().listDataExportRequests(command.getUserId()).stream()
                .map(DataExportRequestMapper::convert).collect(Collectors.toList());
    }

    public byte[] downloadDataExport(UUID userId, UUID id)
            throws DataExportException {

        // Fixme: Resolve hack to bypass strange bug of not allowing two @PathParams
        GdprExportService service = gdprExportServiceFactory.getGdprExportService();
        String userIdAndId = UUIDUtils.toTinkUUID(userId) + "-" + UUIDUtils.toTinkUUID(id);
        try {
            return service.getExportFile(userIdAndId);
        } catch (Exception e){
            throw new DataExportException("Couldn't fetch export file");
        }
    }
}
