package se.tink.backend.aggregation.api;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.core.ProviderConfiguration;

@Path("/providers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProviderService {
    @Path("/list")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    List<ProviderConfiguration> list(@DefaultValue("en") @QueryParam("lang") String lang);

    @Path("/{market}/list")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    List<ProviderConfiguration> listByMarket(@DefaultValue("en") @QueryParam("lang") String lang, @PathParam("market") String market);

    @Path("/{providerName}")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    ProviderConfiguration getProviderByName(@DefaultValue("en") @QueryParam("lang") String lang, @PathParam("providerName") String providerName);
}
