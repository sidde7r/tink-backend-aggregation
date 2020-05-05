package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;

public class SebDualAgentTest {

    private final ArgumentManager<SsnArgumentEnum> manager =
            new ArgumentManager<>(SsnArgumentEnum.values());

    private AgentIntegrationTest.Builder legacyTestBuilder;
    private AgentIntegrationTest.Builder nxgenTestBuilder;

    @Before
    public void before() {
        manager.before();

        legacyTestBuilder =
                new AgentIntegrationTest.Builder("se", "seb-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        nxgenTestBuilder =
                new AgentIntegrationTest.Builder("se", "se-seb-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        final DualAgentIntegrationTest test =
                DualAgentIntegrationTest.of(legacyTestBuilder.build(), nxgenTestBuilder.build());
        test.testAndCompare();
    }
}
