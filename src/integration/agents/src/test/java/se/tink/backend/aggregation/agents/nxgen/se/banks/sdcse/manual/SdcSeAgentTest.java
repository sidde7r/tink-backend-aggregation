package se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SdcSeAgentTest {
    private final String MARKET = "se";
    private final String PROVIDER_NAME = "sparbankensyd-bankid";
    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder(MARKET, PROVIDER_NAME)
                .addCredentialField(Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .withoutRefreshableItems()
                .build()
                .testRefresh();
    }

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder(MARKET, PROVIDER_NAME)
                .addCredentialField(Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
