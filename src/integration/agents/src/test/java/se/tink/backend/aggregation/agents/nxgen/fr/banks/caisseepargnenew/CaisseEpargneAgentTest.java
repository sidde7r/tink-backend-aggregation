package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class CaisseEpargneAgentTest {

    private ArgumentManager<UsernamePasswordArgumentEnum> usernamePasswordArgumentManager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void before() {
        usernamePasswordArgumentManager.before();
    }

    @AfterClass
    public static void after() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginRefresh() throws Exception {

        new AgentIntegrationTest.Builder("fr", "fr-caisseepargne-password-new")
                .addCredentialField(
                        Field.Key.USERNAME,
                        usernamePasswordArgumentManager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD,
                        usernamePasswordArgumentManager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
