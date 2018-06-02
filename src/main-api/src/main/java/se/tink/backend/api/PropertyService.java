package se.tink.backend.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.core.property.ListPropertiesResponse;
import se.tink.backend.core.property.PropertyEventsResponse;
import se.tink.backend.core.property.PropertyResponse;
import se.tink.backend.core.property.UpdatePropertyRequest;
import se.tink.backend.rpc.TinkMediaType;

@Path("/api/v1/properties")
@Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
@Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
public interface PropertyService {
    @GET
    @TeamOwnership(Team.PFM)
    ListPropertiesResponse list(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.PROPERTIES_READ }) AuthenticatedUser authenticatedUser);

    @GET
    @Path("/{id}")
    @TeamOwnership(Team.PFM)
    PropertyResponse get(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.PROPERTIES_READ }) AuthenticatedUser authenticatedUser,
            @PathParam("id") String propertyId);

    @PUT
    @Path("/{id}")
    @TeamOwnership(Team.PFM)
    PropertyResponse update(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.PROPERTIES_WRITE }) AuthenticatedUser authenticatedUser,
            @PathParam("id") String propertyId, UpdatePropertyRequest request);

    @DELETE
    @Path("/{id}/valuation")
    @TeamOwnership(Team.PFM)
    PropertyResponse deleteValuation(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.PROPERTIES_WRITE }) AuthenticatedUser authenticatedUser,
            @PathParam("id") String propertyId);

    @GET
    @Path("/events")
    @TeamOwnership(Team.PFM)
    PropertyEventsResponse getEvents(
        @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.PROPERTIES_READ }) AuthenticatedUser authenticatedUser);
}
