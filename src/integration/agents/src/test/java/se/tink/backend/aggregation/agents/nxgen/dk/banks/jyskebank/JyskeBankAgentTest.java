package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PinArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class JyskeBankAgentTest {

    private final ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    private final ArgumentManager<PinArgumentEnum> pinManager =
            new ArgumentManager<>(PinArgumentEnum.values());

    @Before
    public void setup() {
        manager.before();
        pinManager.before();
    }

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("dk", "dk-jyskebank-nemid")
                .addCredentialField(
                        Field.Key.USERNAME, manager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .addCredentialField(Field.Key.ACCESS_PIN, pinManager.get(PinArgumentEnum.PIN))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("jyskebank-dk")
                .setAppId("tink")
                .build()
                .testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
