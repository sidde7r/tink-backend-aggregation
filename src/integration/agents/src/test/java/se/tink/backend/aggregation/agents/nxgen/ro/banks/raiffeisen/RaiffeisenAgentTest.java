package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;


public class RaiffeisenAgentTest {

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("ro", "ro-raiffeisen-psd2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();

    }
}
