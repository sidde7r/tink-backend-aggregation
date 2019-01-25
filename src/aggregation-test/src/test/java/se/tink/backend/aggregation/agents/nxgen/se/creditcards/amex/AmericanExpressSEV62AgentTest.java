package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class AmericanExpressSEV62AgentTest {

    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("se", "americanexpress")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .doLogout(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
