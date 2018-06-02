package se.tink.backend.export;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.core.DataExportRequest;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

@Path("/gdpr/export")
public interface GdprExportService {

    @GET
    @Path("/monitor/ping")
    @TeamOwnership(Team.PFM)
    @Produces(MediaType.TEXT_PLAIN)
    @AllowAnonymous
    String ping();

    @POST
    @TeamOwnership(Team.PFM)
    @Produces(MediaType.APPLICATION_JSON)
    DataExportRequest createExport(String userId);

    @GET
    @Path("/requests/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    @TeamOwnership(Team.PFM)
    List<DataExportRequest> listDataExportRequests(@PathParam("userid") String userId);

    @GET
    @Path("/{useridandid}")
    @TeamOwnership(Team.PFM)
    byte[] getExportFile(@PathParam("useridandid") String userIdAndId);
}
