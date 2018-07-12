package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class FinTsAgentTest {

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("", "")
                .addCredentialField(Field.Key.USERNAME, "")
                .addCredentialField(Field.Key.PASSWORD, "")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();

    }
}
