package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LoanTest {
    private static final String data =
            "{\n"
                    + "  \"courtageClass\": \"MINI\",\n"
                    + "  \"depositable\": true,\n"
                    + "  \"accountType\": \"KreditkontoISK\",\n"
                    + "  \"withdrawable\": true,\n"
                    + "  \"accountId\": \"4654138\",\n"
                    + "  \"accountTypeName\": \"Kreditkonto\",\n"
                    + "  \"clearingNumber\": \"9552\",\n"
                    + "  \"instrumentTransferPossible\": false,\n"
                    + "  \"internalTransferPossible\": true,\n"
                    + "  \"jointlyOwned\": false,\n"
                    + "  \"interestRate\": 0.99,\n"
                    + "  \"creditedAccount\": {\n"
                    + "    \"accountType\": \"Investeringssparkonto\",\n"
                    + "    \"accountId\": \"3099038\",\n"
                    + "    \"name\": \"ISK\"\n"
                    + "  },\n"
                    + "  \"creditLimit\": 500000.00,\n"
                    + "  \"totalBalance\": -22053.39,\n"
                    + "  \"ownCapital\": -22055.33,\n"
                    + "  \"creditAfterInterest\": -22053.39,\n"
                    + "  \"accruedInterest\": -1.94,\n"
                    + "  \"performance\": -120.8600000000007,\n"
                    + "  \"overMortgaged\": false,\n"
                    + "  \"overdrawn\": false,\n"
                    + "  \"performancePercent\": 0.0,\n"
                    + "  \"numberOfTransfers\": 0,\n"
                    + "  \"numberOfIntradayTransfers\": 0\n"
                    + "}";

    private static final String data2 =
            "{\n"
                    + "  \"courtageClass\": \"MINI\",\n"
                    + "  \"depositable\": true,\n"
                    + "  \"accountType\": \"KreditkontoISK\",\n"
                    + "  \"withdrawable\": true,\n"
                    + "  \"accountId\": \"6083920\",\n"
                    + "  \"accountTypeName\": \"Kreditkonto\",\n"
                    + "  \"clearingNumber\": \"9553\",\n"
                    + "  \"instrumentTransferPossible\": false,\n"
                    + "  \"internalTransferPossible\": true,\n"
                    + "  \"jointlyOwned\": false,\n"
                    + "  \"interestRate\": 0.00,\n"
                    + "  \"creditedAccount\": {\n"
                    + "    \"accountType\": \"Investeringssparkonto\",\n"
                    + "    \"name\": \"ISK\",\n"
                    + "    \"accountId\": \"5069023\"\n"
                    + "  },\n"
                    + "  \"creditLimit\": 1000000.00,\n"
                    + "  \"totalBalance\": 3.17,\n"
                    + "  \"ownCapital\": 3.17,\n"
                    + "  \"accruedInterest\": 0.00,\n"
                    + "  \"overMortgaged\": false,\n"
                    + "  \"overdrawn\": false,\n"
                    + "  \"performance\": 0.0,\n"
                    + "  \"creditAfterInterest\": 3.17,\n"
                    + "  \"performancePercent\": 0.0,\n"
                    + "  \"numberOfTransfers\": 0,\n"
                    + "  \"numberOfIntradayTransfers\": 0\n"
                    + "}";

    @Test
    public void testLoanParsing() {
        AccountDetailsResponse deets =
                SerializationUtils.deserializeFromString(data, AccountDetailsResponse.class);
        Optional<LoanAccount> account = deets.toLoanAccount(new HolderName("Some Name"));
        Assert.assertTrue(account.isPresent());
        LoanAccount loanAccount = account.get();
        Assert.assertEquals(
                BigDecimal.valueOf(-22055.33), loanAccount.getExactBalance().getExactValue());
        Assert.assertEquals(
                BigDecimal.valueOf(0.99).divide(BigDecimal.valueOf(100)),
                BigDecimal.valueOf(loanAccount.getInterestRate()));

        deets = SerializationUtils.deserializeFromString(data2, AccountDetailsResponse.class);
        account = deets.toLoanAccount(new HolderName("Some Name"));
        Assert.assertTrue(account.isPresent());
        loanAccount = account.get();
        Assert.assertEquals(
                BigDecimal.valueOf(3.17), loanAccount.getExactBalance().getExactValue());
        Assert.assertEquals(
                BigDecimal.valueOf(0.00).divide(BigDecimal.valueOf(100)),
                BigDecimal.valueOf(loanAccount.getInterestRate()));
    }
}
