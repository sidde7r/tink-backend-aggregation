package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.rpc.DataExportRequest;
import se.tink.backend.rpc.DataExportRequestListResponse;
import se.tink.backend.rpc.GdprLoginMethodListResponse;
import se.tink.backend.utils.ApiTag;

@Path("/api/v1/user-data-control")
@Api(value = ApiTag.USER_DATA_CONTROL_SERVICE, description = "A data export service")
public interface UserDataControlService {

    @POST
    @Path("/data-exports")
    @TeamOwnership(Team.PFM)
    @ApiOperation(value = "Create a data export request",
            notes = "Create a data export request for an authenticated user",
            response = DataExportRequest.class,
            tags = { ApiTag.USER_DATA_CONTROL_SERVICE, ApiTag.HIDE })
    DataExportRequest createExport(@Authenticated @ApiParam(hidden = true) AuthenticatedUser authenticatedUser);

    @GET
    @Path("/login-methods")
    @Produces(MediaType.APPLICATION_JSON)
    @TeamOwnership(Team.PFM)
    @ApiOperation(value = "List market login methods",
            notes = "Returns a list of markets along with their authentication methods",
            response = GdprLoginMethodListResponse.class,
            tags = { ApiTag.USER_DATA_CONTROL_SERVICE, ApiTag.HIDE })
    GdprLoginMethodListResponse listLoginMethods();

    @GET
    @Path("/data-exports")
    @Produces(MediaType.APPLICATION_JSON)
    @TeamOwnership(Team.PFM)
    @ApiOperation(value = "List data export requests",
            notes = "Returns a list with all data export requests for a given authenticated user",
            response = DataExportRequestListResponse.class,
            tags = { ApiTag.USER_DATA_CONTROL_SERVICE, ApiTag.HIDE })
    DataExportRequestListResponse listExports(
            @Authenticated @ApiParam(hidden = true) AuthenticatedUser authenticatedUser);

    @GET
    @Path("/data-exports/{id}/download")
    @TeamOwnership(Team.PFM)
    @ApiOperation(value = "Download data export",
            notes = "Returns a file to download for an authenticated user. Provide a data export request id",
            tags = { ApiTag.USER_DATA_CONTROL_SERVICE, ApiTag.HIDE })
    Response download(@Authenticated @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @ApiParam(required = true, name = "id", value = "Corresponding data export request id")
            @PathParam("id") String id) throws IOException;
}
