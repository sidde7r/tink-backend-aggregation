package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.User;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.rpc.SearchResponse;
import se.tink.backend.rpc.TinkMediaType;

@Path("/api/v1/search")
@Consumes({
    MediaType.APPLICATION_JSON
})
@Produces({
    MediaType.APPLICATION_JSON
})
@Api(value = "Search Service", description = "Service for searching and fetching transactions and their corresponding statistics.")
public interface SearchService {
    @GET
    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    SearchResponse searchQuery(@Authenticated(scopes = {
            OAuth2AuthorizationScopeTypes.TRANSACTIONS_READ
    }) User user, @QueryParam("query") String queryString,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @QueryParam("sort") String sort,
            @QueryParam("order") String order);

    @POST
    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @TeamOwnership(Team.PFM)
    @ApiOperation(value = "Query transactions",
    notes = "Returns a response containing transaction and their corresponding statistics matching the query.",
    response = SearchResponse.class
    )
    SearchResponse searchQuery(@Authenticated(scopes = {
            OAuth2AuthorizationScopeTypes.TRANSACTIONS_READ
    }) User user, @ApiParam(value = "The search query.", required = true) SearchQuery request);
}
