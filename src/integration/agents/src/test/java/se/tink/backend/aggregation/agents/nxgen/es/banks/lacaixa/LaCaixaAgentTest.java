package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysAgentTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class LaCaixaAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
    }

    private AgentIntegrationTest createAgentTest() {
        return new AgentIntegrationTest.Builder("es", "es-lacaixa-password")
                .addCredentialField(
                        Field.Key.USERNAME, manager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build();
    }

    @Test
    public void testRefresh() throws Exception {
        createAgentTest().testRefresh();
    }

    @Test
    public void testDualAgentTest() throws Exception {
        RedsysAgentTest.runDualAgentTest("es-redsys-caixabank-ob", createAgentTest());
    }
}
