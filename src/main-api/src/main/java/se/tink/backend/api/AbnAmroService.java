package se.tink.backend.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.User;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.rpc.abnamro.AbnAmroUserLoginResponse;
import se.tink.backend.rpc.abnamro.AccountSubscriptionRequest;
import se.tink.backend.rpc.abnamro.AuthenticationRequest;
import se.tink.backend.rpc.abnamro.CustomerValidationRequest;
import se.tink.backend.rpc.abnamro.SubscriptionActivationRequest;

@Path("/api/v1/abnamro")
@Produces({
    MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
})
@Consumes({
    MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
})
public interface AbnAmroService {

    @POST
    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Path("/authenticate")
    @TeamOwnership(Team.PFM)
    public AbnAmroUserLoginResponse authenticate(AuthenticationRequest request);

    @POST
    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Path("/credentials")
    @TeamOwnership(Team.PFM)
    public Credentials credentials(@Authenticated AuthenticatedUser authenticatedUser, AccountSubscriptionRequest request);
    
    @GET
    @Path("/ping")
    @TeamOwnership(Team.PFM)
    public String ping();
    
    @POST
    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Path("/validate")
    @TeamOwnership(Team.PFM)
    @Deprecated
    public void validate(CustomerValidationRequest request);

    @POST
    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Path("/activate")
    @TeamOwnership(Team.PFM)
    public void activate(@Authenticated User user, SubscriptionActivationRequest request);
}
