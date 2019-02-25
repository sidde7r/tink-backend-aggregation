package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class IngAgentTest {
    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder(IngConstants.MARKET, IngConstants.PROVIDER_NAME)
                .addCredentialField(Field.Key.USERNAME, "")
                .addCredentialField(IngConstants.DATE_OF_BIRTH, "") // ddMMyyyy
                .addCredentialField(Field.Key.PASSWORD, "") // six digits
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder().build().testRefresh();
    }
}
