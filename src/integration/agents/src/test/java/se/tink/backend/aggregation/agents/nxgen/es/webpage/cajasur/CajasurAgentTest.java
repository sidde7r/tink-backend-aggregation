package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class CajasurAgentTest {

    private final ArgumentManager<ArgumentManager.UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.UsernamePasswordArgumentEnum.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
    }

    private AgentIntegrationTest createAgentTest() {
        return new AgentIntegrationTest.Builder("es", "es-cajasur-webpage")
                .addCredentialField(
                        Field.Key.USERNAME,
                        manager.get(ArgumentManager.UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD,
                        manager.get(ArgumentManager.UsernamePasswordArgumentEnum.PASSWORD))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .doLogout(true)
                .setFinancialInstitutionId("d3a226b4792411ec90d60242ac120003")
                .setAppId("tink")
                .build();
    }

    @Test
    public void testRefresh() throws Exception {
        createAgentTest().testRefresh();
    }
}
