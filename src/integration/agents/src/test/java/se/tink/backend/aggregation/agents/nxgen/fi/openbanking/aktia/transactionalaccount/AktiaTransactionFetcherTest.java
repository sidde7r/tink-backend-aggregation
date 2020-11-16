package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.ACCOUNT_ID;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.CONTINUATION_KEY;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createTransactionsAndLockedEventsResponseForError;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.getTransactionalAccount;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.TransactionsAndLockedEventsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.TransactionsAndLockedEventsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AktiaTransactionFetcherTest {

    private AktiaTransactionFetcher transactionFetcher;

    private AktiaApiClient aktiaApiClientMock;

    @Before
    public void setUp() {
        aktiaApiClientMock = mock(AktiaApiClient.class);

        transactionFetcher = new AktiaTransactionFetcher(aktiaApiClientMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetTransactionsFor() {
        // given
        final TransactionalAccount transactionalAccount = getTransactionalAccount();

        final TransactionsAndLockedEventsResponseDto dto =
                mock(TransactionsAndLockedEventsResponseDto.class);

        final TransactionsAndLockedEventsResponse response =
                mock(TransactionsAndLockedEventsResponse.class);

        when(response.isSuccessful()).thenReturn(true);
        when(response.getTransactionsAndLockedEventsResponseDto()).thenReturn(dto);

        when(aktiaApiClientMock.getTransactionsAndLockedEvents(ACCOUNT_ID, CONTINUATION_KEY))
                .thenReturn(response);

        // when
        final PaginatorResponse result =
                transactionFetcher.getTransactionsFor(transactionalAccount, CONTINUATION_KEY);

        // then
        assertThat(result).isNotEqualTo(null);
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
