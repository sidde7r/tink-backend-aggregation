package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.rpc.FollowItemListResponse;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.utils.ApiTag;

@Path("/api/v1/follow")
@Api(value = ApiTag.FOLLOW_SERVICE,
        description = "Follow Service handles budgets for expense categories, search strings and savings goals. The idea behind the Follow concept is that the end-user is following a category, a search string or, in the savings goal case, an account.")
public interface FollowService {

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Create Follow Item",
            notes = "Creates a follow item.",
            response = FollowItem.class,
            tags = { ApiTag.FOLLOW_SERVICE, ApiTag.HIDE }
    )
    @TeamOwnership(Team.PFM)
    FollowItem create(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.FOLLOW_WRITE }) AuthenticatedUser authenticatedUser,
            @ApiParam(value = "The follow item to create", required = true) FollowItem followItem
    );

    @POST
    @Path("multiple")
    @TeamOwnership(Team.PFM)
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "", hidden = true, tags = { ApiTag.FOLLOW_SERVICE, ApiTag.HIDE })
    List<FollowItem> create(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.FOLLOW_WRITE }) AuthenticatedUser authenticatedUser,
            List<FollowItem> followItems
    );

    @DELETE
    @Path("{id}")
    @TeamOwnership(Team.PFM)
    @ApiOperation(
            value = "Delete Follow Item",
            notes = "Deletes a follow item.",
            tags = { ApiTag.FOLLOW_SERVICE, ApiTag.HIDE }
    )
    void delete(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.FOLLOW_WRITE }) AuthenticatedUser authenticatedUser,
            @PathParam("id") @ApiParam(value = "The id of the follow item to delete", required = true) String id
    );

    @GET
    @Path("{id}")
    @TeamOwnership(Team.PFM)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Get Follow Item",
            notes = "Gets a follow item on id.",
            response = FollowItem.class,
            tags = { ApiTag.FOLLOW_SERVICE, ApiTag.HIDE }
    )
    FollowItem get(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.FOLLOW_READ }) AuthenticatedUser authenticatedUser,
            @PathParam("id") @ApiParam(value = "The id of the follow item", required = true) String id,
            @QueryParam("period")
            @ApiParam(value = "The period (yyyy-mm) to fetch data for. Defaults to current month.") String period
    );

    @GET
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @TeamOwnership(Team.PFM)
    @ApiOperation(value = "", hidden = true, tags = { ApiTag.FOLLOW_SERVICE, ApiTag.HIDE })
    List<FollowItem> list(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.FOLLOW_READ }) AuthenticatedUser authenticatedUser,
            @QueryParam("period") String period
    );

    @GET
    @Path("list")
    @TeamOwnership(Team.PFM)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "List Follow Items",
            notes = "Lists all follow items. For list Follow Items, no transactions are populated on the Follow Data objects.",
            response = FollowItemListResponse.class,
            tags = { ApiTag.FOLLOW_SERVICE, ApiTag.HIDE }
    )
    FollowItemListResponse getFollowList(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.FOLLOW_READ })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @QueryParam("period")
            @ApiParam(value = "The period (yyyy-mm) to fetch data for. Defaults to current month.") String period
    );

    @GET
    @Path("suggest")
    @TeamOwnership(Team.PFM)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true, tags = { ApiTag.FOLLOW_SERVICE, ApiTag.HIDE })
    List<FollowItem> suggestByType(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.FOLLOW_READ }) AuthenticatedUser authenticatedUser,
            @QueryParam("type") FollowTypes type
    );

    @POST
    @Path("suggest")
    @TeamOwnership(Team.PFM)
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true, tags = { ApiTag.FOLLOW_SERVICE, ApiTag.HIDE })
    FollowItem suggestByTypeAndCriteria(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.FOLLOW_READ }) AuthenticatedUser authenticatedUser,
            @QueryParam("type") FollowTypes type, String criteria
    );

    @PUT
    @Path("{id}")
    @TeamOwnership(Team.PFM)
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Update Follow Item",
            notes = "Updates a follow item. Name and Criteria is updatable.",
            response = FollowItem.class,
            tags = { ApiTag.FOLLOW_SERVICE, ApiTag.HIDE }
    )
    FollowItem update(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.FOLLOW_WRITE })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @PathParam("id") @ApiParam(value = "The id of the follow item to update", required = true) String id,
            @ApiParam(value = "The new follow item", required = true) FollowItem followItem
    );
}
