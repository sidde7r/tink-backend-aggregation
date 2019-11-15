package se.tink.backend.aggregation.client.provider_configuration;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.aggregation.client.provider_configuration.rpc.ProviderConfiguration;

@Path("/providers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProviderConfigurationService {
    @Path("/list")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    List<ProviderConfiguration> list(
            @HeaderParam("x-tink-cluster-name") String clusterName,
            @HeaderParam("x-tink-cluster-environment") String clusterEnvironment);

    @Path("/{market}/list")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    List<ProviderConfiguration> listByMarket(
            @HeaderParam("x-tink-cluster-name") String clusterName,
            @HeaderParam("x-tink-cluster-environment") String clusterEnvironment,
            @PathParam("market") String market);

    @Path("/{providerName}")
    @GET
    @TeamOwnership(Team.INTEGRATION)
    ProviderConfiguration getProviderByName(
            @HeaderParam("x-tink-cluster-name") String clusterName,
            @HeaderParam("x-tink-cluster-environment") String clusterEnvironment,
            @PathParam("providerName") String providerName);
}
