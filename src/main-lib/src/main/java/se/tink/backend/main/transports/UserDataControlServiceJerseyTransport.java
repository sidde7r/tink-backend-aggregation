package se.tink.backend.main.transports;

import com.google.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import se.tink.backend.api.UserDataControlService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.main.controllers.UserDataControlServiceController;
import se.tink.backend.rpc.DataExportRequest;
import se.tink.backend.rpc.DataExportRequestListResponse;
import se.tink.backend.rpc.DownloadDataExportCommand;
import se.tink.backend.rpc.ExportDataRequestCommand;
import se.tink.backend.rpc.GdprLoginMethodListResponse;
import se.tink.backend.rpc.GetDataExportRequestsCommand;
import se.tink.backend.utils.LogUtils;

@Path("/api/v1/user-data-control")
public class UserDataControlServiceJerseyTransport implements UserDataControlService {

    UserDataControlServiceController userDataControlServiceController;

    private final LogUtils log = new LogUtils(UserDataControlServiceJerseyTransport.class);

    @Inject
    UserDataControlServiceJerseyTransport(UserDataControlServiceController userDataControlServiceController) {
        this.userDataControlServiceController = userDataControlServiceController;
    }

    @Override
    public DataExportRequest createExport(AuthenticatedUser authenticatedUser) {

        String userId = authenticatedUser.getUser().getId();
        ExportDataRequestCommand command = new ExportDataRequestCommand(userId);
        log.info(userId, "Creating export");

        try {
            return userDataControlServiceController.createAndSaveExportDataRequest(command);
        } catch (Exception e) {
            log.error(userId, "Export creation failed", e);
            throw new WebApplicationException(e);
        }
    }

    @Override
    public GdprLoginMethodListResponse listLoginMethods() {
        return new GdprLoginMethodListResponse(userDataControlServiceController.getLoginMethodsByMarket());
    }

    @Override
    public DataExportRequestListResponse listExports(AuthenticatedUser authenticatedUser) {

        return new DataExportRequestListResponse(userDataControlServiceController
                .getDataExportRequestsThin(new GetDataExportRequestsCommand(authenticatedUser.getUser().getId())));
    }

    @Override
    public Response download(AuthenticatedUser authenticatedUser, String id) {

        String userId = authenticatedUser.getUser().getId();
        DownloadDataExportCommand command = new DownloadDataExportCommand(userId, id);

        log.info(userId, "Download request received with id " + id);

        try {
            byte[] fileByteArray = userDataControlServiceController
                    .downloadDataExport(command.getUserId(), command.getId());
            return Response.ok(fileByteArray, "text/plain; charset=UTF-8")
                    .header("content-disposition", "attachment; filename = myfile.md").build();
        } catch (Exception e) {
            log.error(userId, e);
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
