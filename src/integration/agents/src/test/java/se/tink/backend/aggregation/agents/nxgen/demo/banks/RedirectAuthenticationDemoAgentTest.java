package se.tink.backend.aggregation.agents.nxgen.demo.banks;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class RedirectAuthenticationDemoAgentTest {
    private final String SOURCE_IDENTIFIER = "";
    private final String DESTINATION_IDENTIFIER = "";
    private final String SOURCE_ACCOUNT_NAME = "";

    @Test
    public void testTransfer() throws Exception {

        Transfer transfer = new Transfer();
        transfer.setType(TransferType.BANK_TRANSFER);
        AccountIdentifier sourceAccount =
                AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, SOURCE_IDENTIFIER);
        sourceAccount.setName(SOURCE_ACCOUNT_NAME);
        transfer.setSource(sourceAccount);
        transfer.setDestination(
                AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, DESTINATION_IDENTIFIER));

        // todo: Should fix this to use ExactCurrencyAmount instead of Amount
        //        BigDecimal d = new BigDecimal(1);
        //        ExactCurrencyAmount amount = new ExactCurrencyAmount(d, "GBP");
        //        LocalDate executionDate = LocalDate.now();
        //        String currency = "GBP";

        transfer.setAmount(Amount.valueOf("GBP", 1050, 2));
        transfer.setSourceMessage("TRANSFER, test Tink!");

        new AgentIntegrationTest.Builder("uk", "uk-test-open-banking-redirect")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("dummy")
                .setAppId("dummy")
                .build()
                .testBankTransfer(transfer);
    }
}
