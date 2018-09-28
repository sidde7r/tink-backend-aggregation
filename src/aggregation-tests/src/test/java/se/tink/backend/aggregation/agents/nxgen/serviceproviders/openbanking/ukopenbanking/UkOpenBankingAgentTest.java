package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class UkOpenBankingAgentTest {
    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-modelo-oauth2")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
