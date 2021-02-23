package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.manual;

import java.util.Calendar;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaSEAgentTest {
    private AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "nordea-bankid")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false)
                    .doLogout(true);

    private enum Arg implements ArgumentManagerEnum {
        USERNAME, // 12 digit SSN
        FROMACCOUNT,
        TOACCOUNT,
        TOACCOUNTSSN, // 10 digit SSN account
        BGACCOUNT,
        OCR,
        EINVOICEBGACCOUNT,
        EINVOICEOCR,
        EINVOICEID;

        private final boolean optional;

        Arg(boolean optional) {
            this.optional = optional;
        }

        Arg() {
            this.optional = false;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }
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

    @Rule public ExpectedException exception = ExpectedException.none();

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
        transfer.setAmount(ExactCurrencyAmount.inSEK(1d));
        transfer.setType(TransferType.BANK_TRANSFER);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        transfer.setDueDate(c.getTime());
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Test message");
        transfer.setRemittanceInformation(remittanceInformation);

        builder.addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .build()
                .testBankTransfer(transfer);
    }

    @Test
    public void testTransferWithAmountLessThan1SEK() throws Exception {
        exception.expect(TransferExecutionException.class);
        exception.expectMessage(
                TransferExecutionException.EndUserMessage.INVALID_MINIMUM_AMOUNT.getKey().get());
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(Type.SE, manager.get(Arg.FROMACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(AccountIdentifier.Type.SE, manager.get(Arg.TOACCOUNT)));
        transfer.setAmount(ExactCurrencyAmount.inSEK(0.99d));
        transfer.setType(TransferType.BANK_TRANSFER);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        transfer.setDueDate(c.getTime());
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Test message");
        transfer.setRemittanceInformation(remittanceInformation);

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
        transfer.setAmount(ExactCurrencyAmount.inSEK(1d));
        transfer.setType(TransferType.BANK_TRANSFER);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        transfer.setDueDate(c.getTime());
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Test message"); // minimum length is 12
        transfer.setRemittanceInformation(remittanceInformation);

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
        transfer.setAmount(ExactCurrencyAmount.inSEK(2d));
        transfer.setType(TransferType.PAYMENT);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        transfer.setDueDate(c.getTime());
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue(manager.get(Arg.OCR));
        transfer.setRemittanceInformation(remittanceInformation);

        builder.addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .build()
                .testBankTransfer(transfer);
    }
}
