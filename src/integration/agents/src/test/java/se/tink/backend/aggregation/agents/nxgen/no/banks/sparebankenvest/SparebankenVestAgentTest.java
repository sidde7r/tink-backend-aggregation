package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class SparebankenVestAgentTest {
    private static final String USERNAME = "";
    private static final String ACTIVATION_CODE = "";

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("no", "no-sparebankenvest-activationcode")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, ACTIVATION_CODE)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder().build().testRefresh();
    }
}
