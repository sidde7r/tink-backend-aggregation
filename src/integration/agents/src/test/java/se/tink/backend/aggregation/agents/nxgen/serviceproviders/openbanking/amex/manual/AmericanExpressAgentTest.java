package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.manual;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class AmericanExpressAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        String market = "se";
        String provider = market.concat("-amex-ob");

        builder =
                new AgentIntegrationTest.Builder(market, provider)
                        .setFinancialInstitutionId("amex")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
