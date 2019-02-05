package se.tink.backend.aggregation.agents.nxgen.es.creditcards.amex;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class AmericanExpressESAgentTest {

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder(AmericanExpressESConstants.MARKET,
                AmericanExpressESConstants.PROVIDER_NAME)
                .addCredentialField(Field.Key.USERNAME, "")
                .addCredentialField(Field.Key.PASSWORD, "")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder().build().testRefresh();
    }

}
