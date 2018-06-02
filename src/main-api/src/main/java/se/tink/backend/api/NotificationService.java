package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.rpc.NotificationQuery;
import se.tink.backend.rpc.NotificationQueryResponse;
import se.tink.backend.utils.ApiTag;

@Path("/api/v1/notifications")
@Api(value = ApiTag.NOTIFICATION_SERVICE, description = "Notification Service")
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface NotificationService {

    @POST
    @Path("{id}/received")
    @TeamOwnership(Team.PFM)
    @ApiOperation(
            value = "Mark a notification as received",
            notes = "Marks a notification as received (only to be used for acknowledging encrypted notifications)",
            tags = { ApiTag.NOTIFICATION_SERVICE, ApiTag.HIDE }
    )
    void received(@PathParam("id") @ApiParam(
            value = "The id of the notification",
            required = true,
            example = "8937fa00166946cbbcbbec569c9d6e90") String id
    );

    @POST
    @Path("{id}/read")
    @TeamOwnership(Team.PFM)
    @ApiOperation(
            value = "Mark a notification as read",
            notes = "Marks a notification as read",
            tags = { ApiTag.NOTIFICATION_SERVICE, ApiTag.HIDE }
    )
    void read(@PathParam("id") @ApiParam(
            value = "The id of the notification",
            required = true,
            example = "8937fa00166946cbbcbbec569c9d6e90") String id
    );

    @POST
    @Path("/query")
    @TeamOwnership(Team.PFM)
    @ApiOperation(
            value = "Query notifications",
            notes = "Queries notifications",
            tags = { ApiTag.NOTIFICATION_SERVICE, ApiTag.HIDE }
    )
    NotificationQueryResponse queryNotifications(
            @Authenticated AuthenticatedUser user,
            @ApiParam(value = "The query.", required = true) NotificationQuery query
    );
}
