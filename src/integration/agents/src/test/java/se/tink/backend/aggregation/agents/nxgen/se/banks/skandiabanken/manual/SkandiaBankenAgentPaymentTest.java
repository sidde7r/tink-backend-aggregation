package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.manual;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ToAccountFromAccountArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class SkandiaBankenAgentPaymentTest {

    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<ToAccountFromAccountArgumentEnum> toFromManager =
            new ArgumentManager<>(ToAccountFromAccountArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        ssnManager.before();
        toFromManager.before();

        builder =
                new AgentIntegrationTest.Builder("se", "skandiabanken-ssn-bankid")
                        .addCredentialField(Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testTransferImmediately() throws Exception {
        Transfer transfer = create1SekImmediateTransfer();
        builder.build().testBankTransfer(transfer);
    }

    @Test
    public void testTransferFuture() throws Exception {
        Transfer transfer = create1SekFutureTransfer();
        builder.build().testBankTransfer(transfer);
    }

    private Transfer create1SekImmediateTransfer() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Test");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Transfer transfer = new Transfer();

        transfer.setSource(
                new SwedishIdentifier(
                        toFromManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(
                        AccountIdentifierType.SE_BG,
                        toFromManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT)));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.0));
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setSourceMessage("Tink source");
        transfer.setType(TransferType.PAYMENT);
        return transfer;
    }

    private Transfer create1SekFutureTransfer() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Test");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Transfer transfer = new Transfer();

        transfer.setSource(
                new SwedishIdentifier(
                        toFromManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(
                        AccountIdentifierType.SE_BG,
                        toFromManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT)));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.0));
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setSourceMessage("Tink source");
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(getDueDateFuture());
        return transfer;
    }

    private Date getDueDateFuture() {
        return Date.from(
                LocalDate.now().atStartOfDay().plusDays(10).atZone(ZoneId.of("CET")).toInstant());
    }
}
