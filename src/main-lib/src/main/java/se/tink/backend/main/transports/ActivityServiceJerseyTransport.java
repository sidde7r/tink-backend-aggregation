package se.tink.backend.main.transports;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import java.util.NoSuchElementException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.api.ActivityService;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.core.Activity;
import se.tink.backend.core.User;
import se.tink.backend.main.controllers.ActivityServiceController;
import se.tink.backend.main.utils.ActivityHtmlHelper;
import se.tink.backend.rpc.ActivityQuery;
import se.tink.backend.rpc.ActivityQueryResponse;
import se.tink.backend.rpc.HtmlDetailsResponse;
import se.tink.backend.rpc.HtmlHeadResponse;
import se.tink.backend.rpc.ListHtmlResponse;
import se.tink.backend.rpc.TinkMediaType;

@Path("/api/v1/activities")
@Consumes({
        MediaType.APPLICATION_JSON,
        TinkMediaType.APPLICATION_PROTOBUF
})
@Produces({
        MediaType.APPLICATION_JSON,
        TinkMediaType.APPLICATION_PROTOBUF
})
@Api(value = "Activity Service", description = "Activities are generated after statistics has been generated. "
        + "The activities resides in a sorted list. Sort order is based on relevance.")
public class ActivityServiceJerseyTransport implements ActivityService {
    private final ActivityServiceController activityServiceController;
    private final ActivityHtmlHelper activityHtmlHelper;

    @Context
    private HttpHeaders headers;

    @Inject
    public ActivityServiceJerseyTransport(ActivityServiceController activityServiceController,
            ActivityHtmlHelper activityHtmlHelper) {
        this.activityServiceController = activityServiceController;
        this.activityHtmlHelper = activityHtmlHelper;
    }

    @GET
    @Path("/html")
    @TeamOwnership(Team.PFM)
    @Override
    @ApiOperation(value = "", hidden = true)
    public ListHtmlResponse listHtml(@Authenticated User user, @QueryParam("offset") int offset,
            @QueryParam("limit") int limit, @QueryParam("screenWidth") double screenWidth,
            @QueryParam("screenPpi") double screenPpi) {
        String userAgent = RequestHeaderUtils.getUserAgent(headers);
        List<Activity> activityList;
        try {
            ActivityQuery activityQuery = new ActivityQuery();
            activityQuery.setOffset(offset);
            activityQuery.setLimit(limit);
            activityList = activityServiceController.query(user.getId(), userAgent, activityQuery).getActivities();
        } catch (LockException e) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
        return activityHtmlHelper.listHtml(user, activityList, userAgent, offset, limit, screenWidth, screenPpi);
    }

    @GET
    @Path("/html-details")
    @TeamOwnership(Team.PFM)
    @Override
    @ApiOperation(value = "", hidden = true)
    public HtmlDetailsResponse activityDetails(@Authenticated User user, @QueryParam("id") String id,
            @QueryParam("screenWidth") double screenWidth, @QueryParam("screenPpi") double screenPpi) {
        try {
            return activityHtmlHelper
                    .activityDetails(user, RequestHeaderUtils.getUserAgent(headers), id, screenWidth, screenPpi);
        } catch (NoSuchElementException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("/html-head")
    @TeamOwnership(Team.PFM)
    @Override
    @ApiOperation(value = "", hidden = true)
    public HtmlHeadResponse htmlHead(@Authenticated User user) {
        return activityHtmlHelper.htmlHead(user, RequestHeaderUtils.getUserAgent(headers));
    }

    @PUT
    @Path("{id}/feedback")
    @TeamOwnership(Team.PFM)
    @Override
    @ApiOperation(value = "", hidden = true)
    public void feedback(@Authenticated User user, @PathParam("id") String id, String opinion) {
        try {
            activityServiceController.feedback(user.getId(), id, opinion);
        } catch (NoSuchElementException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @POST
    @Path("/query")
    @TeamOwnership(Team.PFM)
    @ApiOperation(value = "Query activities",
            notes = "Queries activities"
    )
    @Override
    public ActivityQueryResponse query(@Authenticated @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @ApiParam(value = "The query.", required = true) ActivityQuery query) {
        try {
            return activityServiceController
                    .query(authenticatedUser.getUser().getId(), RequestHeaderUtils.getUserAgent(headers), query);
        } catch (LockException e) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
    }

    @GET
    @Path("/{key}")
    @TeamOwnership(Team.PFM)
    @ApiOperation(value = "Get activity",
            notes = "Get activity"
    )
    @Override
    public Activity get(@Authenticated @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @ApiParam(value = "The key of the activity.", required = true) @PathParam("key") String key) {
        try {
            return activityServiceController
                    .get(authenticatedUser.getUser().getId(), RequestHeaderUtils.getUserAgent(headers), key);
        } catch (NoSuchElementException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } catch (LockException e) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
    }
}
