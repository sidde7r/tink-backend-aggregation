package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.jyske;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants;

public class JyskeAgentTest {

    private final ArgumentManager<IbanArgumentEnum> manager =
            new ArgumentManager<>(IbanArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("dk", "dk-jyskemastercarddk-ob")
                        .addCredentialField(
                                BankdataConstants.CredentialKeys.IBAN,
                                manager.get(IbanArgumentEnum.IBAN))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
