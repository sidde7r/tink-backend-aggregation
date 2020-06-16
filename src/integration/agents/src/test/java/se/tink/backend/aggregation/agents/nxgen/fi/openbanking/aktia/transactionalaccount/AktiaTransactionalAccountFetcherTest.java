package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createAccountsSummaryResponseWithError;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createSuccessfulAccountsSummaryResponse;

import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.AccountSummaryItemDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.AccountsSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.transactionalaccount.converter.AktiaTransactionalAccountConverter;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AktiaTransactionalAccountFetcherTest {

    private AktiaTransactionalAccountFetcher transactionalAccountFetcher;

    private AktiaApiClient aktiaApiClientMock;

    private AktiaTransactionalAccountConverter transactionalAccountConverterMock;

    @Before
    public void setUp() {
        aktiaApiClientMock = mock(AktiaApiClient.class);
        transactionalAccountConverterMock = mock(AktiaTransactionalAccountConverter.class);

        transactionalAccountFetcher =
                new AktiaTransactionalAccountFetcher(
                        aktiaApiClientMock, transactionalAccountConverterMock);
    }

    @Test
    public void shouldFetchAccounts() {
        // given
        final AccountsSummaryResponse accountsSummaryResponse =
                createSuccessfulAccountsSummaryResponse();
        when(aktiaApiClientMock.getAccountsSummary()).thenReturn(accountsSummaryResponse);

        final TransactionalAccount transactionalAccountMock = mock(TransactionalAccount.class);
        when(transactionalAccountConverterMock.toTransactionalAccount(
                        any(AccountSummaryItemDto.class)))
                .thenReturn(Optional.of(transactionalAccountMock));

        // when
        final Collection<TransactionalAccount> resultAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(resultAccounts).containsExactly(transactionalAccountMock);
    }

    @Test
    public void shouldFetchEmptyAccountsList() {
        // given
        final AccountsSummaryResponse accountsSummaryResponse =
                createSuccessfulAccountsSummaryResponse();
        when(aktiaApiClientMock.getAccountsSummary()).thenReturn(accountsSummaryResponse);

        when(transactionalAccountConverterMock.toTransactionalAccount(
                        any(AccountSummaryItemDto.class)))
                .thenReturn(Optional.empty());

        // when
        final Collection<TransactionalAccount> resultAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(resultAccounts).isEmpty();
    }

    @Test
    public void shouldThrowExceptionForResponseWithErrors() {
        // given
        final AccountsSummaryResponse accountsSummaryResponse =
                createAccountsSummaryResponseWithError();
        when(aktiaApiClientMock.getAccountsSummary()).thenReturn(accountsSummaryResponse);

        // when
        final Throwable thrown = catchThrowable(transactionalAccountFetcher::fetchAccounts);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fetching accounts failed.");
    }
}
