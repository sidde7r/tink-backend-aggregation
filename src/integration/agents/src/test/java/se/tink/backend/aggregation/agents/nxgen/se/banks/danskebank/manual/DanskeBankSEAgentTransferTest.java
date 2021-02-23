package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.manual;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ToAccountFromAccountArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public final class DanskeBankSEAgentTransferTest {
    final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("SE", "se-danskebank-bankid")
                    .loadCredentialsBefore(true)
                    .saveCredentialsAfter(true)
                    .expectLoggedIn(true);

    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<ToAccountFromAccountArgumentEnum> toFromManager =
            new ArgumentManager<>(ToAccountFromAccountArgumentEnum.values());

    @Before
    public void setUp() {
        toFromManager.before();
        ssnManager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testFutureTransfer() throws Exception {
        Transfer transfer = createTransfer(true);
        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .build()
                .testBankTransfer(transfer);
    }

    @Test
    public void testTransferNoDate() throws Exception {
        Transfer transfer = createTransfer(false);
        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .build()
                .testBankTransfer(transfer);
    }

    @Test
    public void testFuturePayment() throws Exception {
        Transfer payment = createPayment(true);
        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .build()
                .testBankTransfer(payment);
    }

    @Test
    public void testPaymentNoDate() throws Exception {
        Transfer payment = createPayment(false);
        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .build()
                .testBankTransfer(payment);
    }

    private Transfer createTransfer(boolean futurePayment) {
        Transfer transfer = createBaseTransfer(futurePayment);
        transfer.setType(TransferType.BANK_TRANSFER);
        return transfer;
    }

    private Transfer createPayment(boolean futurePayment) {
        Transfer transfer = createBasePayment(futurePayment);
        transfer.setType(TransferType.PAYMENT);
        return transfer;
    }

    private Transfer createBaseTransfer(boolean futurePayment) {
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(
                        Type.SE, toFromManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(
                        Type.SE, toFromManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT)));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1d));
        if (futurePayment) {
            transfer.setDueDate(getDueDateFuture());
        } else {
            transfer.setDueDate(null);
        }
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Reference");
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setSourceMessage("srcmsg");
        return transfer;
    }

    private Transfer createBasePayment(boolean futurePayment) {
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(
                        Type.SE, toFromManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(
                        Type.SE_BG,
                        toFromManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT)));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1d));
        if (futurePayment) {
            transfer.setDueDate(getDueDateFuture());
        } else {
            transfer.setDueDate(null);
        }
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("Reference");
        transfer.setRemittanceInformation(remittanceInformation);
        return transfer;
    }

    private Date getDueDateFuture() {
        return Date.from(
                LocalDate.now().atStartOfDay().plusDays(5).atZone(ZoneId.of("CET")).toInstant());
    }
}
