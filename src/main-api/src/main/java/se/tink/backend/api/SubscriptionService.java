package se.tink.backend.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.core.SubscriptionListResponse;

@Path("/api/v1/subscription")
public interface SubscriptionService {

    @GET
    @Path("{token}")
    @TeamOwnership(Team.PFM)
    @Produces({
            MediaType.APPLICATION_JSON
    })
    SubscriptionListResponse list(@PathParam("token") String token, @QueryParam("locale") String locale);

    @PUT
    @Path("{token}/{subscriptionType}")
    @TeamOwnership(Team.PFM)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    void subscribe(@PathParam("token") String token, @PathParam("subscriptionType") String subscriptionType);

    @DELETE
    @Path("{token}/{subscriptionType}")
    @TeamOwnership(Team.PFM)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    void unsubscribe(@PathParam("token") String token, @PathParam("subscriptionType") String subscriptionType);

}
