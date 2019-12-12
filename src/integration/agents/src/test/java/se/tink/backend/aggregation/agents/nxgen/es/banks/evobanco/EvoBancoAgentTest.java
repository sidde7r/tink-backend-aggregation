package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class EvoBancoAgentTest {

    private final ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("es", "es-evobanco-password")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                manager.get(UsernamePasswordArgumentEnum.USERNAME))
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {

        builder.build().testRefresh();
    }
}
