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
import se.tink.backend.rpc.AuthenticatedLoginResponse;
import se.tink.backend.rpc.AuthenticatedRegisterRequest;
import se.tink.backend.rpc.AuthenticatedRegisterResponse;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.backend.rpc.auth.EmailAndPasswordAuthenticationRequest;
import se.tink.backend.rpc.auth.bankid.CollectBankIdAuthenticationResponse;
import se.tink.backend.rpc.auth.bankid.InitiateBankIdAuthenticationRequest;
import se.tink.backend.rpc.auth.bankid.InitiateBankIdAuthenticationResponse;

@Path("/api/v1/authentication")
public interface AuthenticationService {
    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @POST
    @Path("bankid")
    @TeamOwnership(Team.PFM)
    InitiateBankIdAuthenticationResponse initiateBankIdAuthentication(InitiateBankIdAuthenticationRequest request);

    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @GET
    @Path("bankid/{authenticationToken}")
    @TeamOwnership(Team.PFM)
    CollectBankIdAuthenticationResponse collectBankIdAuthentication(@PathParam("authenticationToken") String authenticationToken);

    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @POST
    @Path("email-password")
    @TeamOwnership(Team.PFM)
    AuthenticationResponse emailAndPasswordAuthentication(EmailAndPasswordAuthenticationRequest request);

    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @POST
    @Path("{authenticationToken}/login")
    @TeamOwnership(Team.PFM)
    AuthenticatedLoginResponse login(@PathParam("authenticationToken") String authenticationToken);

    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @POST
    @Path("{authenticationToken}/register")
    @TeamOwnership(Team.PFM)
    AuthenticatedRegisterResponse register(@PathParam("authenticationToken") String authenticationToken,
            AuthenticatedRegisterRequest request);
}
