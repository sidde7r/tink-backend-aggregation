package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.manual;

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
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

/** Careful with this test! Actual transfers made! */
public class SwedbankSEPaymentTest {

    final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("SE", "swedbank-bankid")
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
    public void testTransfer() throws Exception {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("This is a test transfer");
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(
                        Type.SE, toFromManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE,
                        toFromManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT)));
        transfer.setAmount(Amount.inSEK(1d));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDueDate(getDueDate());
        transfer.setRemittanceInformation(remittanceInformation);

        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .build()
                .testBankTransfer(transfer);
    }

    @Test
    public void testBgPayment() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE,
                        toFromManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(
                        Type.SE_BG,
                        toFromManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT)));
        transfer.setAmount(Amount.inSEK(1d));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(getDueDate());

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Reference");
        transfer.setRemittanceInformation(remittanceInformation);

        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .build()
                .testBankTransfer(transfer);
    }

    private Date getDueDate() {
        return Date.from(
                LocalDate.now()
                        .atStartOfDay()
                        .plusDays(5)
                        .atZone(ZoneId.of("Europe/Stockholm"))
                        .toInstant());
    }
}
