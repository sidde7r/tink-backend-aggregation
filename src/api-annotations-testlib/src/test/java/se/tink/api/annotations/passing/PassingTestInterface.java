package se.tink.api.annotations.passing;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;

public interface PassingTestInterface {
    @GET
    @Path("/some/random/path")
    @TeamOwnership(Team.INTEGRATION)
    void someApiMethod();
}
