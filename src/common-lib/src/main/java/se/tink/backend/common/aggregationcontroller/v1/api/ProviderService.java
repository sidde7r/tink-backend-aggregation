package se.tink.backend.common.aggregationcontroller.v1.api;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.core.Provider;

@Path("/aggregation/controller/v1/providers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProviderService {
    @Path("/list")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    List<Provider> list();

    @Path("/{market}/list")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    List<Provider> listByMarket(@PathParam("market") String market);

    @Path("/{providerName}")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    Provider getProviderByName(@PathParam("providerName") String providerName);
}
