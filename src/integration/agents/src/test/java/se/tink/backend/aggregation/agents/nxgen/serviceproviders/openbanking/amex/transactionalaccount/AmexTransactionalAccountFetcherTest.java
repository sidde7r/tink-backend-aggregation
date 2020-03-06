package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createAccountsResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createBalancesResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createHmacAccountIds;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createHmacToken;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createMultiToken;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.apiclient.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.BalanceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.converter.AmexTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIdStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIds;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac.HmacMultiTokenStorage;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacMultiToken;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;

public class AmexTransactionalAccountFetcherTest {

    private AmexTransactionalAccountFetcher amexTransactionalAccountFetcher;

    private AmexApiClient amexApiClientMock;

    private HmacMultiTokenStorage hmacMultiTokenStorageMock;

    private HmacAccountIdStorage hmacAccountIdStorageMock;

    private AmexTransactionalAccountConverter amexTransactionalAccountConverterMock;

    @Before
    public void setUp() {
        amexApiClientMock = mock(AmexApiClient.class);
        hmacMultiTokenStorageMock = mock(HmacMultiTokenStorage.class);
        hmacAccountIdStorageMock = mock(HmacAccountIdStorage.class);
        amexTransactionalAccountConverterMock = mock(AmexTransactionalAccountConverter.class);

        amexTransactionalAccountFetcher =
                new AmexTransactionalAccountFetcher(
                        amexApiClientMock,
                        hmacMultiTokenStorageMock,
                        hmacAccountIdStorageMock,
                        amexTransactionalAccountConverterMock);
    }

    @Test
    public void shouldFetchAccounts() {
        // given
        final HmacToken hmacToken = createHmacToken();
        final AccountsResponseDto accountsResponse = createAccountsResponse();
        when(amexApiClientMock.fetchAccounts(hmacToken)).thenReturn(accountsResponse);

        final List<BalanceDto> balancesResponse = createBalancesResponse();
        when(amexApiClientMock.fetchBalances(hmacToken)).thenReturn(balancesResponse);

        final HmacMultiToken hmacMultiToken = createMultiToken(hmacToken);
        when(hmacMultiTokenStorageMock.getToken()).thenReturn(Optional.of(hmacMultiToken));

        final TransactionalAccount transactionalAccountMock = mock(TransactionalAccount.class);
        when(amexTransactionalAccountConverterMock.toTransactionalAccount(
                        accountsResponse, balancesResponse))
                .thenReturn(Optional.of(transactionalAccountMock));

        // when
        final Collection<TransactionalAccount> resultTransactions =
                amexTransactionalAccountFetcher.fetchAccounts();

        // then
        verify(amexApiClientMock).fetchAccounts(hmacToken);
        verify(amexApiClientMock).fetchBalances(hmacToken);

        final HmacAccountIds expectedHmacAccountIds = createHmacAccountIds(hmacToken);
        verify(hmacAccountIdStorageMock).store(expectedHmacAccountIds);

        assertThat(resultTransactions).containsExactly(transactionalAccountMock);
    }

    @Test
    public void shouldThrowExceptionWhenMultiTokenWasNotFound() {
        // given
        when(hmacMultiTokenStorageMock.getToken()).thenReturn(Optional.empty());

        // when
        final Throwable thrown =
                catchThrowable(() -> amexTransactionalAccountFetcher.fetchAccounts());

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hmac token was not found.");

        verify(amexApiClientMock, never()).fetchAccounts(any());
        verify(amexApiClientMock, never()).fetchBalances(any());
        verify(hmacAccountIdStorageMock, never()).store(any());
    }
}
