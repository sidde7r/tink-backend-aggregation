package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class CreditAgricoleBaseIntegrationTest {

    private final String integrationNameFromProviders;
    private AgentIntegrationTest.Builder builder;

    public CreditAgricoleBaseIntegrationTest(String integrationNameFromProviders) {
        this.integrationNameFromProviders = integrationNameFromProviders;
    }

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", integrationNameFromProviders)
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
