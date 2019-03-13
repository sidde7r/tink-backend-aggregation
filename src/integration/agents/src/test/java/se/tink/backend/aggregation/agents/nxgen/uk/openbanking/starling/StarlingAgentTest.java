package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

@Ignore
public class StarlingAgentTest {

    private static final String TRANSFER_SOURCE = "";
    private static final String TRANSFER_DEST = "";

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-starling-oauth2")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }

    @Test
    public void testTransfer() throws Exception {

        Transfer transfer = new Transfer();

        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSource(AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, TRANSFER_SOURCE));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, TRANSFER_DEST));
        transfer.setAmount(Amount.valueOf("GBP", 1050, 2));
        transfer.setSourceMessage("Message!");

        new AgentIntegrationTest.Builder("uk", "uk-starling-oauth2")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .build()
                .testBankTransfer(transfer);
    }
}
