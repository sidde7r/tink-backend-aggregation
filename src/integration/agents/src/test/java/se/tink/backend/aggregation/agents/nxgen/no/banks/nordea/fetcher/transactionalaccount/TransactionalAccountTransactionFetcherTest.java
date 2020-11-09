package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import org.junit.Before;
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

    private static final String FIRST_PAGE_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources/transactionalTransactionsPage1.json";

    private static final String SECOND_PAGE_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources/transactionalTransactionsPage2.json";

    private static final TransactionsResponse FIRST_PAGE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(FIRST_PAGE_FILE_PATH), TransactionsResponse.class);
    private static final TransactionsResponse SECOND_PAGE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(SECOND_PAGE_FILE_PATH), TransactionsResponse.class);

    private FetcherClient fetcherClient;

    private TransactionalAccountTransactionFetcher fetcher;

    @Before
    public void setup() {
        fetcherClient = mock(FetcherClient.class);
        fetcher = new TransactionalAccountTransactionFetcher(fetcherClient);
    }

    @Test
    public void shouldParseTransactionsCorrectly() {
        // given
        TransactionalAccount account = getAccountForTests();
        given(fetcherClient.fetchAccountTransactions(API_IDENTIFIER, PRODUCT_CODE, null))
                .willReturn(FIRST_PAGE_RESPONSE);
        given(fetcherClient.fetchAccountTransactions(API_IDENTIFIER, PRODUCT_CODE, "3"))
                .willReturn(SECOND_PAGE_RESPONSE);

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

    @Test
    public void shouldNotFetchTransactionsIfNoPermissionOnAccountToDoSo() {
        // given
        TransactionalAccount mockAccount = mock(TransactionalAccount.class);
        given(mockAccount.getFromTemporaryStorage("canFetchTransactions", Boolean.class))
                .willReturn(Optional.of(Boolean.FALSE));

        // when
        TransactionKeyPaginatorResponse<String> transactionsFor =
                fetcher.getTransactionsFor(mockAccount, null);

        // then
        assertThat(transactionsFor.getTinkTransactions()).isEmpty();
        verifyNoMoreInteractions(fetcherClient);
    }

    private TransactionalAccount getAccountForTests() {
        TransactionalAccount mockAccount = mock(TransactionalAccount.class);
        given(mockAccount.getApiIdentifier()).willReturn(API_IDENTIFIER);
        given(mockAccount.getFromTemporaryStorage("productCode")).willReturn(PRODUCT_CODE);
        given(mockAccount.getFromTemporaryStorage("canFetchTransactions", Boolean.class))
                .willReturn(Optional.of(Boolean.TRUE));
        given(mockAccount.getExactBalance()).willReturn(ExactCurrencyAmount.zero("NOK"));
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
