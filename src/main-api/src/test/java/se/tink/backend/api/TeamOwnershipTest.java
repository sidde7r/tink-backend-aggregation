package se.tink.backend.api;

import org.junit.Test;
import se.tink.api.annotations.ApiTeamOwnershipChecker;

public class TeamOwnershipTest {
    @Test
    public void testAllApiEndpointsHasATeamAssigned() {
        new ApiTeamOwnershipChecker("se.tink.backend.api").check();
    }
}
