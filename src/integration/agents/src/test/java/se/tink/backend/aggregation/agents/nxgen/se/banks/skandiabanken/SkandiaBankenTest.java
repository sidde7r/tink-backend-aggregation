package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.SkandiaBankenUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.rpc.FetchPaymentsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SkandiaBankenTest {
    final String TEST_DATA_PATH = "data/test/agents/skandiabanken";

    private <T> T loadTestResponse(String path, Class<T> cls) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, path).toFile(), cls);
    }

    @Test
    public void testParseApprovedPayments() {
        final FetchPaymentsResponse response =
                loadTestResponse("approved-payments.json", FetchPaymentsResponse.class);

        assertEquals(7, response.size());
    }

    private TransactionalAccount mockedTransactionalAccount(String accountNumber) {
        TransactionalAccount account = mock(TransactionalAccount.class);
        when(account.getAccountNumber()).thenReturn(accountNumber);
        return account;
    }

    @Test
    public void testUpcomingTransactionFetcher() {
        final FetchPaymentsResponse approvedPaymentsResponse =
                loadTestResponse("approved-payments.json", FetchPaymentsResponse.class);

        // mock client and accounts
        final SkandiaBankenApiClient apiClient = mock(SkandiaBankenApiClient.class);
        when(apiClient.fetchApprovedPayments()).thenReturn(approvedPaymentsResponse);
        final TransactionalAccount account1 = mockedTransactionalAccount("12345678900");
        final TransactionalAccount account2 = mockedTransactionalAccount("23456789012");

        final SkandiaBankenUpcomingTransactionFetcher fetcher =
                new SkandiaBankenUpcomingTransactionFetcher(apiClient);
        final Collection<UpcomingTransaction> upcomingTransactions1 =
                fetcher.fetchUpcomingTransactionsFor(account1);
        assertEquals(5, upcomingTransactions1.size());

        final Collection<UpcomingTransaction> upcomingTransactions2 =
                fetcher.fetchUpcomingTransactionsFor(account2);
        assertEquals(1, upcomingTransactions2.size());
    }
}
