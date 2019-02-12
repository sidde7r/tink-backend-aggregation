package se.tink.backend.aggregation.provider.configuration.api;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.libraries.api.annotations.Team;
import se.tink.libraries.api.annotations.TeamOwnership;
import se.tink.backend.aggregation.provider.configuration.cluster.annotations.ClusterContext;
import se.tink.backend.aggregation.provider.configuration.cluster.identifiers.ClusterInfo;
import se.tink.backend.aggregation.provider.configuration.rpc.ProviderConfigurationDTO;

@Path("/providers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProviderService {
    @Path("/list")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    List<ProviderConfigurationDTO> list(@ClusterContext ClusterInfo clusterInfo);

    @Path("/{market}/list")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    List<ProviderConfigurationDTO> listByMarket(@PathParam("market") String market,
            @ClusterContext ClusterInfo clusterInfo);

    @Path("/{providerName}")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    ProviderConfigurationDTO getProviderByName(@PathParam("providerName") String providerName,
            @ClusterContext ClusterInfo clusterInfo);
}
