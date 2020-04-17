package se.tink.backend.aggregation.agents.banks.lansforsakringar;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class LansforsakringarAgentTest {
    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<ArgumentManager.ToAccountFromAccountArgumentEnum> accountManager =
            new ArgumentManager<>(ArgumentManager.ToAccountFromAccountArgumentEnum.values());

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
        new AgentIntegrationTest.Builder("se", "lansforsakringar-bankid")
                .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }

    @Test
    public void testPayment() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE,
                        accountManager.get(
                                ArgumentManager.ToAccountFromAccountArgumentEnum.FROM_ACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE_BG,
                        accountManager.get(
                                ArgumentManager.ToAccountFromAccountArgumentEnum.TO_ACCOUNT)));
        transfer.setAmount(Amount.inSEK(2d));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(null);
        transfer.setDestinationMessage("Destination message");

        new AgentIntegrationTest.Builder("se", "lansforsakringar-bankid")
                .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testBankTransfer(transfer);
    }
}
