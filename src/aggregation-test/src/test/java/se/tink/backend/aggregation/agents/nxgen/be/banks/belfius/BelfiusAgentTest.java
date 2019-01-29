package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;

@Ignore
public class BelfiusAgentTest {
    private final ArgumentHelper helper = new ArgumentHelper("tink.username", "tink.password");

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("be", "be-belfius-cardreader")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(true);

    @Before
    public void before() {
       helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentHelper.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
       buildWithCredentials().testRefresh();
    }


    private AgentIntegrationTest buildWithCredentials() {
        return builder.addCredentialField(Field.Key.USERNAME, helper.get("tink.username"))
                .addCredentialField(Field.Key.PASSWORD, helper.get("tink.password"))
                .build();
    }
}
