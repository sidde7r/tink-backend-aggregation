package se.tink.backend.aggregation.agents.nxgen.se.openbanking.paypal;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class PayPalAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("se", "se-paypal-ob")
                        .setAppId("tink")
                        .setFinancialInstitutionId("paypal")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
