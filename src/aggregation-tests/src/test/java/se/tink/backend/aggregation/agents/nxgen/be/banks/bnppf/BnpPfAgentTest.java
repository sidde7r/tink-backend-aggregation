package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class BnpPfAgentTest {

    private static final String ACCESS_TOKEN = "";

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("be", "be-bnppf-jwt")
            .addCredentialField(Field.Key.ACCESS_TOKEN, ACCESS_TOKEN)
            .loadCredentialsBefore(false)
            .saveCredentialsAfter(false);

    @Test
    public void testRefresh() throws Exception {
        builder.build()
                .testRefresh();
    }
}
