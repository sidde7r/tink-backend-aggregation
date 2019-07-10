package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import java.util.Calendar;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaSEAgentTest {
    private AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "se-nordea-bankid")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false)
                    .doLogout(true);

    private enum Arg {
        USERNAME, // 12 digit SSN
        FROMACCOUNT,
        TOACCOUNT,
        TOACCOUNTSSN, // 10 digit SSN account
        BGACCOUNT,
        OCR,
        EINVOICEBGACCOUNT,
        EINVOICEOCR,
        EINVOICEID,
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setup() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }

    @Test
    public void testTransfer() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(Type.SE, manager.get(Arg.FROMACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(AccountIdentifier.Type.SE, manager.get(Arg.TOACCOUNT)));
        transfer.setAmount(Amount.inSEK(1d));
        transfer.setType(TransferType.BANK_TRANSFER);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        transfer.setDueDate(c.getTime());
        transfer.setDestinationMessage("Test msg");

        builder.addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .build()
                .testBankTransfer(transfer);
    }

    @Test
    public void testTransferToNordeaSSN() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(Type.SE, manager.get(Arg.FROMACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(Type.SE_NDA_SSN, manager.get(Arg.TOACCOUNTSSN)));
        transfer.setAmount(Amount.inSEK(1d));
        transfer.setType(TransferType.BANK_TRANSFER);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        transfer.setDueDate(c.getTime());
        transfer.setDestinationMessage("Test dest msg");

        builder.addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .build()
                .testBankTransfer(transfer);
    }

    @Test
    public void testPayment() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(AccountIdentifier.Type.SE, manager.get(Arg.FROMACCOUNT)));
        transfer.setDestination(AccountIdentifier.create(Type.SE_BG, manager.get(Arg.BGACCOUNT)));
        transfer.setAmount(Amount.inSEK(2d));
        transfer.setType(TransferType.PAYMENT);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        transfer.setDueDate(c.getTime());
        transfer.setDestinationMessage(manager.get(Arg.OCR));

        builder.addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .build()
                .testBankTransfer(transfer);
    }

    @Test
    public void testApproveEInvoice() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(AccountIdentifier.Type.SE, manager.get(Arg.FROMACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(Type.SE_BG, manager.get(Arg.EINVOICEBGACCOUNT)));
        transfer.setAmount(Amount.inSEK(2d));
        transfer.setType(TransferType.EINVOICE);
        Calendar c = Calendar.getInstance();
        transfer.setDueDate(c.getTime());
        transfer.setDestinationMessage(manager.get(Arg.EINVOICEOCR));
        transfer.addPayload(TransferPayloadType.PROVIDER_UNIQUE_ID, manager.get(Arg.EINVOICEID));

        builder.addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .build()
                .testUpdateTransfer(transfer);
    }
}
