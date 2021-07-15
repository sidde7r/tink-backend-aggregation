package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.santander;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.UserAvailability;

public class SantanderAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        UserAvailability ua = new UserAvailability();
        ua.setUserPresent(false);
        builder =
                new AgentIntegrationTest.Builder("pl", "pl-santander-ob")
                        .expectLoggedIn(false)
                        // TODO
                        .setFinancialInstitutionId("97d06fede8c7400e842017df63274c51")
                        .setAppId("tink")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .setUserAvailability(ua);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
