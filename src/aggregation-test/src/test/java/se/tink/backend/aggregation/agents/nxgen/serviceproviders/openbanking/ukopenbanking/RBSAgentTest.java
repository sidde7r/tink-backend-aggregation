package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import java.util.Date;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.MessageType;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;

@Ignore
public class RBSAgentTest {
    private AgentIntegrationTest.Builder getAgentBuilder() {
        return new AgentIntegrationTest.Builder("uk", "uk-rbs-oauth2")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .doLogout(false);
    }

    @Test
    public void testRefresh() throws Exception {
        getAgentBuilder()
                .build()
                .testRefresh();
    }

    @Test
    public void testBankTransfer() throws Exception {
        AccountIdentifier sourceAccount = createSortCodeIdentifier("-", "name");
        AccountIdentifier destinationAccount = createSortCodeIdentifier("-", "name");

        Transfer transfer = createTransfer(
                sourceAccount,
                destinationAccount,
                null,
                "Test message",
                Amount.valueOf("GBP", 100, 2)
        );

        getAgentBuilder()
                .build()
                .testBankTransfer(transfer);
    }

    private AccountIdentifier createSortCodeIdentifier(String sortCodeAccountNumber, String accountName) {
        AccountIdentifier identifier = new SortCodeIdentifier(sortCodeAccountNumber);
        identifier.setName(accountName);
        return identifier;
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
