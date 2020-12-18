package se.tink.backend.aggregation.agents.nxgen.se.other.csn;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class CSNAgentTest {
    private final ArgumentManager<ArgumentManager.SsnArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.SsnArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testCSN() throws Exception {
        new AgentIntegrationTest.Builder("se", "csn-bankid")
                .addCredentialField(
                        Field.Key.USERNAME, manager.get(ArgumentManager.SsnArgumentEnum.SSN))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
