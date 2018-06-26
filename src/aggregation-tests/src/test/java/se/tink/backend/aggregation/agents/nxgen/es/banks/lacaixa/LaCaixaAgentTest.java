package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

//@Ignore
public class LaCaixaAgentTest {

    private final String TEST_USERNAME = "CCCCCCCC";
    private final String TEST_PASSWORD = "NNNNNN";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup(){
        builder = new AgentIntegrationTest.Builder("es", "es-lacaixa-password")
                .setCredentialType(CredentialsTypes.PASSWORD)
                .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception{

        builder.build().testRefresh();
    }
}
