package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionalAccountTransactionFetcherTest {

    private static final String API_IDENTIFIER = "123456";
    private static final String PRODUCT_CODE = "KOPYTKO";

    private static final String FIRST_PAGE_DATA_JSON =
            "{\"result\": [{\"amount\": -10.0, \"booked\": true, \"booking_date\": \"2020-07-02\", \"card\": {\"card_number\": \"4002886011094549\"}, \"description\": \"APPLE.COM/BILL\", \"exchange\": {}, \"reference_number\": \"012021\", \"transaction_date\": \"2020-07-02\", \"transaction_id\": \"012021\", \"transaction_type\": {\"transaction_code\": \"NO1401\", \"transaction_code_text\": \"Visa varekjÃ¸p\"} }, {\"amount\": 11000.0, \"archive_id\": \"*17002988\", \"booked\": false, \"booking_date\": \"2020-07-01\", \"card\": {}, \"description\": \"FRA ZXCASD ASDASFA\", \"exchange\": {}, \"message\": \"FRA: ASDFD  SADF AS ASD AS D\", \"originators_reference\": \"300039857244\", \"reference_number\": \"857244\", \"transaction_date\": \"2020-07-01\", \"transaction_id\": \"00039857244\", \"transaction_type\": {\"transaction_code\": \"NO0395\", \"transaction_code_text\": \"Straksinnbetaling\"} } ], \"total_size\": 4834, \"continuation_key\": \"3\"}{\"result\": [{\"amount\": -10.0, \"booked\": false, \"booking_date\": \"2020-07-02\", \"card\": {\"card_number\": \"4002886011094549\"}, \"description\": \"APPLE.COM/BILL\", \"exchange\": {}, \"reference_number\": \"012021\", \"transaction_date\": \"2020-07-02\", \"transaction_id\": \"012021\", \"transaction_type\": {\"transaction_code\": \"NO1401\", \"transaction_code_text\": \"Visa varekjÃ¸p\"} }, {\"amount\": 11000.0, \"archive_id\": \"*17002988\", \"booked\": true, \"booking_date\": \"2020-07-01\", \"card\": {}, \"description\": \"FRA ZXCASD ASDASFA\", \"exchange\": {}, \"message\": \"FRA: ASDFD  SADF AS ASD AS D\", \"originators_reference\": \"300039857244\", \"reference_number\": \"857244\", \"transaction_date\": \"2020-07-01\", \"transaction_id\": \"00039857244\", \"transaction_type\": {\"transaction_code\": \"NO0395\", \"transaction_code_text\": \"Straksinnbetaling\"} } ], \"total_size\": 3, \"continuation_key\": \"3\"}";
    private static final String SECOND_PAGE_DATA_JSON =
            "{\"result\": [{\"amount\": -548.5, \"archive_id\": \"*17326006\", \"booked\": true, \"booking_date\": \"2020-07-01\", \"card\": {\"card_number\": \"601114738571\"}, \"description\": \"FFFFFE OBS SLITU  MORSTONG SLITU\", \"exchange\": {}, \"transaction_date\": \"2020-06-30\", \"transaction_id\": \"65080011511\", \"transaction_type\": {\"transaction_code\": \"NO0028\", \"transaction_code_text\": \"VarekjÃ¸p\"} } ], \"total_size\": 3 }";

    @Test
    public void shouldParseTransactionsCorrectly() {
        // given
        TransactionalAccount account = getAccountForTests();
        FetcherClient fetcherClient = mock(FetcherClient.class);
        given(fetcherClient.fetchAccountTransactions(API_IDENTIFIER, PRODUCT_CODE, null))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                FIRST_PAGE_DATA_JSON, TransactionsResponse.class));
        given(fetcherClient.fetchAccountTransactions(API_IDENTIFIER, PRODUCT_CODE, "3"))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                SECOND_PAGE_DATA_JSON, TransactionsResponse.class));
        TransactionalAccountTransactionFetcher fetcher =
                new TransactionalAccountTransactionFetcher(fetcherClient);

        // when
        TransactionKeyPaginatorResponse<String> firstPageOfTransactions =
                fetcher.getTransactionsFor(account, null);
        TransactionKeyPaginatorResponse<String> secondPageOfTransactions =
                fetcher.getTransactionsFor(account, "3");

        // then
        assertThat(firstPageOfTransactions.getTinkTransactions()).hasSize(2);
        assertThat(secondPageOfTransactions.getTinkTransactions()).hasSize(1);

        Iterator<? extends Transaction> iterator =
                firstPageOfTransactions.getTinkTransactions().iterator();

        assertThatTransactionIsMappedCorrectly(
                iterator.next(), -10.0, "APPLE.COM/BILL", false, LocalDate.of(2020, 7, 2));
        assertThatTransactionIsMappedCorrectly(
                iterator.next(),
                11000.0,
                "FRA: ASDFD  SADF AS ASD AS D",
                true,
                LocalDate.of(2020, 7, 1));

        assertThatTransactionIsMappedCorrectly(
                secondPageOfTransactions.getTinkTransactions().iterator().next(),
                -548.5,
                "FFFFFE OBS SLITU  MORSTONG SLITU",
                false,
                LocalDate.of(2020, 7, 1));
    }

    private TransactionalAccount getAccountForTests() {
        TransactionalAccount mockAccount = mock(TransactionalAccount.class);
        given(mockAccount.getApiIdentifier()).willReturn(API_IDENTIFIER);
        given(mockAccount.getFromTemporaryStorage("productCode")).willReturn(PRODUCT_CODE);
        given(mockAccount.getExactBalance()).willReturn(ExactCurrencyAmount.of(0.0, "NOK"));
        return mockAccount;
    }

    private void assertThatTransactionIsMappedCorrectly(
            Transaction transaction,
            double expectedAmount,
            String expectedDescription,
            boolean expectedPending,
            LocalDate expectedBookingDate) {
        assertThat(transaction.getExactAmount())
                .isEqualTo(ExactCurrencyAmount.of(expectedAmount, "NOK"));
        assertThat(transaction.getDescription()).isEqualTo(expectedDescription);
        assertThat(transaction.isPending()).isEqualTo(expectedPending);
        assertThat(transaction.getDate())
                .isEqualTo(
                        new Date(
                                expectedBookingDate
                                        .atTime(java.time.LocalTime.NOON)
                                        .atZone(ZoneId.of("CET"))
                                        .toInstant()
                                        .toEpochMilli()));
    }
}
