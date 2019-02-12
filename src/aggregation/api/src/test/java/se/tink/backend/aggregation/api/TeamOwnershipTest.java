package se.tink.backend.aggregation.api;

import org.junit.Test;
import se.tink.libraries.api.annotations.ApiTeamOwnershipChecker;

public class TeamOwnershipTest {

    @Test
    public void testAllApiEndpointsHasATeamAssigned() {
        new ApiTeamOwnershipChecker("se.tink.backend.aggregation").check();
    }

}
