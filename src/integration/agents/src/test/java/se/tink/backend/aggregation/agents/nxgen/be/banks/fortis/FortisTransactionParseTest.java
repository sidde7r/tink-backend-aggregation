package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import java.util.Collection;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.EbankingUsersResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.UpcomingTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FortisTransactionParseTest {

    @Test
    public void bulkErrorMessage() {
        EbankingUsersResponse ebankingUsersResponse =
                SerializationUtils.deserializeFromString(
                        FortisTestData.BULK_ERROR_MESSAGE, EbankingUsersResponse.class);

        Assert.assertEquals(
                "Global error",
                "E",
                ebankingUsersResponse.getBusinessMessageBulk().getGlobalIndicator());
    }

    @Test
    public void transactionPagination() {
        TransactionsResponse transactionsResponse =
                SerializationUtils.deserializeFromString(
                        FortisTestData.TRANSACTIONS_EMPTY_RESPONSE, TransactionsResponse.class);

        Optional<Boolean> aBoolean = transactionsResponse.canFetchMore();

        Assert.assertFalse(aBoolean.orElseThrow(IllegalArgumentException::new));
    }

    @Test
    public void transactionPagenationMoreAvailable() {
        TransactionsResponse transactionsResponse = FortisTestData.TRANSACTIONS_RESPONSE;

        Assert.assertEquals("Transactions", 12, transactionsResponse.getTinkTransactions().size());
        Optional<Boolean> aBoolean = transactionsResponse.canFetchMore();

        Assert.assertTrue(aBoolean.orElseThrow(IllegalArgumentException::new));
    }

    @Test
    public void upcomingTransactions() {
        String upcoming = FortisTestData.UPCOMING_TRANSACTIONS;
        UpcomingTransactionsResponse upcomingTransaction =
                SerializationUtils.deserializeFromString(
                        upcoming, UpcomingTransactionsResponse.class);

        Collection<? extends UpcomingTransaction> tinkTransactions =
                upcomingTransaction.getTinkTransactions();

        Assert.assertEquals("Upcoming transactions", 1, tinkTransactions.size());
        Assert.assertFalse(
                upcomingTransaction.canFetchMore().orElseThrow(IllegalArgumentException::new));
    }

    @Test
    public void upcomingTransactionsNoMore() {
        String upcoming = FortisTestData.UPCOMING_TRANSACTIONS_EMPTY;
        UpcomingTransactionsResponse upcomingTransaction =
                SerializationUtils.deserializeFromString(
                        upcoming, UpcomingTransactionsResponse.class);

        Collection<? extends UpcomingTransaction> tinkTransactions =
                upcomingTransaction.getTinkTransactions();

        Assert.assertFalse(
                upcomingTransaction.canFetchMore().orElseThrow(IllegalArgumentException::new));
    }
}
