package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import java.util.Date;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.enums.MessageType;
import se.tink.libraries.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;

@Ignore
public class ModeloAgentTest {
    private AccountIdentifier sourceAccount;
    private AccountIdentifier destinationAccount;

    private AgentIntegrationTest.Builder getAgentBuilder() {
        return new AgentIntegrationTest.Builder("uk", "uk-modelo-oauth2")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .doLogout(false);
    }

    @Before
    public void setup() {
        // These account numbers are taken from their demo data. It might have changed!
        this.sourceAccount = new SortCodeIdentifier("10000119820101");
        this.destinationAccount = new SortCodeIdentifier("10000190210003");
    }

    @Test
    public void testRefresh() throws Exception {
        getAgentBuilder()
                .build()
                .testRefresh();
    }

    @Test
    public void testBanktransfer() throws Exception {

        Transfer transfer = createTransfer(
                sourceAccount,
                destinationAccount,
                null,
                "Test transfer",
                Amount.valueOf("GBP", 100, 2)
        );

        getAgentBuilder()
                .build()
                .testBankTransfer(transfer);
    }

    private Transfer createTransfer(AccountIdentifier sourceAccount, AccountIdentifier destinationAccount,
            Date dueDate, String destinationMessage, Amount amount) {
        Transfer transfer = new Transfer();

        transfer.setSource(sourceAccount);
        transfer.setDestination(destinationAccount);
        transfer.setAmount(amount);
        transfer.setDueDate(dueDate);
        transfer.setMessageType(MessageType.FREE_TEXT);
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDestinationMessage(destinationMessage);

        return transfer;
    }
}
