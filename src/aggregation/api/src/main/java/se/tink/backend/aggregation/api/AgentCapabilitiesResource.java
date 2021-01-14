package se.tink.backend.aggregation.api;

import static se.tink.api.annotations.Team.AGGREGATION_MARKETS;
import static se.tink.api.annotations.Team.CORE_AGGREGATION;

import java.util.Map;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.TeamOwnership;

@Path("/agent-capabilities")
@Produces(MediaType.APPLICATION_JSON)
public interface AgentCapabilitiesResource {

    @GET
    @TeamOwnership(AGGREGATION_MARKETS)
    Map<String, Set<String>> getAgentCapabilities();

    @GET
    @Path("/payments")
    @TeamOwnership(CORE_AGGREGATION)
    Map<String, Map<String, Set<String>>> getAgentPisCapabilities();
}
