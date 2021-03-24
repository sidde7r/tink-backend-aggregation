package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.manual;

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
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

/** Careful with this test! Actual transfers made! */
public class HandelsbankenSEPaymentTest {

    final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("SE", "handelsbanken-bankid")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false)
                    .expectLoggedIn(false);

    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<ToAccountFromAccountArgumentEnum> toFromManager =
            new ArgumentManager<>(ToAccountFromAccountArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        toFromManager.before();
        ssnManager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testTransferFuture() throws Exception {
        Transfer transfer = createTransfer(true);
        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .build()
                .testBankTransfer(transfer);
    }

    @Test
    public void testTransferToday() throws Exception {
        Transfer transfer = createTransfer(false);
        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .build()
                .testBankTransfer(transfer);
    }

    @Test
    public void testBgPaymentFuture() throws Exception {
        Transfer transfer = createPayment(true);
        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .build()
                .testBankTransfer(transfer);
    }

    @Test
    public void testBgPaymentToday() throws Exception {
        Transfer transfer = createPayment(false);
        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .build()
                .testBankTransfer(transfer);
    }

    private Transfer createPayment(boolean futurePayment) {
        Transfer transfer = createBaseTransfer(futurePayment);
        transfer.setType(TransferType.PAYMENT);
        return transfer;
    }

    private Transfer createTransfer(boolean futurePayment) {
        Transfer transfer = createBaseTransfer(futurePayment);
        transfer.setType(TransferType.BANK_TRANSFER);
        return transfer;
    }

    private Transfer createBaseTransfer(boolean futurePayment) {
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(
                        AccountIdentifierType.SE,
                        toFromManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(
                        AccountIdentifierType.SE_BG,
                        toFromManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT)));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1d));
        if (futurePayment) {
            transfer.setDueDate(getDueDateFuture());
        } else {
            transfer.setDueDate(getDueDateToday());
        }
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(null);
        remittanceInformation.setValue("Reference");
        transfer.setRemittanceInformation(remittanceInformation);
        return transfer;
    }

    private Date getDueDateFuture() {
        return Date.from(
                LocalDate.now().atStartOfDay().plusDays(5).atZone(ZoneId.of("CET")).toInstant());
    }

    private Date getDueDateToday() {
        return Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.of("CET")).toInstant());
    }
}
