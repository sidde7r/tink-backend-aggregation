package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PasswordArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PsuIdArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.CredentialKeys;

@Ignore
public class FiduciaAgentTest {

    private final ArgumentManager<PsuIdArgumentEnum> psuIdManager =
            new ArgumentManager<>(PsuIdArgumentEnum.values());
    private final ArgumentManager<IbanArgumentEnum> ibanManager =
            new ArgumentManager<>(IbanArgumentEnum.values());
    private final ArgumentManager<PasswordArgumentEnum> passwordManager =
            new ArgumentManager<>(PasswordArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        psuIdManager.before();
        ibanManager.before();
        passwordManager.before();

        builder =
                new AgentIntegrationTest.Builder("de", "de-fiducia-ob")
                        .addCredentialField(
                                CredentialKeys.IBAN, ibanManager.get(IbanArgumentEnum.IBAN))
                        .addCredentialField(
                                CredentialKeys.PSU_ID, psuIdManager.get(PsuIdArgumentEnum.PSU_ID))
                        .addCredentialField(
                                CredentialKeys.PASSWORD,
                                passwordManager.get(PasswordArgumentEnum.PASSWORD))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
