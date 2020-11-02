package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SpankkiAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("fi", "fi-spankki-codecard")
                .addCredentialField(
                        Field.Key.USERNAME, manager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder().build().testRefresh();
    }

    @Test
    public void testAutoRefresh() throws Exception {
        builder().loadCredentialsBefore(true).saveCredentialsAfter(true).build().testRefresh();
    }
}
