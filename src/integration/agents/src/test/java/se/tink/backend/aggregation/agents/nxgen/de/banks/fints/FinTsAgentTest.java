package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;

public class FinTsAgentTest {

    ArgumentHelper helper =
            new ArgumentHelper("tink.username", "tink.password", "tink.market", "tink.provider");

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentHelper.afterClass();
    }

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder(helper.get("tink.market"), helper.get("tink.provider"))
                .addCredentialField(Field.Key.USERNAME, helper.get("tink.username"))
                .addCredentialField(Field.Key.PASSWORD, helper.get("tink.password"))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
