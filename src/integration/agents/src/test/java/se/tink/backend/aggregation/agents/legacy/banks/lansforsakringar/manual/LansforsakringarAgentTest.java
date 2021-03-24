package se.tink.backend.aggregation.agents.banks.lansforsakringar.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ToAccountFromAccountArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class LansforsakringarAgentTest {
    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<ToAccountFromAccountArgumentEnum> accountManager =
            new ArgumentManager<>(ToAccountFromAccountArgumentEnum.values());

    private AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "lansforsakringar-bankid")
                    .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false);

    @Before
    public void setUp() throws Exception {
        ssnManager.before();
        accountManager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }

    @Test
    public void testPayment() throws Exception {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Destination message");
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(
                        AccountIdentifierType.SE,
                        accountManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(
                        AccountIdentifierType.SE_BG,
                        accountManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT)));
        transfer.setAmount(ExactCurrencyAmount.inSEK(2d));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(null);
        transfer.setRemittanceInformation(remittanceInformation);

        builder.build().testBankTransfer(transfer);
    }
}
