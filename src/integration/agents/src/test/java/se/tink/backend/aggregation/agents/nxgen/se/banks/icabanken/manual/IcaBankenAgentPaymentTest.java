package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.manual;

import java.util.Date;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

// DISCLAIMER! Actual money being transferred, run under own responsability
public class IcaBankenAgentPaymentTest {

    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<IcaBankenAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(IcaBankenAgentPaymentTest.Arg.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        ssnManager.before();
        creditorDebtorManager.before();

        builder =
                new AgentIntegrationTest.Builder("se", "icabanken-bankid")
                        .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                        .expectLoggedIn(true)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRealDomesticPayment() throws Exception {
        builder.build().testBankTransfer(createRealDomesticTransfer());
    }

    private Transfer createRealDomesticTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.0));
        transfer.setSource(new SwedishIdentifier(creditorDebtorManager.get(Arg.DEBTOR_ACCOUNT)));
        transfer.setSourceMessage("Message");
        transfer.setDestination(
                new SwedishIdentifier(creditorDebtorManager.get(Arg.CREDITOR_ACCOUNT)));
        transfer.setRemittanceInformation(
                getRemittanceInformation(RemittanceInformationType.UNSTRUCTURED, "Message"));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDueDate(new Date());

        return transfer;
    }

    private RemittanceInformation getRemittanceInformation(
            RemittanceInformationType type, String value) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(type);
        remittanceInformation.setValue(value);
        return remittanceInformation;
    }

    private enum Arg implements ArgumentManagerEnum {
        DEBTOR_ACCOUNT, // Domestic Swedish account number
        CREDITOR_ACCOUNT; // Domestic Swedish account number

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
