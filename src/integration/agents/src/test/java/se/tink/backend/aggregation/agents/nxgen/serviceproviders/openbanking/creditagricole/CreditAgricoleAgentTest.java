package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;


public class CreditAgricoleAgentTest {

    @Test
    public void testAuthenticate() throws Exception {
        new AgentIntegrationTest.Builder("fr", "fr-creditagricole-oauth1").build().testRefresh();
    }
}
