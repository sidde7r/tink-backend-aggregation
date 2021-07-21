package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.creditagricole;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CreditAgricoleAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pl", "pl-creditagricole-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("e3ce18f5a5f84683afda4604ad121e26")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
