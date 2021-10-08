package se.tink.backend.aggregation.agents.nxgen.it.openbanking.unicredit.agent;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.tools.UserAvailabilityBuilder;

public class UnicreditAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("it", "it-unicredit-oauth2")
                        .setFinancialInstitutionId("unicredit-it")
                        .setAppId("tink")
                        .setUserAvailability(UserAvailabilityBuilder.availableUser())
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
