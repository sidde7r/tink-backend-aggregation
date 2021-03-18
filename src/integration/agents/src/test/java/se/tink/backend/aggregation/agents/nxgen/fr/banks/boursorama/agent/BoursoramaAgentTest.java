package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.agent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Beneficiary;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

public class BoursoramaAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
    }

    private static AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("fr", "fr-boursorama-password");
    }

    @Test
    public void testRefresh() throws Exception {
        builder()
                .addCredentialField(
                        Field.Key.USERNAME, manager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }

    @Test
    public void testCreateBeneficiary() throws Exception {
        final String newBeneficiaryIban = "dummyIban";
        final String newBeneficiaryName = "dummyName";
        builder()
                .addCredentialField(
                        Field.Key.USERNAME, manager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testCreateBeneficiary(
                        CreateBeneficiary.builder()
                                .beneficiary(
                                        Beneficiary.builder()
                                                .name(newBeneficiaryName)
                                                .accountNumber(newBeneficiaryIban)
                                                .accountNumberType(AccountIdentifierType.IBAN)
                                                .build())
                                .build());
    }
}
