package se.tink.backend.aggregation.api;

import static se.tink.api.annotations.Team.CORE_AGGREGATION;

import java.util.Map;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.TeamOwnership;
import se.tink.libraries.authentication_options.AuthenticationOptionDto;

@Path("/authentication-options")
@Produces(MediaType.APPLICATION_JSON)
public interface AuthenticationOptionsService {

    @GET
    @TeamOwnership(CORE_AGGREGATION)
    Map<String, Set<AuthenticationOptionDto>> getAgentsAuthenticationOptions();
}
