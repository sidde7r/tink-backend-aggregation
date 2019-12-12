package se.tink.backend.aggregation.agents.nxgen.fr.banks.cic;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class CicBankAgentNewTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void before() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private static AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("fr", "fr-cic-password")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA);
    }

    @Test
    public void testCicbankFr() throws Exception {
        builder()
                .addCredentialField(
                        Field.Key.USERNAME, manager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .build()
                .testRefresh();
    }
}
