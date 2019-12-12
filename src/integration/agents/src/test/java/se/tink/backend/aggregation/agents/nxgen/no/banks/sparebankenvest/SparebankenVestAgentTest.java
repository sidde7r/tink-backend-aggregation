package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class SparebankenVestAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testRegisterAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("no", "no-sparebankenvest-activationcode")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                manager.get(UsernamePasswordArgumentEnum.USERNAME))
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true)
                        .expectLoggedIn(false);

        builder.build().testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
