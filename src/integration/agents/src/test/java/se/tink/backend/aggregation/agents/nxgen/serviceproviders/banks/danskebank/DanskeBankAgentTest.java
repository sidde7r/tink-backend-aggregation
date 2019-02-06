package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class DanskeBankAgentTest {

    private final String FI_MARKET = "fi";
    private final String FI_PROVIDER_NAME = "fi-danskebank-codecard";

    private final String DK_MARKET = "dk";
    private final String DK_PROVIDER_NAME = "dk-danskebank-servicecode";

    private final String TEST_USERNAME = "NNNN";
    private final String TEST_PASSWORD = "NNNN";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup(){
        builder = new AgentIntegrationTest.Builder(DK_MARKET, DK_PROVIDER_NAME)
                .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception{

        builder.build().testRefresh();
    }
}
