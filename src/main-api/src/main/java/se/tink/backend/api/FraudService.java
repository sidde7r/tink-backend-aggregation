package se.tink.backend.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.User;
import se.tink.backend.rpc.FraudActivationRequest;
import se.tink.backend.rpc.FraudActivationResponse;
import se.tink.backend.rpc.FraudChangeStatusRequest;
import se.tink.backend.rpc.FraudDetailsHelpResponse;
import se.tink.backend.rpc.FraudDetailsListResponse;
import se.tink.backend.rpc.FraudItemsResponse;
import se.tink.backend.rpc.TinkMediaType;

@Path("/api/v1/fraud")
@Consumes({
    MediaType.APPLICATION_JSON
})
@Produces({
    MediaType.APPLICATION_JSON
})
public interface FraudService {

    @GET
    @Path("/details/{id}")
    @TeamOwnership(Team.PFM)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    public FraudDetailsListResponse details(@Authenticated User user, @PathParam("id") String id);
    
    @GET
    @Path("/list")
    @TeamOwnership(Team.PFM)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    public FraudItemsResponse list(@Authenticated User user);
    
    @POST
    @Path("/activation")
    @TeamOwnership(Team.PFM)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    public FraudActivationResponse activation(@Authenticated AuthenticatedUser authenticatedUser, FraudActivationRequest request);
    
    @POST
    @Path("/details/status")
    @TeamOwnership(Team.PFM)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    public FraudDetailsListResponse status(@Authenticated User user, FraudChangeStatusRequest request);
    
    @GET
    @Path("/details/help/{type}")
    @TeamOwnership(Team.PFM)
    @Produces({MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF})
    public FraudDetailsHelpResponse help(@Authenticated User user, @PathParam("type") FraudDetailsContentType type);

    @GET
    @Path("/details/{id}/extended-information")
    @TeamOwnership(Team.PFM)
    @Produces({MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF})
    public FraudDetailsHelpResponse extendedInformation(@Authenticated User user, @PathParam("id") String detailsId);
}
