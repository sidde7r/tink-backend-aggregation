package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.payment.rpc.Beneficiary;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

public class CaisseEpargneAgentTest {

    private ArgumentManager<UsernamePasswordArgumentEnum> usernamePasswordArgumentManager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void before() {
        usernamePasswordArgumentManager.before();
    }

    @AfterClass
    public static void after() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginRefresh() throws Exception {

        new AgentIntegrationTest.Builder("fr", "fr-caisseepargneiledefrance-password")
                .addCredentialField(
                        Field.Key.USERNAME,
                        usernamePasswordArgumentManager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD,
                        usernamePasswordArgumentManager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }

    @Test
    public void testCreateBeneficiary() throws Exception {
        final String redCrossIban = "CH6200240240C05735300";
        new AgentIntegrationTest.Builder("fr", "fr-caisseepargneiledefrance-password")
                .addCredentialField(
                        Field.Key.USERNAME,
                        usernamePasswordArgumentManager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD,
                        usernamePasswordArgumentManager.get(UsernamePasswordArgumentEnum.PASSWORD))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testCreateBeneficiary(
                        CreateBeneficiary.builder()
                                .beneficiary(
                                        Beneficiary.builder()
                                                .name("Tink RED CROSS")
                                                .accountNumber(redCrossIban)
                                                .accountNumberType(AccountIdentifierType.IBAN)
                                                .build())
                                .build());
    }
}
