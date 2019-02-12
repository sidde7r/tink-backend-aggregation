package se.tink.libraries.api.annotations.passing;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import se.tink.libraries.api.annotations.Team;
import se.tink.libraries.api.annotations.TeamOwnership;

public interface PassingTestInterface {
    @GET
    @Path("/some/random/path")
    @TeamOwnership(Team.INTEGRATION)
    void someApiMethod();
}
