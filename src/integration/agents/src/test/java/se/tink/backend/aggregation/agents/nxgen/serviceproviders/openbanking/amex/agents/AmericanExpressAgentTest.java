package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.agents;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class AmericanExpressAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("uk", "uk-amex-ob")
                        .setFinancialInstitutionId("amex")
                        .setAppId("tink")
                        .doLogout(true)
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
