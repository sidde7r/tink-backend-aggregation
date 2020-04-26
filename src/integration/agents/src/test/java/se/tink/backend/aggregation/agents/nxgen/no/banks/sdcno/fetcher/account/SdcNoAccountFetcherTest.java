package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SdcNoAccountFetcherTest {

    private SdcNoApiClient client;
    private SdcNoAccountFetcher fetcher;

    @Captor
    ArgumentCaptor<FilterAccountsRequest> captor =
            ArgumentCaptor.forClass(FilterAccountsRequest.class);

    @Before
    public void setUp() {
        client = mock(SdcNoApiClient.class);
        fetcher = new SdcNoAccountFetcher(client);
    }

    @Test
    public void fetchAccountsShouldTakeAccountsFromClientAndTransform() {
        // given
        TransactionalAccount transactionalAccount = transactionalAccount();
        // and
        FilterAccountsResponse response = mock(FilterAccountsResponse.class);
        given(response.getTinkAccounts())
                .willReturn(Collections.singletonList(transactionalAccount));
        // and
        given(client.filterAccounts(any(FilterAccountsRequest.class))).willReturn(response);

        // when
        Collection<TransactionalAccount> result = fetcher.fetchAccounts();

        // then
        verify(client).filterAccounts(captor.capture());
        // and
        FilterAccountsRequest request = captor.getValue();
        assertThat(request.isIncludeCreditAccounts()).isTrue();
        assertThat(request.isIncludeDebitAccounts()).isTrue();
        assertThat(request.isOnlyFavorites()).isFalse();
        assertThat(request.isOnlyQueryable()).isTrue();
        // and
        assertThat(result).containsOnly(transactionalAccount);
    }

    private TransactionalAccount transactionalAccount() {
        return TransactionalAccount.builder(
                        AccountTypes.CHECKING, "sample id", ExactCurrencyAmount.inEUR(12.34d))
                .setAccountNumber("sample account number")
                .setName("sample name")
                .setBankIdentifier("sample bank identifier")
                .build();
    }
}
