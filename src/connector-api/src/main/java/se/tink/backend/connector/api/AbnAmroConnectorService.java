package se.tink.backend.connector.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.connector.rpc.abnamro.TransactionAccountContainer;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;
import se.tink.libraries.http.annotations.auth.AllowClient;

@Path("/connector/abnamro")
@Produces({
    MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
})
@Consumes({
    MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
})
@AllowClient("ABN_AMRO")
public interface AbnAmroConnectorService {

    @GET
    @Path("ping")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @AllowAnonymous
    String ping();

    @POST
    @Path("transactions")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
        void transactions(TransactionAccountContainer container);

}
