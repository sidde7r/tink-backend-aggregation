package se.tink.backend.aggregation.agents.banks.seb.manual;

import java.util.Date;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

// DISCLAIMER! Actual money being transferred, run under own responsibility
public class SebApiAgentDomesticTransferTest {

    private final ArgumentManager<Arg> creditorDebtorManager = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    private enum Arg implements ArgumentManagerEnum {
        DEBTOR_ACCOUNT, // Domestic Swedish account number
        CREDITOR_ACCOUNT; // Domestic Swedish account number

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    @Before
    public void setup() {
        creditorDebtorManager.before();
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testPayments() throws Exception {
        new AgentIntegrationTest.Builder("SE", "seb-bankid")
                .addCredentialField(Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testBankTransfer(createTransfer());
    }

    private Transfer createTransfer() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Message");
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.0));
        transfer.setSource(new SwedishIdentifier(creditorDebtorManager.get(Arg.DEBTOR_ACCOUNT)));
        transfer.setSourceMessage("Message");
        transfer.setDestination(
                new SwedishIdentifier(creditorDebtorManager.get(Arg.CREDITOR_ACCOUNT)));
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDueDate(new Date());

        return transfer;
    }
}
