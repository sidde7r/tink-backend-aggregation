package se.tink.backend.aggregation.api;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.aggregation.rpc.entities.ProviderConfiguration;

@Path("/providers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProviderConfigurationService {
    @Path("/list")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    List<ProviderConfiguration> list();

    @Path("/{market}/list")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    List<ProviderConfiguration> listByMarket(@PathParam("market") String market);

    @Path("/{providerName}")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    ProviderConfiguration getProviderByName(@PathParam("providerName") String providerName);
}
