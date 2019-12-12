package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class FidorAgentTest {

    private final ArgumentManager<UsernamePasswordArgumentEnum> helper;
    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("de", "de-hypovereinsbank-password")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false);

    public FidorAgentTest() {
        helper = new ArgumentManager<>(UsernamePasswordArgumentEnum.values());
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void before() {
        helper.before();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        builder.addCredentialField(
                        Field.Key.USERNAME, helper.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, helper.get(UsernamePasswordArgumentEnum.PASSWORD))
                .build()
                .testRefresh();
    }
}
