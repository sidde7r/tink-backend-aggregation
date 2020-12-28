package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.starling;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class StarlingAgentTest {

    private static final String TRANSFER_SOURCE = "";
    private static final String TRANSFER_DEST = "";
    private static final String STARLING_FINANCIAL_INSTITUTION_ID =
            "b615ccc66e4b4ed1876e80ad397acf56";

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-starling-oauth2")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId(STARLING_FINANCIAL_INSTITUTION_ID)
                .setAppId("tink")
                .build()
                .testRefresh();
    }

    @Test
    public void testTransfer() throws Exception {

        Transfer transfer = new Transfer();

        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSource(
                AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, TRANSFER_SOURCE));
        transfer.setDestination(
                AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, TRANSFER_DEST));
        transfer.setAmount(Amount.valueOf("GBP", 1050, 2));
        transfer.setSourceMessage("Message!");

        new AgentIntegrationTest.Builder("uk", "uk-starling-oauth2")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId(STARLING_FINANCIAL_INSTITUTION_ID)
                .setAppId("tink")
                .build()
                .testBankTransfer(transfer);
    }
}
