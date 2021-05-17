package se.tink.backend.aggregation.agents.legacy.banks.seb.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;

public class SebApiAgentIntegrationTest {
    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "seb-bankid")
                    .expectLoggedIn(false)
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(true);
    private final ArgumentManager<UsernameArgumentEnum> helper =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, helper.get(UsernameArgumentEnum.USERNAME))
                .build()
                .testRefresh();
    }
}
