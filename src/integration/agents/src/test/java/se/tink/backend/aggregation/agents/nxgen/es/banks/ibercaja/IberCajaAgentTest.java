package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysAgentTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class IberCajaAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> helper =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private AgentIntegrationTest createAgentTest() {
        return new AgentIntegrationTest.Builder("es", "es-ibercaja-password")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .addCredentialField(
                        Field.Key.USERNAME, helper.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, helper.get(UsernamePasswordArgumentEnum.PASSWORD))
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
        RedsysAgentTest.runDualAgentTest("es-redsys-ibercaja-ob", createAgentTest());
    }
}
