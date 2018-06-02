package se.tink.backend.system.api;

import org.junit.Test;
import se.tink.api.annotations.ApiTeamOwnershipChecker;

public class TeamOwnershipTest {
    @Test
    public void testAllApiEndpointsHasATeamAssigned() {
        new ApiTeamOwnershipChecker("se.tink.backend.system").check();
    }
}
