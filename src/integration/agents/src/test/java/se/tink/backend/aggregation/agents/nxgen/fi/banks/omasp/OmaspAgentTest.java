package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class OmaspAgentTest {

    private enum Arg {
        USERNAME,
        PASSWORD
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("fi", "fi-omasp-codecard")
                .addCredentialField(Key.USERNAME, manager.get(Arg.USERNAME))
                .addCredentialField(Key.PASSWORD, manager.get(Arg.PASSWORD))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .saveCredentialsAfter(true)
                .loadCredentialsBefore(true)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
