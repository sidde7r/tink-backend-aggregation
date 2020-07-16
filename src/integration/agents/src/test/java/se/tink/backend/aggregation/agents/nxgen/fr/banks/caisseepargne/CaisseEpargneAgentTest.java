package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.libraries.account.AccountIdentifier.Type;
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

        new AgentIntegrationTest.Builder("fr", "fr-caisseepargne-password")
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
        final String RED_CROSS = "CH6200240240C05735300";
        final String BENEFICIARY_ACCOUNT = "FR7630056005020502000363678"; // Fondation de France
        new AgentIntegrationTest.Builder("fr", "fr-caisseepargne-password")
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
                                                .accountNumber(RED_CROSS)
                                                .accountNumberType(Type.IBAN)
                                                .build())
                                .build());
    }
}
