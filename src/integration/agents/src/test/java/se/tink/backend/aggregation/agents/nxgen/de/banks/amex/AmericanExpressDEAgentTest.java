package se.tink.backend.aggregation.agents.nxgen.de.banks.amex;

import static se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum.PASSWORD;
import static se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum.USERNAME;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class AmericanExpressDEAgentTest {

    private final ArgumentManager<UsernamePasswordArgumentEnum> helper =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("de", "de-americanexpress-password")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false);

    @Before
    public void before() {
        helper.before();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, helper.get(USERNAME))
                .addCredentialField(Field.Key.PASSWORD, helper.get(PASSWORD))
                .build()
                .testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
