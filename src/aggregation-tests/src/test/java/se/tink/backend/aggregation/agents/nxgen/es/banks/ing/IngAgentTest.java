package se.tink.backend.aggregation.agents.nxgen.es.banks.ing;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class IngAgentTest {

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder(IngConstants.MARKET, IngConstants.PROVIDER_NAME)
                .addCredentialField(Field.Key.USERNAME, "")
                .addCredentialField(IngConstants.DATE_OF_BIRTH, "") // dd/MM/yyyy
                .addCredentialField(Field.Key.PASSWORD, "") // six digits
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder().build().testRefresh();
    }
}
