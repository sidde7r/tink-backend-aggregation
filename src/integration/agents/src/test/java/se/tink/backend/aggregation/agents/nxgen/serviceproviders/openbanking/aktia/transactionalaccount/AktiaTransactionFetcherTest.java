package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.AktiaTestFixtures.ACCOUNT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.AktiaTestFixtures.AMOUNT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.AktiaTestFixtures.CONTINUATION_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.AktiaTestFixtures.TRANSACTION_MESSAGE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.AktiaTestFixtures.createPaginatorResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.AktiaTestFixtures.createTransactionsAndLockedEventsResponseForError;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.AktiaTestFixtures.createTransactionsAndLockedEventsResponseWithContinuationKey;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.AktiaTestFixtures.getTransactionalAccount;

import java.math.BigDecimal;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.dto.response.TransactionsAndLockedEventsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.response.TransactionsAndLockedEventsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.transactionalaccount.converter.AktiaTransactionalAccountConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class AktiaTransactionFetcherTest {

    private AktiaTransactionFetcher transactionFetcher;

    private AktiaApiClient aktiaApiClientMock;

    private AktiaTransactionalAccountConverter transactionalAccountConverterMock;

    @Before
    public void setUp() {
        aktiaApiClientMock = mock(AktiaApiClient.class);
        transactionalAccountConverterMock = mock(AktiaTransactionalAccountConverter.class);

        transactionFetcher =
                new AktiaTransactionFetcher(aktiaApiClientMock, transactionalAccountConverterMock);
    }

    @Test
    public void shouldGetTransactionsFor() {
        // given
        final TransactionsAndLockedEventsResponse transactionsAndLockedEventsResponse =
                createTransactionsAndLockedEventsResponseWithContinuationKey();
        when(aktiaApiClientMock.getTransactionsAndLockedEvents(ACCOUNT_ID, CONTINUATION_KEY))
                .thenReturn(transactionsAndLockedEventsResponse);

        final TransactionalAccount transactionalAccount = getTransactionalAccount();

        final TransactionKeyPaginatorResponse<String> paginatorResponse = createPaginatorResponse();
        when(transactionalAccountConverterMock.toPaginatorResponse(
                        any(TransactionsAndLockedEventsResponseDto.class)))
                .thenReturn(paginatorResponse);

        // when
        final PaginatorResponse response =
                transactionFetcher.getTransactionsFor(transactionalAccount, CONTINUATION_KEY);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTinkTransactions()).hasSize(1);
        final Transaction resultTransaction =
                new ArrayList<>(response.getTinkTransactions()).get(0);

        assertThat(resultTransaction.isPending()).isFalse();
        assertThat(resultTransaction.getExactAmount().getCurrencyCode()).isEqualTo("EUR");
        assertThat(resultTransaction.getExactAmount().getExactValue())
                .isEqualTo(new BigDecimal(AMOUNT));
        assertThat(resultTransaction.getDescription()).isEqualTo(TRANSACTION_MESSAGE);
    }

    @Test
    public void shouldThrowExceptionForResponseWithErrors() {
        // given
        final TransactionsAndLockedEventsResponse transactionsAndLockedEventsResponse =
                createTransactionsAndLockedEventsResponseForError();
        when(aktiaApiClientMock.getTransactionsAndLockedEvents(ACCOUNT_ID, CONTINUATION_KEY))
                .thenReturn(transactionsAndLockedEventsResponse);

        final TransactionalAccount transactionalAccount = getTransactionalAccount();

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                transactionFetcher.getTransactionsFor(
                                        transactionalAccount, CONTINUATION_KEY));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fetching transactions failed.");
    }
}
