package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.agent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class IspAgentTest {

    private ArgumentManager<ArgumentManager.UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.UsernamePasswordArgumentEnum.values());

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("it", "it-isp-password")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false);

    @Before
    public void before() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        builder.addCredentialField(
                        Field.Key.USERNAME,
                        manager.get(ArgumentManager.UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD,
                        manager.get(ArgumentManager.UsernamePasswordArgumentEnum.PASSWORD))
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
