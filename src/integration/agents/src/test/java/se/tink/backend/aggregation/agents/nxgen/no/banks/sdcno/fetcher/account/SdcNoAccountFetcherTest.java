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
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.AccountNumberToIbanConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.OtherIdentifier;
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
        fetcher = new SdcNoAccountFetcher(client, accountNumber -> accountNumber);
    }

    @Test
    public void fetchAccountsShouldTakeAccountsFromClientAndTransform() {
        // given
        TransactionalAccount transactionalAccount = transactionalAccount();
        // and
        FilterAccountsResponse response = mock(FilterAccountsResponse.class);
        given(response.getTinkAccounts(any(AccountNumberToIbanConverter.class)))
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
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(12.34)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("UN123")
                                .withAccountNumber("AN123")
                                .withAccountName("NM123")
                                .addIdentifier(new OtherIdentifier("ID123"))
                                .build())
                .build()
                .orElse(null);
    }
}
