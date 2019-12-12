package se.tink.backend.aggregation.agents.nxgen.fr.banks.monabanq;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;

@Ignore
public class MonaBanqDualAgentIntegrationTest {

    private final ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void setup() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void dualTest() throws Exception {

        DualAgentIntegrationTest.of(
                        new AgentIntegrationTest.Builder("fr", "fr-monabanq-oauth2")
                                .expectLoggedIn(false)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .build(),
                        new AgentIntegrationTest.Builder("fr", "fr-monabanq-password")
                                .addCredentialField(
                                        Field.Key.USERNAME,
                                        manager.get(
                                                // Card Number
                                                UsernamePasswordArgumentEnum.USERNAME))
                                .addCredentialField(
                                        Field.Key.PASSWORD,
                                        manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                                .expectLoggedIn(false)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .build())
                .testAndCompare();
    }
}
