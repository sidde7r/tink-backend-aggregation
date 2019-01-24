package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class SwedbankSEAgentTest {
    @Test
    public void testRegisterAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder = new AgentIntegrationTest.Builder("se", "swedbank-bankid")
                .addCredentialField(Field.Key.USERNAME, "yyyyMMddnnnn")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true);

        builder.build()
                .testRefresh();
    }
}
