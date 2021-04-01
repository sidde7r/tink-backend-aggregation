package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
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

    private static final String SUPER_BOLAN_DATA =
            "{\n"
                    + "  \"courtageClass\": \"PRO2\",\n"
                    + "  \"depositable\": true,\n"
                    + "  \"accountType\": \"Superbolanekonto\",\n"
                    + "  \"withdrawable\": true,\n"
                    + "  \"accountId\": \"6663592\",\n"
                    + "  \"accountTypeName\": \"Superbolånet PB\",\n"
                    + "  \"clearingNumber\": \"9552\",\n"
                    + "  \"instrumentTransferPossible\": false,\n"
                    + "  \"internalTransferPossible\": true,\n"
                    + "  \"jointlyOwned\": false,\n"
                    + "  \"interestRate\": 0.79,\n"
                    + "  \"nextPaymentPrognosis\": 3528.60,\n"
                    + "  \"totalBalanceDue\": 5378947.00,\n"
                    + "  \"remainingLoan\": 170700.14,\n"
                    + "  \"nextPaymentDate\": \"2019-10-31\",\n"
                    + "  \"numberOfTransfers\": 0,\n"
                    + "  \"numberOfIntradayTransfers\": 0,\n"
                    + "  \"sharpeRatio\": -2.0391090573654393,\n"
                    + "  \"standardDeviation\": 0.1586169482290629\n"
                    + "}";

    @Test
    @Ignore // TODO previously unmaintained -- should be fixed
    public void testLoanParsing() {
        AccountDetailsResponse deets =
                SerializationUtils.deserializeFromString(data, AccountDetailsResponse.class);
        Optional<LoanAccount> account = deets.toLoanAccount("Some Name");
        Assert.assertTrue(account.isPresent());
        LoanAccount loanAccount = account.get();
        Assert.assertEquals(
                BigDecimal.valueOf(-22055.33), loanAccount.getExactBalance().getExactValue());
        Assert.assertEquals(
                BigDecimal.valueOf(0.99).divide(BigDecimal.valueOf(100)),
                BigDecimal.valueOf(loanAccount.getInterestRate()));

        deets = SerializationUtils.deserializeFromString(data2, AccountDetailsResponse.class);
        account = deets.toLoanAccount("Some Name");
        Assert.assertTrue(account.isPresent());
        loanAccount = account.get();
        Assert.assertEquals(
                BigDecimal.valueOf(3.17), loanAccount.getExactBalance().getExactValue());
        Assert.assertEquals(
                BigDecimal.valueOf(0.00).divide(BigDecimal.valueOf(100)),
                BigDecimal.valueOf(loanAccount.getInterestRate()));
    }

    @Test
    @Ignore // TODO previously unmaintained -- should be fixed
    public void testSuperbolanParsing() {
        AccountDetailsResponse deets =
                SerializationUtils.deserializeFromString(
                        SUPER_BOLAN_DATA, AccountDetailsResponse.class);
        Optional<LoanAccount> account = deets.toLoanAccount("Some Name");
        Assert.assertTrue(account.isPresent());
        LoanAccount loanAccount = account.get();
        Assert.assertEquals(
                BigDecimal.valueOf(170700.14), loanAccount.getExactBalance().getExactValue());
        Assert.assertEquals(
                ExactCurrencyAmount.inSEK(5378947.00),
                loanAccount.getDetails().getInitialBalance());
        Assert.assertEquals(
                0,
                BigDecimal.valueOf(3528.60)
                        .compareTo(
                                loanAccount
                                        .getDetails()
                                        .getExactMonthlyAmortization()
                                        .getExactValue()));
        Assert.assertEquals(
                BigDecimal.valueOf(5378947.00).subtract(BigDecimal.valueOf(170700.14)),
                loanAccount.getDetails().getExactAmortized().getExactValue());
        Assert.assertEquals(
                BigDecimal.valueOf(0.79).divide(BigDecimal.valueOf(100)),
                BigDecimal.valueOf(loanAccount.getInterestRate()));
    }
}
