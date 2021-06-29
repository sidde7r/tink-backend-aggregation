package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.agent;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.UserAvailability;

public class ConsorsbankAgentTest {
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(false);
        builder =
                new AgentIntegrationTest.Builder("de", "de-consorsbank-ob")
                        .loadCredentialsBefore(false)
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(true)
                        // .setUserAvailability(userAvailability)
                        .setAppId("tink")
                        .setFinancialInstitutionId("consorsbank");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
